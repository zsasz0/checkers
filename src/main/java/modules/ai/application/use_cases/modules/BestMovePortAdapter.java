package modules.ai.application.use_cases.modules;

import modules.ai.out_ports.MoveSubmissionPort;
import modules.game.domain.Game;
import modules.game.domain.Move;
import modules.game.domain.Player;

public final class BestMovePortAdapter {

    private final Search search;
    private final MoveSubmissionPort moveSubmissionPort;

    public BestMovePortAdapter(
            MoveSubmissionPort moveSubmissionPort) {

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
            Game game,
            Player player,
            int depth) {

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