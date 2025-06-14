package chess.src.piece;

import chess.src.board.InvalidLocationException;
import chess.src.board.Square;

public abstract class Piece {
    protected int color;
    protected Square location;

    public Piece(int color, Square location) {
        this.color = color;
        this.location = location;
    }

    public int getColor(){return color;}

    public String getLocationSymbol(){return this.location.getSquareSymbol();}

    public abstract boolean canMove (String to) throws InvalidLocationException;

    public abstract void move (String to) throws InvalidLocationException;

    public abstract Piece clone(Square newSquare);

    public Square getLocation() {
        return location;
    }
}
