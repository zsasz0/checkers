package modules.game.domain;

import java.util.Objects;

public final class Piece {
    private final Player player;
    private final PieceType type;

    public Piece(Player player, PieceType type) {
        this.player = player;
        this.type = type;
    }

    public Player player() {
        return player;
    }

    public PieceType type() {
        return type;
    }

    public boolean isKing() {
        return type == PieceType.KING;
    }

    public Piece promoted() {
        return new Piece(player, PieceType.KING);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return player == piece.player && type == piece.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, type);
    }

    @Override
    public String toString() {
        return player + "-" + type;
    }
}
