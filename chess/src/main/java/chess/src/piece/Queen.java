package chess.src.piece;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public class Queen extends Piece{

    public Queen(int color, Square location) {
        super(color, location);
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {
        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        int columnDistance = targetLocation.getColumnDistance(location);

        if (location == targetLocation)
            return false;
        if (columnDistance != 0){ // if the target is not on the same column
            if(rowDistance == 0 || columnDistance == rowDistance || columnDistance == -rowDistance){
                if (!(rowDistance == 1 || rowDistance == -1)) { // skip the first squares between the two.
                    // if there are any occupied squares on the way, move is invalid.
                    Square[] between = location.getBoard().getSquaresBetween(location, targetLocation);
                    for (Square sqr : between) {
                        if (!sqr.isEmpty()) {
                            return false;
                        }
                    }
                }
                return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
            }
            return false; // if it is not on the same row or in any diagonal direction, the move is invalid.
        }
        else{
            if (rowDistance > 1 || rowDistance < -1) { // skip the first squares between the two.
                // if there are any occupied squares on the way, move is invalid.
                Square[] between = location.getBoard().getSquaresBetween(location, targetLocation);
                for (Square sqr : between) {
                    if (!sqr.isEmpty()) {
                        return false;
                    }
                }
            }
            return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
        }

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
        return color == ChessBoard.WHITE ? "Q" : "q";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new Queen(this.color, newSquare);
    }
}
