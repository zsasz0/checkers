package modules.game.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Game {
    private Board board;
    private Player currentPlayer;
    private final Player startingPlayer;
    private GameStatus status;
    private final List<Move> history = new ArrayList<>();
	// Number of consecutive moves without a capture, used to enforce
	// the draw rule when the limit is reached.
    private int movesWithoutCapture;

    public Game() {
        this(Player.BLACK);
    }

    public Game(Player startingPlayer) {
        this(Board.standardSetup(), startingPlayer);
    }

    public Game(Board board, Player startingPlayer) {
        this.board = board;
        this.currentPlayer = startingPlayer;
        this.status = GameStatus.IN_PROGRESS;
        this.startingPlayer = startingPlayer;
    }

    public Board board() {
        return board;
    }

    public Player currentPlayer() {
        return currentPlayer;
    }

    public GameStatus status() {
        return status;
    }

    public List<Move> history() {
        return Collections.unmodifiableList(history);
    }

    public int movesWithoutCapture() {
        return movesWithoutCapture;
    }

    public void switchPlayer() {
        currentPlayer = currentPlayer.opponent();
    }

    public void recordMove(Move move) {
        history.add(move);
        if (move.isCapture()) {
            movesWithoutCapture = 0;
        } else {
            movesWithoutCapture++;
        }
    }

    public void finish(GameStatus newStatus) {
        this.status = newStatus;
    }

    public void reset() {
        board.place(Board.standardSetup());
        currentPlayer = startingPlayer;
        status = GameStatus.IN_PROGRESS;
        history.clear();
        movesWithoutCapture = 0;
    }
    
    public Game copy() {
        Game copy = new Game(this.currentPlayer);  // constructor may need adjustments
        copy.board = this.board.copy();            // uses Board.copy()
        copy.status = this.status;                 // copy other fields if needed
        // If you have other fields (like move history), copy them too.
        return copy;
    }
}
