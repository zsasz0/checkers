package modules.ai.application.use_cases.modules;

import modules.ai.out_ports.GameRequirementPort;
import modules.ai.out_ports.MoveSubmissionPort;
import modules.game.domain.Game;
import modules.game.domain.GameStatus;
import modules.game.domain.Move;
import modules.game.domain.Player;

public final class BestMovePortAdapter {

    private final Search search;
    private final GameRequirementPort gameRequirementPort;
    private final MoveSubmissionPort moveSubmissionPort;

    public BestMovePortAdapter(
            GameRequirementPort gameRequirementPort,
            MoveSubmissionPort moveSubmissionPort) {

        this.gameRequirementPort = gameRequirementPort;
        this.moveSubmissionPort = moveSubmissionPort;

        Evaluation evaluation =
                new Evaluation();

        MoveOrdering moveOrdering =
                new MoveOrdering();

        BoardSimulator boardSimulator =
                new BoardSimulator();

        this.search = new Search(
                evaluation,
                moveOrdering,
                boardSimulator
        );
    }

    public void execute(
            Player player,
            int depth) {

        Game game = gameRequirementPort.getGame();

        if (game == null
                || game.status() != GameStatus.IN_PROGRESS
                || game.currentPlayer() != player) {

            return;
        }

        Game searchGame = game.copy();

        Move move = search.getBestMove(
                searchGame,
                player,
                depth
        );

        moveSubmissionPort.submitMove(move);
    }

    public void setMaxSearchTimeMs(
            long milliseconds) {

        search.setMaxSearchTimeMs(milliseconds);
    }
}