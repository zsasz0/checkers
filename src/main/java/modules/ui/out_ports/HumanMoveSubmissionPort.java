package modules.ui.out_ports;

import modules.game.domain.Move;

public interface HumanMoveSubmissionPort {

	void submitMove(Move move);
}