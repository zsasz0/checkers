package modules.ai.application.use_cases.modules;


import modules.game.domain.*;

import java.util.Comparator;
import java.util.List;

public final class MoveOrdering {

    // =========================================================
    // Move sorting
    // =========================================================

    public void sortMoves(
            List<Move> moves,
            int ply,
            Move previousMove,
            Move[][] pvTable,
            Move[] killerByPly,
            Move[] killerByMove,
            int[] historyTable) {

        Comparator<Move> comparator =
                new Comparator<Move>() {

                    @Override
                    public int compare(
                            Move a,
                            Move b) {

                        int scoreA =
                                moveOrderingScore(
                                        a,
                                        ply,
                                        previousMove,
                                        pvTable,
                                        killerByPly,
                                        killerByMove,
                                        historyTable
                                );

                        int scoreB =
                                moveOrderingScore(
                                        b,
                                        ply,
                                        previousMove,
                                        pvTable,
                                        killerByPly,
                                        killerByMove,
                                        historyTable
                                );

                        return Integer.compare(
                                scoreB,
                                scoreA
                        );
                    }
                };

        moves.sort(comparator);
    }

    // =========================================================
    // Move ordering score
    // =========================================================

    private int moveOrderingScore(
            Move move,
            int ply,
            Move previousMove,
            Move[][] pvTable,
            Move[] killerByPly,
            Move[] killerByMove,
            int[] historyTable) {

        int score =
                orderScore(
                        move,
                        historyTable
                );

        /*
         * Principal variation move.
         */
        Move pvMove =
                pvTable[0][ply];

        if (movesEqual(
                move,
                pvMove)) {

            score += 18_000_000;
        }

        /*
         * Killer move for this ply.
         */
        Move killer =
                killerByPly[ply];

        if (movesEqual(
                move,
                killer)) {

            score += 100_000;
        }

        /*
         * Killer move associated with
         * the previous move.
         */
        if (previousMove != null) {

            Move previousKiller =
                    killerByMove[
                            moveIndex(previousMove)
                    ];

            if (movesEqual(
                    move,
                    previousKiller)) {

                score += 99_998;
            }
        }

        return score;
    }

    // =========================================================
    // History heuristic
    // =========================================================

    public void updateHistory(
            Move move,
            int depth,
            int[] historyTable) {

        int index =
                moveIndex(move);

        historyTable[index] +=
                depth * depth;
    }

    // =========================================================
    // Move index
    // =========================================================

    public int moveIndex(
            Move move) {

        int a =
                move.from().column() * 10
                        + move.from().row();

        int b =
                move.to().column() * 10
                        + move.to().row();

        return a + b * 100;
    }

    // =========================================================
    // Base ordering score
    // =========================================================

    private int orderScore(
            Move move,
            int[] historyTable) {

        int captureScore =
                move.captured().size() * 255;

        int captureBonus =
                move.isCapture()
                        ? 255
                        : 0;

        int historyScore =
                historyTable[
                        moveIndex(move)
                ];

        return captureScore
                + captureBonus
                + historyScore;
    }

    // =========================================================
    // Move comparison
    // =========================================================

    private boolean movesEqual(
            Move a,
            Move b) {

        if (a == null ||
                b == null) {

            return false;
        }

        return a.from().equals(b.from())
                && a.to().equals(b.to());
    }
}