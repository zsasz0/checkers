package main.config;

import modules.ai.application.use_cases.AiPlayer;
import modules.ai.application.use_cases.modules.BestMovePortAdapter;
import modules.ai.out_ports.GameRequirementPort;
import modules.ai.out_ports.MoveSubmissionPort;

import modules.game.application.use_cases.GameController;
import modules.game.application.use_cases.rules.CheckersRules;
import modules.game.application.use_cases.rules.utils.CaptureStrategy;
import modules.game.application.use_cases.rules.utils.SpanishTunisianCaptureStrategy;
import modules.game.domain.Game;
import modules.game.domain.Player;
import modules.game.in_ports.MakeMovePort;

import modules.integration.infrastructure.adapters.AiGameRequirementAdapter;
import modules.integration.infrastructure.adapters.AiMoveSubmissionAdapter;
import modules.integration.infrastructure.adapters.GameView_GameViewPort_Adapter;
import modules.integration.infrastructure.adapters.LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter;
import modules.integration.infrastructure.adapters.HumanMoveAdapter;

import modules.ui.application.use_cases.BoardView;
import modules.ui.application.use_cases.SideBar;
import modules.ui.out_ports.HumanMoveSubmissionPort;

public class GameConfig {

    private final ApplicationContext context;

    public GameConfig(ApplicationContext context) {
        this.context = context;
        registerCommonBeans();
    }

    private void registerCommonBeans() {

        CaptureStrategy captureStrategy =
                new SpanishTunisianCaptureStrategy();

        context.registerSingleton(
                CaptureStrategy.class,
                captureStrategy
        );

        CheckersRules checkersRules =
                new CheckersRules(captureStrategy);

        context.registerSingleton(
                CheckersRules.class,
                checkersRules
        );
    }

    /*
     * =========================================================
     * COMMON
     * =========================================================
     */

    private GameController createGameController(Game game) {

        CheckersRules checkersRules =
                context.getBean(CheckersRules.class);

        return new GameController(
                game,
                checkersRules
        );
    }

    private void wireBoard(
            BoardView boardView,
            SideBar sideBar,
            GameController controller
    ) {

        GameView_GameViewPort_Adapter gameViewPort =
                new GameView_GameViewPort_Adapter(
                        controller
                );

        LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter legalMovesPort =
                new LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter(
                        controller
                );

        boardView.setGameRequirementPort(
                gameViewPort
        );

        boardView.setLegalMovesRequirementPort(
                legalMovesPort
        );

        sideBar.setGameRequirementPort(
                gameViewPort
        );

        boardView.startGame();
        sideBar.startTimer();

        context.registerSingleton(
                GameController.class,
                controller
        );
    }

    /*
     * =========================================================
     * HUMAN -> GAME
     * =========================================================
     */

    private HumanMoveSubmissionPort createHumanMovePort(
            MakeMovePort gameMakeMovePort
    ) {

        return new HumanMoveAdapter(
                gameMakeMovePort
        );
    }

    /*
     * =========================================================
     * AI -> GAME
     * =========================================================
     */

    private MoveSubmissionPort createAiMovePort(
            MakeMovePort gameMakeMovePort
    ) {

        return new AiMoveSubmissionAdapter(
                gameMakeMovePort
        );
    }

    /*
     * =========================================================
     * HUMAN VS AI
     * =========================================================
     */

    public BoardView setupAIGame(
            BoardView boardView,
            SideBar sideBar
    ) {

        Game game =
                new Game(Player.WHITE);

        boardView.setHumanPlayer(Player.WHITE);
        boardView.setEnabled(true);

        GameController controller =
                createGameController(game);

        /*
         * Game inbound port.
         */
        MakeMovePort makeMovePort = controller;

        /*
         * Human -> Game
         */
        HumanMoveSubmissionPort humanMovePort =
                createHumanMovePort(
                        makeMovePort
                );

        boardView.setHumanMoveSubmissionPort(
                humanMovePort
        );

        /*
         * AI -> Game
         */
        MoveSubmissionPort aiMovePort =
                createAiMovePort(
                        makeMovePort
                );

        /*
         * AI -> Game queries.
         */
        GameRequirementPort aiGameRequirement =
                new AiGameRequirementAdapter(
                        controller
                );

        BestMovePortAdapter ai =
                new BestMovePortAdapter(
                        aiGameRequirement,
                        aiMovePort
                );

        /*
         * UI -> Game queries.
         */
        GameView_GameViewPort_Adapter gameView =
                new GameView_GameViewPort_Adapter(
                        controller
                );

        LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter legalMoves =
                new LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter(
                        controller
                );

        boardView.setGameRequirementPort(
                gameView
        );

        boardView.setLegalMovesRequirementPort(
                legalMoves
        );

        sideBar.setGameRequirementPort(
                gameView
        );

        boardView.startGame();
        sideBar.startTimer();

        context.registerSingleton(
                GameController.class,
                controller
        );

        /*
         * AI owns BLACK.
         *
         * The game does NOT ask the AI for a move.
         * The AI watches the game through its own outbound
         * context port, calculates a move, and submits it
         * through its outbound move port.
         */
        AiPlayer aiPlayer =
                new AiPlayer(
                        aiGameRequirement,
                        ai,
                        Player.BLACK,
                        12
                );

        aiPlayer.startGame();

        return boardView;
    }

    /*
     * =========================================================
     * HUMAN VS HUMAN
     * =========================================================
     */

    public BoardView setupOfflineGame(
            BoardView boardView,
            SideBar sideBar
    ) {

        Game game =
                new Game(Player.WHITE);

        boardView.setHumanPlayer(null);
        boardView.setEnabled(true);

        GameController controller =
                createGameController(game);

        /*
         * Game inbound port.
         */
        MakeMovePort makeMovePort = controller;

        /*
         * Human -> Game
         *
         * Both WHITE and BLACK use the same submission
         * port because the Game already knows whose turn it is.
         */
        HumanMoveSubmissionPort humanMovePort =
                createHumanMovePort(
                        makeMovePort
                );

        boardView.setHumanMoveSubmissionPort(
                humanMovePort
        );

        wireBoard(
                boardView,
                sideBar,
                controller
        );

        /*
         * No controller.startGame().
         *
         * The game waits for the human to submit a move.
         */
        return boardView;
    }
}