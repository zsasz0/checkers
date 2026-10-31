package modules.game.application.use_cases;

import java.util.List;

import modules.game.application.use_cases.rules.CheckersRules;
import modules.game.domain.Game;
import modules.game.domain.GameStatus;
import modules.game.domain.Move;
import modules.game.in_ports.GameProviderPort;
import modules.game.in_ports.LegalsMovesProviderPort;
import modules.game.in_ports.MakeMovePort;

public class GameController
        implements GameProviderPort,
                   LegalsMovesProviderPort,
                   MakeMovePort {

    private final Game game;
    private final CheckersRules rules;

    public GameController(
            Game game,
            CheckersRules rules) {

        this.game = game;
        this.rules = rules;
    }

    @Override
    public void execute(Move move) {

        if (game.status() != GameStatus.IN_PROGRESS) {
            return;
        }

        if (move == null) {
            return;
        }

        rules.applyMove(game, move);
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public List<Move> legalMoves(Game game) {
        return rules.legalMoves(
                game.board(),
                game.currentPlayer()
        );
    }
}