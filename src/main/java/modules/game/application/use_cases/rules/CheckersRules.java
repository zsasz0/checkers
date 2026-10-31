package modules.game.application.use_cases.rules;





import java.util.ArrayList;
import java.util.List;

import modules.game.application.use_cases.rules.utils.CaptureStrategy;
import modules.game.domain.*;





public class CheckersRules {

    public static final int DRAW_MOVE_LIMIT = 40;

    private final CaptureStrategy captureStrategy;

    public  CheckersRules(
            CaptureStrategy captureStrategy) {

        this.captureStrategy = captureStrategy;
    }

    /**
     * Returns all legal moves for the player.
     *
     * Captures are mandatory.
     * If captures exist, only moves capturing
     * the maximum number of pieces are returned.
     */
   
    public List<Move> legalMoves(Board board,Player player) {

        List<Move> captures =
                captureStrategy.findCaptures(
                        board,
                        player
                );

        if (!captures.isEmpty()) {

            int max = 0;

            for (Move move : captures) {
                max = Math.max(
                        max,
                        move.captured().size()
                );
            }

            List<Move> best =
                    new ArrayList<>();

            for (Move move : captures) {
                if (move.captured().size() == max) {
                    best.add(move);
                }
            }

            return best;
        }

        List<Move> simple =
                new ArrayList<>();

        for (Position pos :
                board.piecePositions(player)) {

            Piece piece =
                    board.pieceAt(pos);

            collectSimpleMoves(
                    board,
                    pos,
                    piece,
                    player,
                    simple
            );
        }

        return simple;
    }

    /**
     * Applies a move to the game.
     */

public void applyMove(
        Game game,
        Move move) {

    Player mover = game.currentPlayer();
    Board board = game.board();

    System.out.println(
            "[RULES] " + mover
                    + " attempts: "
                    + move.from()
                    + " -> "
                    + move.to()
    );

    // =====================================================
    // FINAL LEGAL-MOVE CHECK
    // =====================================================

    List<Move> legalMoves =
            legalMoves(board, mover);

    Move validatedMove = legalMoves.stream()
            .filter(candidate ->
                    samePosition(
                            candidate.from(),
                            move.from()
                    )
                    &&
                    samePosition(
                            candidate.to(),
                            move.to()
                    )
            )
            .findFirst()
            .orElse(null);

    if (validatedMove == null) {

        System.out.println(
                "[RULES] ERROR: Illegal move rejected: "
                        + move.from()
                        + " -> "
                        + move.to()
        );

        return;
    }

    // =====================================================
    // APPLY THE VALIDATED LEGAL MOVE
    // =====================================================

    Piece piece =
            board.pieceAt(validatedMove.from());

    if (piece == null) {

        System.out.println(
                "[RULES] ERROR: No piece at "
                        + validatedMove.from()
        );

        return;
    }

    System.out.println(
            "[RULES] Piece: "
                    + piece.player()
                    + (piece.isKing()
                    ? " KING"
                    : " NORMAL")
    );

    // Remove piece from original position
    board.setPiece(
            validatedMove.from(),
            null
    );

    // Remove captured pieces from the
    // authoritative legal Move
    for (Position capturedPos :
            validatedMove.captured()) {

        System.out.println(
                "[RULES] Capturing piece at "
                        + capturedPos
        );

        board.removePiece(capturedPos);
    }

    // Move piece
    Position landing =
            validatedMove.to();

    board.setPiece(
            landing,
            piece
    );

    System.out.println(
            "[RULES] "
                    + mover
                    + " moved "
                    + validatedMove.from()
                    + " -> "
                    + landing
    );

    // Promotion
    if (!piece.isKing()
            && reachedLastRow(
                    landing,
                    piece.player())) {

        System.out.println(
                "[RULES] Piece promoted to KING at "
                        + landing
        );

        board.setPiece(
                landing,
                piece.promoted()
        );
    }

    // Record the validated move
    game.recordMove(validatedMove);

    // Next turn
    game.switchPlayer();

    System.out.println(
            "[RULES] Next player: "
                    + game.currentPlayer()
    );

    Player next =
            game.currentPlayer();

    // Check game end
    if (board.countPieces(next) == 0
            || legalMoves(
                    board,
                    next
            ).isEmpty()) {

        GameStatus result =
                mover == Player.BLACK
                        ? GameStatus.BLACK_WON
                        : GameStatus.WHITE_WON;

        System.out.println(
                "[RULES] Game finished: "
                        + result
        );

        game.finish(result);

    } else if (
            game.movesWithoutCapture()
                    >= DRAW_MOVE_LIMIT) {

        System.out.println(
                "[RULES] Game finished: DRAW"
        );

        game.finish(GameStatus.DRAW);
    }
}


private boolean samePosition(
        Position a,
        Position b) {

    return a.row() == b.row()
            && a.column() == b.column();
}
    public Piece pieceAfterMove(
            Piece piece,
            Position landing,
            Player owner) {

        if (piece.isKing()) {
            return piece;
        }

        return reachedLastRow(
                landing,
                owner
        )
                ? piece.promoted()
                : piece;
    }

    /**
     * Temporary.
     *
     * We will move this into
     * SpanishTunisianMoveStrategy next.
     */
    private void collectSimpleMoves(
            Board board,
            Position pos,
            Piece piece,
            Player player,
            List<Move> result) {

        if (piece.isKing()) {

            for (int dr = -1; dr <= 1; dr += 2) {
                for (int dc = -1; dc <= 1; dc += 2) {

                    Position current =
                            pos.offset(dr, dc);

                    while (
                            board.inBounds(current)
                                    && board.isEmpty(current)
                    ) {

                        result.add(
                                new Move(
                                        pos,
                                        current
                                )
                        );

                        current =
                                current.offset(dr, dc);
                    }
                }
            }

            return;
        }

        int forward =
                player.forwardDirection();

        for (int dc = -1; dc <= 1; dc += 2) {

            Position to =
                    pos.offset(forward, dc);

            if (board.inBounds(to)
                    && board.isEmpty(to)) {

                result.add(
                        new Move(pos, to)
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