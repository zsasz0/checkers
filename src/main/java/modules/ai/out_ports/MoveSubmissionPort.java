package modules.ai.out_ports;

import modules.game.domain.Move;

public interface MoveSubmissionPort {

    void submitMove(Move move);
}