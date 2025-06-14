package chess.src.piece;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public class Knight extends Piece{

    public Knight(int color, Square location) {
        super(color, location);
    }

    @Override
    public boolean canMove(String to) throws InvalidLocationException {
        Square targetLocation = location.getBoard().getSquareAt(to);
        int rowDistance = targetLocation.getRowDistance(location);
        int columnDistance = targetLocation.getColumnDistance(location);

        if (location == targetLocation)
            return false;
        // if the target location satisfies the one of the conditions below, the move is valid.
        if ((rowDistance == 2 || rowDistance == -2) && (columnDistance == 1 || columnDistance == -1))
            return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;
        if ((rowDistance == 1 || rowDistance == -1) && (columnDistance == 2 || columnDistance == -2))
            return targetLocation.isEmpty() || targetLocation.getPiece().getColor() != color;

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
        return color == ChessBoard.WHITE ? "N" : "n";
    }

    @Override
    public Piece clone(Square newSquare) {
        return new Knight(this.color, newSquare);
    }
}
