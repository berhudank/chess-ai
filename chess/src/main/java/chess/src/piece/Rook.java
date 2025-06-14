package chess.src.piece;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public class Rook extends Piece{

    public Rook(int color, Square location) {
        super(color, location);
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {

        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        int columnDistance = targetLocation.getColumnDistance(location);
        // if the current location equals the target or the target is on neither the same column nor the same row, the move is invalid.
        if (location == targetLocation || (columnDistance != 0 && rowDistance != 0)) {
            return false;
        }
        if(rowDistance > 1 || rowDistance < -1 || columnDistance > 1 || columnDistance < -1) { // skip the first squares between the two.
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
        return color == ChessBoard.WHITE ? "R" : "r";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new Rook(this.color, newSquare);
    }
}
