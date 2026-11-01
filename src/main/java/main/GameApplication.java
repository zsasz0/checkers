package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import modules.game.domain.GameMode;
import modules.ui.application.use_cases.BoardView;
import modules.ui.application.use_cases.GameModeSelectionView;
import modules.ui.application.use_cases.MainWindow;
import modules.ui.application.use_cases.SideBar;
import main.config.ApplicationContext;
import main.config.GameConfig;

public class GameApplication extends Application {

    private ApplicationContext context;
    private GameConfig gameConfig;
    private MainWindow mainWindow;

    @Override
    public void start(Stage stage) {
        context = new ApplicationContext();
        gameConfig = new GameConfig(context);

        GameModeSelectionView selectionView = new GameModeSelectionView((mode, depth) -> {
            startGameWithMode(mode, depth);
        });

        mainWindow = new MainWindow(stage, selectionView);
        mainWindow.showModeSelection();
    }

    private void startGameWithMode(GameMode mode, int depth) {
        BoardView boardView = new BoardView();
        SideBar sideBar = new SideBar(() -> mainWindow.showModeSelection());

        switch (mode) {
        case AI -> gameConfig.setupAIGame(boardView, sideBar, depth);
        case OFFLINE -> gameConfig.setupOfflineGame(boardView, sideBar);
        case AI_VS_AI -> System.out.println("");
        case CLI -> System.out.println("");

        default -> {
            System.out.println("Mode not implemented yet: " + mode);
            return;
        }
    }

        mainWindow.showGame(boardView, sideBar);
        Platform.runLater(boardView::render);
    }
}