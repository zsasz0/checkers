package modules.game.domain;

public enum Player {
    BLACK,
    WHITE;

    public Player opponent() {
        return this == BLACK ? WHITE : BLACK;
    }

    public int forwardDirection() {
        return this == BLACK ? 1 : -1;
    }
}
