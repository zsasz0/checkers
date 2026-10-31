package modules.game.in_ports;

import modules.game.domain.Move;

public interface MakeMovePort {

    void execute(Move move);
}