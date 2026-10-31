package modules.ai.application.use_cases.modules;






import java.util.Arrays;
import java.util.List;

import modules.game.application.use_cases.rules.CheckersRules;
import modules.game.application.use_cases.rules.utils.SpanishTunisianCaptureStrategy;
import modules.game.domain.*;

public final class Search {

    // =========================================================
    // Search constants
    // =========================================================

    private static final int MAX_SCORE = 12000;
    private static final int MAX_PLY = 32;
    private static final int HISTORY_SIZE = 10201;
    private static final int TIME_CHECK_MASK = 255;

    // =========================================================
    // Dependencies
    // =========================================================

    private final Evaluation evaluation;
    private final MoveOrdering moveOrdering;
    private final BoardSimulator boardSimulator;

    // =========================================================
    // Search state
    // =========================================================



    private long nodesSearched;
    private long searchStartTime;
    private long elapsedTime;

    private long maxSearchTimeMs = 5000;

    private boolean abortSearch;

    private Move bestMove;

    // =========================================================
    // Search tables
    // =========================================================

    /*
     * AI-specific data.
     *
     * Move itself remains immutable and contains
     * no AI search information.
     */
    private final Move[][] pvTable =
            new Move[MAX_PLY][MAX_PLY];

    private final int[] pvLength =
            new int[MAX_PLY];

    private final int[] historyTable =
            new int[HISTORY_SIZE];

    private final Move[] killerByMove =
            new Move[HISTORY_SIZE];

    private final Move[] killerByPly =
            new Move[MAX_PLY];

    private  final  CheckersRules rules =
            new CheckersRules(
                    new SpanishTunisianCaptureStrategy()
            );
    // =========================================================
    // Constructor
    // =========================================================

    public Search(
            Evaluation evaluation,
            MoveOrdering moveOrdering,
            BoardSimulator boardSimulator) {

        this.evaluation = evaluation;
        this.moveOrdering = moveOrdering;
        this.boardSimulator = boardSimulator;

        clearPvTable();
    }

    // =========================================================
    // Main entry point
    // =========================================================

    public Move getBestMove(
            Game game,
            Player player,
            int depth) {

        Board board = game.board();

        List<Move> legalMoves =
                rules.legalMoves(
                        board,
                        player
                );

        if (legalMoves.isEmpty()) {
            return null;
        }

        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

 

        nodesSearched = 0;

        bestMove =
                legalMoves.get(0);

        abortSearch = false;

        searchStartTime =
                System.currentTimeMillis();

        clearHistoryTable();
        clearPvTable();

        /*
         * Iterative deepening.
         */
        for (int currentDepth = 1;
             currentDepth <= depth;
             currentDepth++) {

            if (shouldStop()) {
                break;
            }

            Move previousBest =
                    bestMove;

            rootSearch(
                    game,
                    player,
                    currentDepth,
                    -MAX_SCORE,
                    MAX_SCORE
            );

            /*
             * Keep the last completed result
             * if the time limit was reached.
             */
            if (abortSearch) {
                bestMove = previousBest;
                break;
            }
        }

        elapsedTime =
                System.currentTimeMillis()
                        - searchStartTime;

        System.out.println(
                "AI searched "
                        + nodesSearched
                        + " nodes in "
                        + elapsedTime
                        + " ms"
        );

        return bestMove;
    }

    // =========================================================
    // Root search
    // =========================================================

    private int rootSearch(
            Game game,
            Player player,
            int depth,
            int alpha,
            int beta) {

        Board board = game.board();

        List<Move> moves =
                rules.legalMoves(
                        board,
                        player
                );

        if (moves.isEmpty()) {
            return -MAX_SCORE;
        }

        /*
         * Order root moves.
         */
        moveOrdering.sortMoves(
                moves,
                0,
                null,
                pvTable,
                killerByPly,
                killerByMove,
                historyTable
        );

        int bestScore = -MAX_SCORE;
        int currentAlpha = alpha;

        for (Move move : moves) {

            if (shouldStop()) {
                abortSearch = true;
                break;
            }

            Piece[][] snapshot =
                    boardSimulator.snapshot(board);

            boardSimulator.makeMove(
                    board,
                    move
            );

            Player opponent =
                    player.opponent();

            int score =
                    -negamax(
                            game,
                            opponent,
                            depth - 1,
                            1,
                            -beta,
                            -currentAlpha,
                            move
                    );

            boardSimulator.restore(
                    board,
                    snapshot
            );

            if (abortSearch) {
                break;
            }

            if (score > bestScore) {

                bestScore = score;

                bestMove = move;
            }

            if (score > currentAlpha) {
                currentAlpha = score;
            }

            if (currentAlpha >= beta) {
                break;
            }
        }

        return bestScore;
    }

    // =========================================================
    // Negamax
    // =========================================================

    private int negamax(
            Game game,
            Player player,
            int depth,
            int ply,
            int alpha,
            int beta,
            Move previousMove) {

        nodesSearched++;

        if ((nodesSearched & TIME_CHECK_MASK) == 0) {

            if (shouldStop()) {

                abortSearch = true;

                return 0;
            }
        }

        if (ply >= MAX_PLY - 1) {

            return evaluation.evaluate(
                    game.board(),
                    player
            );
        }

        if (depth <= 0) {

            return evaluation.evaluate(
                    game.board(),
                    player
            );
        }

        Board board = game.board();

        List<Move> moves =
                rules.legalMoves(
                        board,
                        player
                );

        /*
         * No legal moves means loss.
         */
        if (moves.isEmpty()) {
            return -MAX_SCORE + ply;
        }

        /*
         * Order moves before searching.
         */
        moveOrdering.sortMoves(
                moves,
                ply,
                previousMove,
                pvTable,
                killerByPly,
                killerByMove,
                historyTable
        );

        /*
         * Extend search when there is only
         * one legal move.
         */
        if (moves.size() == 1) {
            depth++;
        }

        int bestValue = -MAX_SCORE;

        int bestAlpha = alpha;

        for (Move move : moves) {

            Piece[][] snapshot =
                    boardSimulator.snapshot(board);

            boardSimulator.makeMove(
                    board,
                    move
            );

            Player opponent =
                    player.opponent();

            int value =
                    -negamax(
                            game,
                            opponent,
                            depth - 1,
                            ply + 1,
                            -beta,
                            -bestAlpha,
                            move
                    );

            boardSimulator.restore(
                    board,
                    snapshot
            );

            if (abortSearch) {
                return 0;
            }

            /*
             * Alpha-beta cutoff.
             */
            if (value >= beta) {

                /*
                 * Store quiet moves as killer moves.
                 */
                if (!move.isCapture()) {

                    killerByPly[ply] =
                            move;

                    if (previousMove != null) {

                        killerByMove[
                                moveOrdering.moveIndex(
                                        previousMove
                                )
                        ] = move;
                    }
                }

                moveOrdering.updateHistory(
                        move,
                        depth,
                        historyTable
                );

                return value;
            }

            if (value > bestValue) {
                bestValue = value;
            }

            if (value > bestAlpha) {

                bestAlpha = value;

                pvTable[ply][ply] =
                        move;

                pvLength[ply] =
                        ply + 1;
            }
        }

        return bestValue;
    }

    // =========================================================
    // Search tables
    // =========================================================

    private void clearHistoryTable() {

        Arrays.fill(
                historyTable,
                0
        );
    }

    private void clearPvTable() {

        for (int i = 0;
             i < MAX_PLY;
             i++) {

            Arrays.fill(
                    pvTable[i],
                    null
            );

            pvLength[i] = 0;
        }
    }

    // =========================================================
    // Time control
    // =========================================================

    private boolean shouldStop() {

        elapsedTime =
                System.currentTimeMillis()
                        - searchStartTime;

        return elapsedTime >
                maxSearchTimeMs;
    }

    public void setMaxSearchTimeMs(
            long milliseconds) {

        this.maxSearchTimeMs =
                milliseconds;
    }
}