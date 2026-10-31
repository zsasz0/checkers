package modules.integration.infrastructure.adapters;

import modules.ai.out_ports.MoveSubmissionPort;
import modules.game.domain.Move;
import modules.game.in_ports.MakeMovePort;

public final class AiMoveSubmissionAdapter
        implements MoveSubmissionPort {

    private final MakeMovePort makeMovePort;

    public AiMoveSubmissionAdapter(
            MakeMovePort makeMovePort) {

        this.makeMovePort = makeMovePort;
    }

    @Override
    public void submitMove(Move move) {

        if (move == null) {
            return;
        }

        makeMovePort.execute(move);
    }
}