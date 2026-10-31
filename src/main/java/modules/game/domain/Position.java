package modules.game.domain;

import java.util.Objects;

public final class Position {
    private final int row;
    private final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    public Position offset(int dRow, int dColumn) {
        return new Position(row + dRow, column + dColumn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}
