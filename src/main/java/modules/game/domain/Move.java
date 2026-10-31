package modules.game.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Move {

    private final Position from;
    private final Position to;
    private final List<Position> path;
    private final List<Position> captured;

    /**
     * Creates a simple non-capturing move.
     */
    public Move(Position from, Position to) {
        this(
                from,
                to,
                Collections.singletonList(to),
                Collections.emptyList()
        );
    }

    /**
     * Creates a move with its complete path and captured pieces.
     */
    public Move(
            Position from,
            Position to,
            List<Position> path,
            List<Position> captured) {

        this.from = from;
        this.to = to;

        this.path =
                Collections.unmodifiableList(
                        new ArrayList<>(path)
                );

        this.captured =
                Collections.unmodifiableList(
                        new ArrayList<>(captured)
                );
    }

    /**
     * Returns the starting position.
     */
    public Position from() {
        return from;
    }

    /**
     * Returns the final position.
     */
    public Position to() {
        return to;
    }

    /**
     * Returns all positions visited by the move.
     */
    public List<Position> path() {
        return path;
    }

    /**
     * Returns all captured piece positions.
     */
    public List<Position> captured() {
        return captured;
    }

    /**
     * Returns true when this move captures at least one piece.
     */
    public boolean isCapture() {
        return !captured.isEmpty();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Move)) {
            return false;
        }

        Move move = (Move) o;

        return Objects.equals(from, move.from)
                && Objects.equals(to, move.to)
                && Objects.equals(path, move.path)
                && Objects.equals(captured, move.captured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                from,
                to,
                path,
                captured
        );
    }

    @Override
    public String toString() {

        return from
                + " -> "
                + to
                + (
                    captured.isEmpty()
                        ? ""
                        : " (captures " + captured + ")"
                );
    }
}