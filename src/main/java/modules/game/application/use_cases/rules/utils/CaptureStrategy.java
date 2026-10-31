package modules.game.application.use_cases.rules.utils;



import java.util.List;

import modules.game.domain.Board;
import modules.game.domain.Move;
import modules.game.domain.Player;




public interface CaptureStrategy {

    List<Move> findCaptures(
            Board board,
            Player player);
}

