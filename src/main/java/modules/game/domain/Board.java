package modules.game.domain;

import java.util.ArrayList;
import java.util.List;

public final class Board {
    public static final int SIZE = 8;

    private final Piece[][] cells = new Piece[SIZE][SIZE];

    public static Board standardSetup() {
        Board board = new Board();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!isPlayableSquare(row, col)) {
                    continue;
                }
                if (row < 3) {
                    board.cells[row][col] = new Piece(Player.BLACK, PieceType.MAN);
                } else if (row > 4) {
                    board.cells[row][col] = new Piece(Player.WHITE, PieceType.MAN);
                }
            }
        }
        return board;
    }

    public static boolean isPlayableSquare(int row, int col) {
        return (row + col) % 2 == 1;
    }

    public Piece pieceAt(Position position) {
        return (cells[position.row()][position.column()]);
    }

    public void setPiece(Position position, Piece piece) {
        cells[position.row()][position.column()] = piece;
    }

    public void removePiece(Position position) {
        cells[position.row()][position.column()] = null;
    }

    public void clear() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = null;
            }
        }
    }

    public boolean isEmpty(Position position) {
        return cells[position.row()][position.column()] == null;
    }

    public boolean inBounds(Position position) {
        return position.row() >= 0 && position.row() < SIZE
                && position.column() >= 0 && position.column() < SIZE;
    }

    public List<Position> piecePositions(Player player) {
        List<Position> result = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col];
                if (piece != null && piece.player() == player) {
                    result.add(new Position(row, col));
                }
            }
        }
        return result;
    }

    public int countPieces(Player player) {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col];
                if (piece != null && piece.player() == player) {
                    count++;
                }
            }
        }
        return count;
    }

    public Board copy() {
        Board copy = new Board();
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(cells[row], 0, copy.cells[row], 0, SIZE);
        }
        return copy;
    }

    public void place(Board other) {
        for (int row = 0; row < SIZE; row++) {
            System.arraycopy(other.cells[row], 0, cells[row], 0, SIZE);
        }
    }
    
}
