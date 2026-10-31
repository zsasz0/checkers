package modules.ui.application.use_cases;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import modules.game.domain.Board;
import modules.game.domain.Game;
import modules.game.domain.Move;
import modules.game.domain.Piece;
import modules.game.domain.Player;
import modules.game.domain.Position;

import modules.ui.out_ports.GameRequirementPort;
import modules.ui.out_ports.HumanMoveSubmissionPort;
import modules.ui.out_ports.LegalsMovesRequirementPort;

public class BoardView extends GridPane {

    private boolean initialized = false;

    /*
     * UI outbound ports.
     */
    private GameRequirementPort gameRequirementPort;
    private LegalsMovesRequirementPort legalMovesRequirementPort;
    private HumanMoveSubmissionPort humanMoveSubmissionPort;

    /*
     * Current UI selection.
     */
    private Position selectedPosition;
    private List<Move> selectedMoves = List.of();

    /*
     * UI state.
     */
    private boolean enabled = true;
    private Player humanPlayer = Player.WHITE;

    /*
     * Used to detect changes in the game.
     */
    private String lastGameState;

    /*
     * Periodically checks the game state.
     */
    private final Timeline gameWatcher;

    public BoardView() {

        setAlignment(Pos.CENTER);

        setMinSize(0, 0);

        setMaxSize(
                Double.MAX_VALUE,
                Double.MAX_VALUE
        );

        widthProperty().addListener(
                (obs, oldValue, newValue) -> render()
        );

        heightProperty().addListener(
                (obs, oldValue, newValue) -> render()
        );

        /*
         * Check the game every 100 ms.
         *
         * This allows the UI to notice when
         * the AI changes the game state.
         */
        gameWatcher = new Timeline(
                new KeyFrame(
                        Duration.millis(100),
                        event -> checkGameState()
                )
        );

        gameWatcher.setCycleCount(
                Timeline.INDEFINITE
        );

        gameWatcher.play();

        render();
    }

    /*
     * =========================================================
     * DYNAMIC SIZE
     * =========================================================
     */

    private double getTileSize() {

        double width = getWidth();
        double height = getHeight();

        if (width <= 0 || height <= 0) {
            return 1;
        }

        return Math.min(
                width / Board.SIZE,
                height / Board.SIZE
        );
    }

    /*
     * =========================================================
     * PORTS
     * =========================================================
     */

    public void setGameRequirementPort(
            GameRequirementPort gameRequirementPort
    ) {

        this.gameRequirementPort =
                gameRequirementPort;

        /*
         * Force the next check to detect the game.
         */
        lastGameState = null;

        render();
    }

    public void setLegalMovesRequirementPort(
            LegalsMovesRequirementPort legalMovesRequirementPort
    ) {

        this.legalMovesRequirementPort =
                legalMovesRequirementPort;
    }

    public void setHumanMoveSubmissionPort(
            HumanMoveSubmissionPort humanMoveSubmissionPort
    ) {

        this.humanMoveSubmissionPort =
                humanMoveSubmissionPort;
    }

    public void setHumanPlayer(
            Player humanPlayer
    ) {

        this.humanPlayer = humanPlayer;
    }

    /*
     * =========================================================
     * UI STATE
     * =========================================================
     */

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;

        setMouseTransparent(!enabled);
        setDisable(!enabled);
    }

    public void updateBoard(Game game) {

        lastGameState =
                createGameStateSignature(game);

        render();
    }

    public void clearSelection() {

        selectedPosition = null;
        selectedMoves = List.of();

        render();
    }

    /*
     * =========================================================
     * GAME WATCHER
     * =========================================================
     */

    private void checkGameState() {

        if (!initialized
                || gameRequirementPort == null) {

            return;
        }

        Game game =
                gameRequirementPort.getGame();

        if (game == null) {
            return;
        }

        String currentState =
                createGameStateSignature(game);

        /*
         * Only redraw when the actual game state changed.
         */
        if (!currentState.equals(lastGameState)) {

            lastGameState = currentState;

            /*
             * A new turn started.
             * Any old selection belongs to the previous turn.
             */
            selectedPosition = null;
            selectedMoves = List.of();

            render();
        }
    }

    /*
     * Creates a simple representation of the current
     * game state so the UI can detect changes.
     */
    private String createGameStateSignature(
            Game game
    ) {

        StringBuilder state =
                new StringBuilder();

        state.append(game.currentPlayer())
                .append('|')
                .append(game.status())
                .append('|');

        Board board = game.board();

        for (int row = 0;
             row < Board.SIZE;
             row++) {

            for (int col = 0;
                 col < Board.SIZE;
                 col++) {

                Position position =
                        new Position(row, col);

                Piece piece =
                        board.pieceAt(position);

                if (piece == null) {

                    state.append('.');

                } else {

                    state.append(
                            piece.player() == Player.WHITE
                                    ? 'W'
                                    : 'B'
                    );

                    state.append(
                            piece.isKing()
                                    ? 'K'
                                    : 'P'
                    );
                }
            }
        }

        return state.toString();
    }

    /*
     * =========================================================
     * START GAME
     * =========================================================
     */

    public void startGame() {

        initialized = true;

        setEnabled(true);

        lastGameState = null;

        render();
    }

    /*
     * =========================================================
     * RENDER
     * =========================================================
     */

    public void render() {

        if (!Platform.isFxApplicationThread()) {

            Platform.runLater(
                    this::render
            );

            return;
        }

        getChildren().clear();

        double tileSize =
                getTileSize();

        /*
         * Before a game starts, display
         * an empty board.
         */
        if (!initialized
                || gameRequirementPort == null) {

            renderEmptyBoard(tileSize);

            return;
        }

        Game game =
                gameRequirementPort.getGame();

        if (game == null) {

            renderEmptyBoard(tileSize);

            return;
        }

        Board board =
                game.board();

        /*
         * Board
         */
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

                StackPane tile =
                        createTile(
                                row,
                                col,
                                position,
                                tileSize
                        );

                Piece piece =
                        board.pieceAt(position);

                if (piece != null) {

                    tile.getChildren().add(
                            createPiece(
                                    piece,
                                    tileSize
                            )
                    );
                }

                add(
                        tile,
                        col,
                        row
                );
            }
        }
    }

    /*
     * =========================================================
     * EMPTY BOARD
     * =========================================================
     */

    private void renderEmptyBoard(
            double tileSize
    ) {

        for (int row = 0;
             row < Board.SIZE;
             row++) {

            for (int col = 0;
                 col < Board.SIZE;
                 col++) {

                StackPane tile =
                        new StackPane();

                tile.setPrefSize(
                        tileSize,
                        tileSize
                );

                tile.setMinSize(
                        tileSize,
                        tileSize
                );

                tile.setMaxSize(
                        tileSize,
                        tileSize
                );

                applyBoardColor(
                        tile,
                        row,
                        col
                );

                add(
                        tile,
                        col,
                        row
                );
            }
        }
    }

    /*
     * =========================================================
     * TILE
     * =========================================================
     */

    private StackPane createTile(
            int row,
            int col,
            Position position,
            double tileSize
    ) {

        StackPane tile =
                new StackPane();

        tile.setPrefSize(
                tileSize,
                tileSize
        );

        tile.setMinSize(
                tileSize,
                tileSize
        );

        tile.setMaxSize(
                tileSize,
                tileSize
        );

        applyBoardColor(
                tile,
                row,
                col
        );

        if (position.equals(selectedPosition)) {

            tile.setStyle(
                    tile.getStyle()
                            + "-fx-border-color: yellow;"
                            + "-fx-border-width: 4;"
            );
        }

        if (isLegalDestination(position)) {

            tile.setStyle(
                    tile.getStyle()
                            + "-fx-border-color: limegreen;"
                            + "-fx-border-width: 4;"
            );
        }

        tile.setOnMouseClicked(
                event -> handleClick(position)
        );

        return tile;
    }

    private void applyBoardColor(
            StackPane tile,
            int row,
            int col
    ) {

        if ((row + col) % 2 == 0) {

            tile.setStyle(
                    "-fx-background-color: #F0D9B5;"
            );

        } else {

            tile.setStyle(
                    "-fx-background-color: #B58863;"
            );
        }
    }

    /*
     * =========================================================
     * LEGAL MOVES
     * =========================================================
     */

    private boolean isLegalDestination(
            Position position
    ) {

        return selectedMoves
                .stream()
                .anyMatch(
                        move ->
                                move.to()
                                        .equals(position)
                );
    }

    /*
     * =========================================================
     * HUMAN CLICK
     * =========================================================
     */

    private void handleClick(
            Position position
    ) {

        if (!enabled
                || gameRequirementPort == null
                || legalMovesRequirementPort == null
                || humanMoveSubmissionPort == null) {

            return;
        }

        Game game =
                gameRequirementPort.getGame();

        if (game == null) {
            return;
        }

        /*
         * Don't allow the human to play
         * when it is not their turn.
         */
        if (humanPlayer != null
                && game.currentPlayer() != humanPlayer) {

            return;
        }

        Board board =
                game.board();

        /*
         * =====================================================
         * SELECT PIECE
         * =====================================================
         */

        if (selectedPosition == null) {

            Piece piece =
                    board.pieceAt(position);

            if (piece == null) {
                return;
            }

            if (piece.player()
                    != game.currentPlayer()) {

                System.out.println(
                        "[UI] Cannot select opponent piece."
                );

                return;
            }

            List<Move> legalMoves =
                    legalMovesRequirementPort
                            .legalMoves(game);

            selectedMoves =
                    legalMoves
                            .stream()
                            .filter(
                                    move ->
                                            move.from()
                                                    .equals(position)
                            )
                            .toList();

            if (selectedMoves.isEmpty()) {

                System.out.println(
                        "[UI] No legal moves for this piece."
                );

                return;
            }

            selectedPosition = position;

            render();

            return;
        }

        /*
         * =====================================================
         * SELECT DESTINATION
         * =====================================================
         */

        Move move =
                selectedMoves
                        .stream()
                        .filter(
                                m ->
                                        m.to()
                                                .equals(position)
                        )
                        .findFirst()
                        .orElse(null);

        /*
         * Clicked somewhere that isn't
         * a legal destination.
         */
        if (move == null) {

            Piece piece =
                    board.pieceAt(position);

            /*
             * Allow selecting another piece
             * belonging to the current player.
             */
            if (piece != null
                    && piece.player()
                    == game.currentPlayer()) {

                selectedPosition = null;
                selectedMoves = List.of();

                handleClick(position);
            }

            return;
        }

        /*
         * Clear selection before submitting.
         */
        selectedPosition = null;
        selectedMoves = List.of();

        System.out.println(
                "[UI] Human move selected: "
                        + move
        );

        /*
         * UI OUTBOUND PORT
         *
         * The UI gives the move to the integration
         * adapter, which eventually calls the
         * Game hexagon's MakeMovePort.
         */
        humanMoveSubmissionPort
                .submitMove(move);

        render();
    }

    /*
     * =========================================================
     * PIECES
     * =========================================================
     */

    private Node createPiece(
            Piece piece,
            double tileSize
    ) {

        double radius =
                tileSize * 0.35;

        Circle circle =
                new Circle(radius);

        circle.setStyle(
                piece.player() == Player.BLACK
                        ? "-fx-fill: black;"
                        : "-fx-fill: white;"
        );

        if (!piece.isKing()) {
            return circle;
        }

        Label king =
                new Label("K");

        king.setStyle(
                "-fx-font-size: "
                        + (tileSize * 0.25)
                        + "px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: gold;"
        );

        return new StackPane(
                circle,
                king
        );
    }
}