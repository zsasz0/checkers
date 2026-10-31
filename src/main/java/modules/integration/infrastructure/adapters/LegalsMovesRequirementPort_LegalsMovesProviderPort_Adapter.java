package modules.integration.infrastructure.adapters;

import java.util.List;
import modules.game.domain.Game;
import modules.game.domain.Move;
import modules.game.in_ports.LegalsMovesProviderPort;
import modules.ui.out_ports.LegalsMovesRequirementPort;

public class LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter implements LegalsMovesRequirementPort {

    private final LegalsMovesProviderPort legalsProvider;

    public LegalsMovesRequirementPort_LegalsMovesProviderPort_Adapter(LegalsMovesProviderPort legalsProvider) {
        this.legalsProvider = legalsProvider;
    }

    @Override
    public List<Move> legalMoves(Game game) {
        return legalsProvider.legalMoves(game);
    }
}