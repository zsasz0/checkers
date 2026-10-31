package modules.ui.application.use_cases;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class MainWindow {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private final Stage stage;
    private final Scene modeSelectionScene;
    private Scene gameScene;

    public MainWindow(Stage stage, GameModeSelectionView selectionView) {
        this.stage = stage;
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setResizable(false);

        // Build mode selection scene with left sidebar
        BorderPane selectionRoot = new BorderPane();
        selectionRoot.setLeft(createBrandSidebar());
        selectionRoot.setCenter(selectionView);
        selectionView.setMaxWidth(400);   // keep the selection view compact
        selectionView.setMaxHeight(400);

        this.modeSelectionScene = new Scene(selectionRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private VBox createBrandSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(160);
        sidebar.setPadding(new Insets(20));
        sidebar.setSpacing(10);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-background-color: #333;");

        Label title = new Label("CHECKERS");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        sidebar.getChildren().add(title);

        return sidebar;
    }

    public void showModeSelection() {
        stage.setScene(modeSelectionScene);
        stage.setTitle("Checkers - Select Mode");
        stage.show();
    }

    public void showGame(BoardView boardView, SideBar sideBar) {
        BorderPane gameRoot = new BorderPane();
        gameRoot.setLeft(sideBar);          // sidebar on left
        gameRoot.setCenter(boardView);

        gameScene = new Scene(gameRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(gameScene);
        stage.setTitle("Checkers");
        stage.show();
    }
}