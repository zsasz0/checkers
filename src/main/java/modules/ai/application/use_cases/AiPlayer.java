package modules.ai.application.use_cases;

import modules.ai.application.use_cases.modules.BestMovePortAdapter;
import modules.ai.out_ports.GameRequirementPort;
import modules.game.domain.Game;
import modules.game.domain.GameStatus;
import modules.game.domain.Player;

/*
 * Self-driving AI player.
 *
 * The AI owns BLACK and is never told to move by the outside world.
 * Like the UI's BoardView, it watches the game through its own
 * outbound port and acts when it detects that it is its turn.
 */
public final class AiPlayer {

    private static final long POLL_INTERVAL_MS = 20;

    private final GameRequirementPort gameRequirementPort;
    private final BestMovePortAdapter bestMovePortAdapter;
    private final Player aiPlayer;
    private final int depth;

    private volatile boolean running;

    public AiPlayer(
            GameRequirementPort gameRequirementPort,
            BestMovePortAdapter bestMovePortAdapter,
            Player aiPlayer,
            int depth) {

        this.gameRequirementPort = gameRequirementPort;
        this.bestMovePortAdapter = bestMovePortAdapter;
        this.aiPlayer = aiPlayer;
        this.depth = depth;
    }

    public void startGame() {

        if (running) {
            return;
        }

        running = true;

        Thread aiThread = new Thread(this::watchGame);

        aiThread.setDaemon(true);
        aiThread.setName("Checkers-AI");

        aiThread.start();
    }

    public void stopGame() {

        running = false;
    }

    /*
     * Polls the game through the AI outbound port.
     *
     * The AI does not receive a Game instance from the
     * composition root; it asks for the context itself.
     */
    private void watchGame() {

        while (running) {

            Game game = gameRequirementPort.getGame();

            if (game == null) {

                sleep();

                continue;
            }

            if (game.status() != GameStatus.IN_PROGRESS) {

                running = false;

                return;
            }

            if (game.currentPlayer() == aiPlayer) {

                bestMovePortAdapter.execute(
                        aiPlayer,
                        depth
                );
            }

            sleep();
        }
    }

    private void sleep() {

        try {

            Thread.sleep(POLL_INTERVAL_MS);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            running = false;
        }
    }
}
