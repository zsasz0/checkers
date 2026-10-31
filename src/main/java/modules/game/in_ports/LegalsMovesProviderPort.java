package modules.game.in_ports;

import java.util.List;

import modules.game.domain.Game;
import modules.game.domain.Move;

public interface LegalsMovesProviderPort {

    List<Move> legalMoves(Game game);



}