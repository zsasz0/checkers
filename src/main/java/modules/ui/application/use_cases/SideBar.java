package modules.ui.application.use_cases;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import modules.game.domain.Board;
import modules.game.domain.Game;
import modules.game.domain.Player;
import modules.game.domain.Position;
import modules.ui.out_ports.GameRequirementPort;

public class SideBar extends VBox {

    private final Label statusLabel =
            new Label("Game in progress");

    private final Label turnLabel =
            new Label("Turn: WHITE");

    private final Label whiteCapturedLabel =
            new Label("White captured: 0");

    private final Label blackCapturedLabel =
            new Label("Black captured: 0");

    private final Label whiteTimeLabel =
            new Label("White time: 0s");

    private final Label blackTimeLabel =
            new Label("Black time: 0s");

    private final Button backButton =
            new Button("Back to Menu");

    private GameRequirementPort gameRequirementPort;

    private int whiteSeconds = 0;
    private int blackSeconds = 0;

    private final Timeline timer;

    public SideBar(Runnable onBackToMenu) {

        setPadding(new Insets(15));
        setSpacing(15);
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(180);

        setStyle(
                "-fx-background-color: #444;"
        );

        statusLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
        );

        turnLabel.setStyle(
                "-fx-text-fill: yellow;" +
                "-fx-font-size: 14px;"
        );

        whiteCapturedLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
        );

        blackCapturedLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
        );

        whiteTimeLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
        );

        blackTimeLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
        );

        backButton.setOnAction(
                e -> onBackToMenu.run()
        );

        getChildren().addAll(
                statusLabel,
                turnLabel,
                whiteCapturedLabel,
                blackCapturedLabel,
                whiteTimeLabel,
                blackTimeLabel,
                backButton
        );

        /*
         * Refresh every second.
         *
         * This updates:
         * - timer
         * - turn
         * - captured pieces
         * - game status
         */
        timer = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        e -> update()
                )
        );

        timer.setCycleCount(
                Timeline.INDEFINITE
        );
    }

    public void setGameRequirementPort(
            GameRequirementPort gameRequirementPort
    ) {

        this.gameRequirementPort =
                gameRequirementPort;

        refreshGameInfo();
    }

    public void startTimer() {

        whiteSeconds = 0;
        blackSeconds = 0;

        updateTimeLabels();

        timer.play();
    }

    public void stopTimer() {
        timer.stop();
    }

    /*
     * =========================================================
     * PERIODIC UPDATE
     * =========================================================
     */

    private void update() {

        if (gameRequirementPort == null) {
            return;
        }

        Game game =
                gameRequirementPort.getGame();

        if (game == null) {
            return;
        }

        /*
         * Count time for the player whose turn
         * is currently active.
         */
        Player current =
                game.currentPlayer();

        if (current == Player.WHITE) {
            whiteSeconds++;
        } else if (current == Player.BLACK) {
            blackSeconds++;
        }

        refreshGameInfo();
    }

    /*
     * =========================================================
     * GAME INFORMATION
     * =========================================================
     */

    private void refreshGameInfo() {

        if (gameRequirementPort == null) {
            return;
        }

        Game game =
                gameRequirementPort.getGame();

        if (game == null) {
            return;
        }

        Board board =
                game.board();

        /*
         * Standard checkers starts with
         * 12 pieces for each player.
         */
        int totalPieces = 12;

        int whitePieces =
                countPieces(
                        board,
                        Player.WHITE
                );

        int blackPieces =
                countPieces(
                        board,
                        Player.BLACK
                );

        /*
         * If Black has 10 pieces left,
         * White has captured 2.
         */
        int whiteCaptured =
                totalPieces - blackPieces;

        /*
         * If White has 9 pieces left,
         * Black has captured 3.
         */
        int blackCaptured =
                totalPieces - whitePieces;

        whiteCapturedLabel.setText(
                "White captured: "
                        + whiteCaptured
        );

        blackCapturedLabel.setText(
                "Black captured: "
                        + blackCaptured
        );

        turnLabel.setText(
                "Turn: "
                        + game.currentPlayer()
        );

        switch (game.status()) {

            case IN_PROGRESS ->
                    statusLabel.setText(
                            "Game in progress"
                    );

            case WHITE_WON ->
                    statusLabel.setText(
                            "White won"
                    );

            case BLACK_WON ->
                    statusLabel.setText(
                            "Black won"
                    );

            case DRAW ->
                    statusLabel.setText(
                            "Draw"
                    );
        }

        updateTimeLabels();
    }

    /*
     * =========================================================
     * COUNT PIECES
     * =========================================================
     */

    private int countPieces(
            Board board,
            Player player
    ) {

        int count = 0;

        for (int row = 0;
             row < Board.SIZE;
             row++) {

            for (int col = 0;
                 col < Board.SIZE;
                 col++) {

                Position position =
                        new Position(
                                row,
                                col
                        );

                var piece =
                        board.pieceAt(position);

                if (piece != null
                        && piece.player() == player) {

                    count++;
                }
            }
        }

        return count;
    }

    /*
     * =========================================================
     * TIME LABELS
     * =========================================================
     */

    private void updateTimeLabels() {

        whiteTimeLabel.setText(
                "White time: "
                        + whiteSeconds
                        + "s"
        );

        blackTimeLabel.setText(
                "Black time: "
                        + blackSeconds
                        + "s"
        );
    }
}