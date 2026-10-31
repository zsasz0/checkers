package modules.integration.infrastructure.adapters;

import modules.game.domain.Move;
import modules.game.in_ports.MakeMovePort;
import modules.ui.out_ports.HumanMoveSubmissionPort;

public class HumanMoveAdapter
        implements HumanMoveSubmissionPort {

    private final MakeMovePort makeMovePort;

    public HumanMoveAdapter(MakeMovePort makeMovePort) {
        this.makeMovePort = makeMovePort;
    }

    @Override
    public void submitMove(Move move) {
        makeMovePort.execute(move);
    }


}