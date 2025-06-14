package chess.src.piece;

import chess.src.board.*;

public class Pawn extends Piece {
    private boolean initialLocation;
    public boolean isPromoted = false;

    public Pawn(int color, Square location, boolean initialLocation) {
        super(color, location);
        this.initialLocation = initialLocation;
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {
        boolean validMove = false;

        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        if (this.location.isAtSameColumn(targetLocation)) {
            if (color == ChessBoard.WHITE && rowDistance > 0 && rowDistance <= 2) {
                if (rowDistance == 2) {
                    if (initialLocation) {
                        // pawn is moving twice, check two squares in front are empty
                        Square[] between = location.getBoard().getSquaresBetween(location, targetLocation);
                        validMove = targetLocation.isEmpty() && between[0].isEmpty();
                    }
                } else {
                    validMove = targetLocation.isEmpty();
                }
                return validMove;
            } else if (color == ChessBoard.BLACK && rowDistance < 0 && rowDistance >= -2) {
                if (rowDistance == -2) {
                    if (initialLocation) {
                        // pawn is moving twice, check two squares in front are empty
                        Square[] between = location.getBoard().getSquaresBetween(location, targetLocation);
                        validMove = targetLocation.isEmpty() && between[0].isEmpty();
                    }
                } else {
                    validMove = targetLocation.isEmpty();
                }
            }

            // attacking diagonals
        } else if (this.location.isNeighborColumn(targetLocation)) {
            if (color == ChessBoard.WHITE && rowDistance == 1) {
                validMove = !targetLocation.isEmpty() && targetLocation.getPiece().getColor() == ChessBoard.BLACK;
            } else if (color == ChessBoard.BLACK && rowDistance == -1) {
                validMove = !targetLocation.isEmpty() && targetLocation.getPiece().getColor() == ChessBoard.WHITE;
            }

        }
        return validMove;
    }

    @Override
    public void move(String to) throws InvalidLocationException {

        Square targetLocation = location.getBoard().getSquareAt(to);
        // promoteToQueen
        if (targetLocation.isAtLastRow(color)) {
            targetLocation.putNewQueen(this, color);
            isPromoted = true;
        } else {
            targetLocation.setPiece(this);
            // clear previous location
            location.clear();
            // update current location
            location = targetLocation;
            if (initialLocation)
                initialLocation = false;
        }

        location.getBoard().nextPlayer();
        
    }

    @Override
    public String toString() {
        return color == ChessBoard.WHITE ? "P" : "p";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new Pawn(this.color, newSquare, this.initialLocation);
    }
}
