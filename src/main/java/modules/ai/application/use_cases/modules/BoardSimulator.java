package modules.ai.application.use_cases.modules;


import modules.game.domain.*;

public final class BoardSimulator {

    public void makeMove(
            Board board,
            Move move) {

        Piece piece =
                board.pieceAt(
                        move.from()
                );

        if (piece == null) {
            return;
        }

        // Remove piece from original position
        board.setPiece(
                move.from(),
                null
        );

        // Remove captured pieces
        for (Position captured :
                move.captured()) {

            board.removePiece(
                    captured
            );
        }

        // Put piece on destination
        board.setPiece(
                move.to(),
                piece
        );

        // Promote man to king
        if (!piece.isKing() &&
                reachedLastRow(
                        move.to(),
                        piece.player())) {

            board.setPiece(
                    move.to(),
                    piece.promoted()
            );
        }
    }

    public Piece[][] snapshot(
            Board board) {

        Piece[][] result =
                new Piece[
                        Board.SIZE
                ][
                        Board.SIZE
                ];

        for (int row = 0;
             row < Board.SIZE;
             row++) {

            for (int column = 0;
                 column < Board.SIZE;
                 column++) {

                result[row][column] =
                        board.pieceAt(
                                new Position(
                                        row,
                                        column
                                )
                        );
            }
        }

        return result;
    }

    public void restore(
            Board board,
            Piece[][] snapshot) {

        for (int row = 0;
             row < Board.SIZE;
             row++) {

            for (int column = 0;
                 column < Board.SIZE;
                 column++) {

                board.setPiece(
                        new Position(
                                row,
                                column
                        ),
                        snapshot[row][column]
                );
            }
        }
    }

    private boolean reachedLastRow(
            Position position,
            Player player) {

        return player == Player.BLACK
                ? position.row() == Board.SIZE - 1
                : position.row() == 0;
    }
}