package modules.ui.out_ports;

import java.util.List;

import modules.game.domain.Game;
import modules.game.domain.Move;

public interface LegalsMovesRequirementPort {

    List<Move> legalMoves(Game game);

}