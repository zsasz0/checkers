package modules.integration.infrastructure.adapters;

import modules.ai.out_ports.GameRequirementPort;
import modules.game.domain.Game;
import modules.game.in_ports.GameProviderPort;

public final class AiGameRequirementAdapter implements GameRequirementPort {

    private final GameProviderPort gameProvider;

    public AiGameRequirementAdapter(GameProviderPort gameProvider) {
        this.gameProvider = gameProvider;
    }

    @Override
    public Game getGame() {
        return gameProvider.getGame();
    }
}
