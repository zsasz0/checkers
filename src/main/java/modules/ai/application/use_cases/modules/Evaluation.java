package modules.ai.application.use_cases.modules;


import modules.game.domain.*;


public final class Evaluation {

    // =========================================================
    // Evaluation values
    // =========================================================

    private int manValue = 260;

    private int kingValue = 200;

    private int centerBonus = 24;

    private int extendedCenterBonus = 9;

    private int edgePenalty = -12;

    private int kingCenterBonus = 4;

    private int kingEdgePenalty = -2;

    private int progressWeight = 3;

    // =========================================================
    // Evaluation options
    // =========================================================

    private boolean edgePenaltyEnabled = true;

    private boolean kingProgressEnabled = true;

    private boolean centerEnabled = true;

    private boolean advancedPositionalEval = true;

    // =========================================================
    // Board evaluation
    // =========================================================

    public int evaluate(
            Board board,
            Player side) {

        Player opponent =
                side.opponent();

        int sideScore = 0;

        int opponentScore = 0;

        for (Position position :
                board.piecePositions(side)) {

            Piece piece =
                    board.pieceAt(position);

            if (piece != null) {

                sideScore +=
                        evaluatePiece(
                                piece,
                                position
                        );
            }
        }

        for (Position position :
                board.piecePositions(opponent)) {

            Piece piece =
                    board.pieceAt(position);

            if (piece != null) {

                opponentScore +=
                        evaluatePiece(
                                piece,
                                position
                        );
            }
        }

        return sideScore -
                opponentScore;
    }

    // =========================================================
    // Piece evaluation
    // =========================================================

    private int evaluatePiece(
            Piece piece,
            Position position) {

        int x =
                position.column();

        int y =
                position.row();

        int score;

        if (piece.isKing()) {

            score = kingValue;

            if (advancedPositionalEval &&
                    centerEnabled &&
                    isKingCenter(
                            x,
                            y,
                            Board.SIZE)) {

                score +=
                        kingCenterBonus;
            }

            if (advancedPositionalEval &&
                    edgePenaltyEnabled &&
                    isEdge(
                            x,
                            y,
                            Board.SIZE)) {

                score +=
                        kingEdgePenalty;
            }

        } else {

            score = manValue;

            /*
             * Forward progress toward promotion.
             */
            if (kingProgressEnabled) {

                if (piece.player() ==
                        Player.WHITE) {

                    score +=
                            (Board.SIZE - 1 - y)
                                    * progressWeight;

                } else {

                    score +=
                            y * progressWeight;
                }
            }

            /*
             * Center control.
             */
            if (centerEnabled) {

                if (isDeepCenter(
                        x,
                        y,
                        Board.SIZE)) {

                    score +=
                            centerBonus;

                } else if (
                        isExtendedCenter(
                                x,
                                y,
                                Board.SIZE)) {

                    score +=
                            extendedCenterBonus;

                } else if (
                        edgePenaltyEnabled &&
                        isEdge(
                                x,
                                y,
                                Board.SIZE)) {

                    score +=
                            edgePenalty;
                }
            }
        }

        return score;
    }

    // =========================================================
    // Board geometry
    // =========================================================



    private boolean isDeepCenter(
            int x,
            int y,
            int size) {

        return size == 8 &&
                (x == 3 || x == 4) &&
                (y == 3 || y == 4);
    }

    private boolean isExtendedCenter(
            int x,
            int y,
            int size) {

        if (size != 8) {
            return false;
        }

        return x >= 2 &&
                x <= 5 &&
                y >= 2 &&
                y <= 5;
    }

    private boolean isKingCenter(
            int x,
            int y,
            int size) {

        if (size == 8) {

            return (x == 3 || x == 4) &&
                    (y == 3 || y == 4);
        }

        if (size == 10) {

            return (x == 4 || x == 5) &&
                    (y == 4 || y == 5);
        }

        return false;
    }

    private boolean isEdge(
            int x,
            int y,
            int size) {

        int last =
                size - 1;

        return x == 0 ||
                x == last ||
                y == 0 ||
                y == last;
    }

    // =========================================================
    // Configuration
    // =========================================================

    public void setManValue(int value) {
        this.manValue = value;
    }

    public void setKingValue(int value) {
        this.kingValue = value;
    }

    public void setCenterBonus(int value) {
        this.centerBonus = value;
    }

    public void setExtendedCenterBonus(int value) {
        this.extendedCenterBonus = value;
    }

    public void setEdgePenalty(int value) {
        this.edgePenalty = value;
    }

    public void setKingCenterBonus(int value) {
        this.kingCenterBonus = value;
    }

    public void setKingEdgePenalty(int value) {
        this.kingEdgePenalty = value;
    }

    public void setProgressWeight(int value) {
        this.progressWeight = value;
    }

    public void setEdgePenaltyEnabled(
            boolean enabled) {

        this.edgePenaltyEnabled =
                enabled;
    }

    public void setKingProgressEnabled(
            boolean enabled) {

        this.kingProgressEnabled =
                enabled;
    }

    public void setCenterEnabled(
            boolean enabled) {

        this.centerEnabled =
                enabled;
    }

    public void setAdvancedPositionalEval(
            boolean enabled) {

        this.advancedPositionalEval =
                enabled;
    }
}