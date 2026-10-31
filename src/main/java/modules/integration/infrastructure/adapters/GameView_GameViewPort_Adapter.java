package modules.integration.infrastructure.adapters;

import modules.game.domain.Game;
import modules.game.in_ports.GameProviderPort;
import modules.ui.out_ports.GameRequirementPort;

public class GameView_GameViewPort_Adapter implements GameRequirementPort {

    private final GameProviderPort gameProvider;

    public GameView_GameViewPort_Adapter(GameProviderPort gameProvider) {
        this.gameProvider = gameProvider;
    }

    @Override
    public Game getGame() {
        return gameProvider.getGame();
    }
}