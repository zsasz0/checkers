package modules.ui.application.use_cases;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.game.domain.GameMode;

import java.util.function.BiConsumer;

public class GameModeSelectionView extends VBox {

    private static final int MIN_AI_DEPTH = 1;
    private static final int MAX_AI_DEPTH = 20;
    private static final int DEFAULT_AI_DEPTH = 12;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private final BiConsumer<GameMode, Integer> onStart;

    public GameModeSelectionView(BiConsumer<GameMode, Integer> onStart) {
        this.onStart = onStart;

        setPrefWidth(220);
        setMinWidth(220);
        setMaxWidth(220);

        setAlignment(Pos.CENTER);
        setSpacing(15);

        setStyle(
                "-fx-padding: 30;" +
                "-fx-background-color: #333;"
        );

        Label title = new Label("GAME MODES");
        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;"
        );

        getChildren().add(title);

        addModeOption("Play vs AI", GameMode.AI);
        addModeOption("CLI vs AI", GameMode.CLI);
        addModeOption("Offline (2 Players)", GameMode.OFFLINE);
        addModeOption("Online", GameMode.ONLINE);
        addModeOption("Challenge", GameMode.CHALLENGE);
        addModeOption("AI vs AI", GameMode.AI_VS_AI);

        Label depthLabel = new Label("AI Depth");
        depthLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;"
        );

        ComboBox<Integer> depthBox = new ComboBox<>();
        for (int depth = MIN_AI_DEPTH; depth <= MAX_AI_DEPTH; depth++) {
            depthBox.getItems().add(depth);
        }
        depthBox.setValue(DEFAULT_AI_DEPTH);
        depthBox.setPrefWidth(80);

        HBox depthRow = new HBox(10, depthLabel, depthBox);
        depthRow.setAlignment(Pos.CENTER);

        getChildren().add(depthRow);

        Button startButton = new Button("Start Game");

        startButton.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-padding: 8 20;"
        );

        startButton.setOnAction(e -> {

            RadioButton selected =
                    (RadioButton) toggleGroup.getSelectedToggle();

            if (selected != null) {

                GameMode mode =
                        (GameMode) selected.getUserData();

                int depth = depthBox.getValue();

                onStart.accept(mode, depth);
            }
        });

        getChildren().add(startButton);
    }

    private void addModeOption(
            String text,
            GameMode mode) {

        RadioButton radio =
                new RadioButton(text);

        radio.setToggleGroup(toggleGroup);
        radio.setUserData(mode);

        radio.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;"
        );

        if (mode == GameMode.AI) {
            radio.setSelected(true);
        }

        getChildren().add(radio);
    }
}