package chess.src.board;

import chess.src.piece.Pawn;
import chess.src.piece.Piece;
import chess.src.piece.Queen;

public class Square {
    private ChessBoard board;
    private int column;
    private int row;
    private String squareSymbol;
    private Piece piece;


    public Square(ChessBoard board, int column, int row){
        this.board = board;
        this.column = column;
        this.row = row;
        this.squareSymbol = (char) (this.column + 97) + String.valueOf(this.row + 1);
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public Piece getPiece() {
        return piece;
    }

    public ChessBoard getBoard() {
        return board;
    }
    public int getRowDistance(Square location) {
        return this.row - location.row;
    }

    public int getColumnDistance(Square location){
        return this.column - location.column;
    }

    public String getSquareSymbol(){return this.squareSymbol;}

    public void setPiece(Piece piece) {
        // The internal structure of the piece prohibits the piece from moving to a square occupied by another piece of the same color.
        if(this.piece != null) { // so if this square is not empty, the piece at this square is captured
            board.decreaseCount(this.piece.getColor()); // and the piece count of that color is decreased by one.
        }
        if(piece != null){
            String source = piece.getLocationSymbol();
            String pieceSymbol = (piece.getColor() == ChessBoard.WHITE ? "w" : "b") + piece.toString().toUpperCase();
            board.updateBoardMap(source, this.squareSymbol, pieceSymbol);
        }
        this.piece = piece;

    }


    public boolean isEmpty(){
        return piece == null;
    }
    public boolean isAtSameColumn(Square s){
        return this.column == s.column;
    }
    public boolean isAtLastRow(int color){
        return (color == ChessBoard.WHITE && row == 7) || (color == ChessBoard.BLACK && row == 0);
    }
    public boolean isNeighborColumn(Square targetLocation) {
        return (this.column + 1 == targetLocation.column) || (this.column - 1 == targetLocation.column);
    }

    public void putNewQueen(Pawn pawn, int color) {
        this.piece = new Queen(color, this);
        board.updateBoardMap(pawn.getLocationSymbol(), this.squareSymbol, (this.piece.getColor() == ChessBoard.WHITE ? "w" : "b") + this.piece.toString().toUpperCase());
    }

    public void clear() {
        piece = null;
    }
}
