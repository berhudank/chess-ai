package chess.src.piece;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public class King extends Piece{

    public King(int color, Square location) {
        super(color, location);
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {
        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        int columnDistance = targetLocation.getColumnDistance(location);

        if(location == targetLocation) {
            return false;
        }
        // checking only the first squares around current location.
        if (columnDistance == 0){
            if (rowDistance == 1 || rowDistance == -1)
                return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
        } else if (columnDistance == 1 || columnDistance == -1) {
            if (rowDistance == 0 || rowDistance == 1 || rowDistance == -1)
                return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
        }
        return false;
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
        return color == ChessBoard.WHITE ? "K" : "k";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new King(this.color, newSquare);
    }
}
