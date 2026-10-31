package modules.game.application.use_cases.rules.utils;

import java.util.ArrayList;
import java.util.List;

import modules.game.domain.*;



public class SpanishTunisianCaptureStrategy
implements CaptureStrategy {

@Override
public List<Move> findCaptures(
    Board board,
    Player player) {

List<Move> captures = new ArrayList<>();

for (Position position : board.piecePositions(player)) {

    Piece piece = board.pieceAt(position);

    List<Move> pieceCaptures;

    if (piece.isKing()) {
        pieceCaptures = collectKingCaptures(
                board,
                position,
                piece,
                player,
                new ArrayList<>(List.of(position)),
                new ArrayList<>()
        );
    } else {
        pieceCaptures = collectManCaptures(
                board,
                position,
                piece,
                player,
                new ArrayList<>(List.of(position)),
                new ArrayList<>()
        );
    }

    captures.addAll(pieceCaptures);
}

return captures;
}

private List<Move> collectManCaptures(
    Board board,
    Position pos,
    Piece piece,
    Player player,
    List<Position> path,
    List<Position> captured) {

List<Move> result = new ArrayList<>();

int forward = player.forwardDirection();

for (int dc = -1; dc <= 1; dc += 2) {

    Position over = pos.offset(forward, dc);
    Position landing = pos.offset(
            forward * 2,
            dc * 2
    );

    if (!board.inBounds(landing)) {
        continue;
    }

    Piece enemy = board.pieceAt(over);

    if (enemy == null || enemy.player() == player) {
        continue;
    }

    if (!board.isEmpty(landing)) {
        continue;
    }

    // Simulate capture
    board.setPiece(pos, null);
    board.removePiece(over);
    board.setPiece(landing, piece);

    List<Position> newPath =
            new ArrayList<>(path);

    List<Position> newCaptured =
            new ArrayList<>(captured);

    newPath.add(landing);
    newCaptured.add(over);

    if (reachedLastRow(landing, player)) {

        result.add(new Move(
                newPath.get(0),
                landing,
                newPath,
                newCaptured
        ));

    } else {

        result.addAll(
                collectManCaptures(
                        board,
                        landing,
                        piece,
                        player,
                        newPath,
                        newCaptured
                )
        );
    }

    // Undo simulation
    board.setPiece(landing, null);
    board.setPiece(over, enemy);
    board.setPiece(pos, piece);
}

/*
 * If no further capture was found but we already
 * captured something, this is a complete sequence.
 */
if (result.isEmpty() && !captured.isEmpty()) {

    result.add(new Move(
            path.get(0),
            path.get(path.size() - 1),
            new ArrayList<>(path),
            new ArrayList<>(captured)
    ));
}

return result;
}

private List<Move> collectKingCaptures(
    Board board,
    Position pos,
    Piece piece,
    Player player,
    List<Position> path,
    List<Position> captured) {

List<Move> result = new ArrayList<>();

for (int dr = -1; dr <= 1; dr += 2) {
    for (int dc = -1; dc <= 1; dc += 2) {

        Position current =
                pos.offset(dr, dc);

        Position enemyPosition = null;
        Piece enemyPiece = null;

        while (board.inBounds(current)) {

            Piece target =
                    board.pieceAt(current);

            if (enemyPosition == null) {

                if (target == null) {
                    current =
                            current.offset(dr, dc);
                    continue;
                }

                if (target.player() == player) {
                    break;
                }

                enemyPosition = current;
                enemyPiece = target;

                current =
                        current.offset(dr, dc);

                continue;
            }

            if (target != null) {
                break;
            }

            Position landing = current;

            // Simulate capture
            board.setPiece(pos, null);
            board.removePiece(enemyPosition);
            board.setPiece(landing, piece);

            List<Position> newPath =
                    new ArrayList<>(path);

            List<Position> newCaptured =
                    new ArrayList<>(captured);

            newPath.add(landing);
            newCaptured.add(enemyPosition);

            result.addAll(
                    collectKingCaptures(
                            board,
                            landing,
                            piece,
                            player,
                            newPath,
                            newCaptured
                    )
            );

            // Undo simulation
            board.setPiece(landing, null);
            board.setPiece(
                    enemyPosition,
                    enemyPiece
            );
            board.setPiece(pos, piece);

            current =
                    current.offset(dr, dc);
        }
    }
}

/*
 * No more captures.
 */
if (result.isEmpty() && !captured.isEmpty()) {

    result.add(new Move(
            path.get(0),
            path.get(path.size() - 1),
            new ArrayList<>(path),
            new ArrayList<>(captured)
    ));
}

return result;
}

private boolean reachedLastRow(
    Position position,
    Player player) {

return player == Player.BLACK
        ? position.row() == Board.SIZE - 1
        : position.row() == 0;
}
}