package modules.game.in_ports;

import modules.game.domain.Game;

public interface GameProviderPort {
    Game getGame();
}