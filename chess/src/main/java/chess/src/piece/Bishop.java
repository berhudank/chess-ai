package chess.src.piece;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public class Bishop extends Piece{

    public Bishop(int color, Square location) {
        super(color, location);
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {
        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        int columnDistance = targetLocation.getColumnDistance(location);

        if (Math.abs(rowDistance) != Math.abs(columnDistance)) // if the target is not in any diagonal direction, the move is invalid.
            return false;
        if (!((columnDistance == 1 || columnDistance == -1) && (rowDistance == 1 || rowDistance == -1))) { // skip the first squares between the two.
            // if there are any occupied squares on the way, move is invalid.
            Square[] between = location.getBoard().getSquaresBetween(location, targetLocation);
            for(Square sqr: between){
                if (!sqr.isEmpty()) {
                    return false;
                }
            }

        }

        return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
    }

    @Override
    public void move(String to) throws InvalidLocationException {
        Square targetLocation = location.getBoard().getSquareAt(to);

        targetLocation.setPiece(this);
        //clear previous location
        location.clear();
        //update current location
        location = targetLocation;
        location.getBoard().nextPlayer();
    }
    @Override
    public String toString() {
        return color == ChessBoard.WHITE ? "B" : "b";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new Bishop(this.color, newSquare);
    }
}
