package chess.src.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chess.src.piece.*;

public class ChessBoard {
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    private int whiteCount;
    private int blackCount;
    private int currentPlayer;

    public Square[][] boardMatrix = new Square[8][8];
    public Map<String, String> boardMap = new HashMap<>(64);

    // boardMatrix[0] represents the array that contains the squares at these
    // locations -> {A1, A2, A3, A4, A5, A6, A7, A8}
    // So boardMatrix[0][0] represents the Square at the location A1

    public ChessBoard(String fen) {
        String[] parts = fen.split(" ");
        String[] rows = parts[0].split("/");

        for (int j = 0; j < 8; j++) { // j = FEN row index (0 = rank 8, 7 = rank 1)
            int row = 7 - j; // Map FEN row to boardMatrix row
            int i = 0; // column (file)
            for (char c : rows[j].toCharArray()) {
                if (Character.isDigit(c)) {
                    int empty = c - '0';
                    for (int k = 0; k < empty; k++) {
                        Square sqr = new Square(this, i, row);
                        this.boardMatrix[i][row] = sqr;
                        i++;
                    }
                } else {
                    boolean isWhite = Character.isUpperCase(c);
                    int color = isWhite ? WHITE : BLACK;
                    Square sqr = new Square(this, i, row);
                    Piece piece = null;
                    String pieceSymbol = (isWhite ? "w" : "b") + Character.toUpperCase(c);

                    switch (Character.toLowerCase(c)) {
                        case 'k':
                            piece = new King(color, sqr);
                            break;
                        case 'q':
                            piece = new Queen(color, sqr);
                            break;
                        case 'r':
                            piece = new Rook(color, sqr);
                            break;
                        case 'b':
                            piece = new Bishop(color, sqr);
                            break;
                        case 'n':
                            piece = new Knight(color, sqr);
                            break;
                        case 'p':
                            piece = new Pawn(color, sqr, true);
                            break;
                    }
                    this.boardMatrix[i][row] = sqr;
                    if (piece != null) {
                        sqr.setPiece(piece);
                        this.boardMap.put(sqr.getSquareSymbol(), pieceSymbol);
                    }
                    i++;
                }
            }
        }
        this.currentPlayer = parts[1].equals("w") ? WHITE : BLACK;
        // Optionally parse castling, en passant, etc.
    }

    public ChessBoard(ChessBoard original) {
        this.whiteCount = original.whiteCount;
        this.blackCount = original.blackCount;
        this.currentPlayer = original.currentPlayer;
        this.boardMap = new HashMap<>(original.boardMap); // shallow copy is fine since keys/values are strings

        // Deep copy boardMatrix
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square originalSquare = original.boardMatrix[i][j];
                this.boardMatrix[i][j] = new Square(this, i, j); // recreate square for this board
                Piece piece = originalSquare.getPiece();
                if (piece != null) {
                    this.boardMatrix[i][j].setPiece(piece.clone(this.boardMatrix[i][j])); // clone with new square
                }
            }
        }
    }

    public ChessBoard() {
        // fills the board with squares
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square sqr = new Square(this, i, j);
                boardMatrix[i][j] = sqr;
                Piece p = fill(i, j, sqr);
                sqr.setPiece(p);
                if (p == null)
                    continue;
                String square = (char) (97 + i) + String.valueOf(j + 1);
                String pieceSymbol = (p.getColor() == WHITE ? "w" : "b") + p.toString().toUpperCase();
                boardMap.put(square, pieceSymbol);
            }
        }
        whiteCount = 16;
        blackCount = 16;
    }

    // fills the squares with pieces
    private Piece fill(int i, int j, Square sqr) {
        int color = j <= 1 ? WHITE : BLACK; // determines the color of the piece to be put according to the row number.
        // The array contains the pieces to be put on the first and last row from column
        // A to column H.
        // The square which the piece are on is assigned to that piece.
        Piece[] pieces = {
                new Rook(color, sqr), new Knight(color, sqr), new Bishop(color, sqr),
                new Queen(color, sqr), new King(color, sqr), new Bishop(color, sqr),
                new Knight(color, sqr), new Rook(color, sqr)
        };

        if (j <= 1) {
            return j == 1 ? new Pawn(color, sqr, true) : pieces[i]; // puts the correct white piece to that square if it
                                                                    // is the first row, otherwise puts a pawn.
        } else if (j >= 6) {
            return j == 6 ? new Pawn(color, sqr, true) : pieces[i]; // does the same thing but puts black piece instead.
        } else {
            return null; // there are no pieces between the first and the last two rows.
        }
    }

    public Piece getPieceAt(String location) throws InvalidLocationException {
        return getSquareAt(location).getPiece();
    }

    public Square getSquareAt(String location) throws InvalidLocationException {
        // The user must type only two characters.
        if (location.length() != 2)
            throw new InvalidLocationException("    Only two characters are allowed! Usage example: 'a3' ");

        String[] input = location.split(""); // splitting the string into two substrings of single character.
        int col = input[0].toLowerCase().charAt(0) - 97; // converting the character typed for the column to
                                                         // corresponding index on the boardMatrix array
                                                         // 97 is the ASCII value of the lowercase 'a'.
        int row;
        try {
            row = Integer.parseInt(input[1]) - 1;
        } catch (NumberFormatException nfe) {
            throw new InvalidLocationException("    The row must be a number! Usage example: 'a3' ");
        }
        // restricts the range of the rows and columns that can be typed.
        if (col < 0 || col > 7 || row < 0 || row > 7)
            throw new InvalidLocationException("    Invalid range! Type a-h for the column, 1-8 for the row");
        return boardMatrix[col][row];
    }

    public Square[] getSquaresBetween(Square location, Square targetLocation) {
        ArrayList<Square> squaresBetween = new ArrayList<>();

        int initialColumn = location.getColumn();
        int finalColumn = targetLocation.getColumn();
        int initialRow = location.getRow();
        int finalRow = targetLocation.getRow();

        int rowDiff = finalRow - initialRow;
        int columnDiff = finalColumn - initialColumn;

        // if the squares are on the same column
        if (columnDiff == 0) {
            if (rowDiff > 0) {
                for (int i = initialRow + 1; i < finalRow; i++) {
                    squaresBetween.add(boardMatrix[initialColumn][i]);
                }
            } else {
                for (int i = initialRow - 1; i > finalRow; i--) {
                    squaresBetween.add(boardMatrix[initialColumn][i]);
                }
            }
        }
        // if the squares on the same row
        else if (rowDiff == 0) {
            if (columnDiff > 0) {
                for (int i = initialColumn + 1; i < finalColumn; i++) {
                    squaresBetween.add(boardMatrix[i][initialRow]);
                }
            } else {
                for (int i = initialColumn - 1; i > finalColumn; i--) {
                    squaresBetween.add(boardMatrix[i][initialRow]);
                }
            }
        }
        // then the squares are on the same diagonal. Check the positions.
        else {
            // bottom-left to top-right
            if (columnDiff > 0 && rowDiff > 0) {
                for (int i = initialColumn + 1, j = initialRow + 1; i < finalColumn; i++, j++) {
                    squaresBetween.add(boardMatrix[i][j]);
                }
                // top-right to bottom-left
            } else if (columnDiff < 0 && rowDiff < 0) {
                for (int i = initialColumn - 1, j = initialRow - 1; i > finalColumn; i--, j--) {
                    squaresBetween.add(boardMatrix[i][j]);
                }
                // top-left to bottom-right
            } else if (columnDiff > 0) {
                for (int i = initialColumn + 1, j = initialRow - 1; i < finalColumn; i++, j--) {
                    squaresBetween.add(boardMatrix[i][j]);
                }
                // bottom-right to top-left
            } else {
                for (int i = initialColumn - 1, j = initialRow + 1; i > finalColumn; i--, j++) {
                    squaresBetween.add(boardMatrix[i][j]);
                }
            }
        }
        // the return type is a Square array, so it is converted to an array.
        return squaresBetween.toArray(new Square[squaresBetween.size()]);
    }

    public String getCurrentPlayer() {
        return currentPlayer == WHITE ? "white" : "black";
    }

    // Updates the whiteCount or blackCount variables when a piece has been
    // captured.
    public void decreaseCount(int capturedColor) {
        if (capturedColor == WHITE)
            whiteCount--;
        else
            blackCount--;
    }

    public void updateBoardMap(String sourceSquare, String targetSquare, String pieceSymbol) {
        boardMap.put(targetSquare, pieceSymbol);
        boardMap.remove(sourceSquare);
    }

    // Checks if the king of the specified color is in check.
    // A king is in check if it is attacked by at least one piece of the opposite
    public boolean isKingInCheck(int color) {
        Square kingSquare = findKing(color);
        int opponentColor = (color == WHITE) ? BLACK : WHITE;
        return isSquareAttacked(kingSquare, opponentColor);
    }

    // Finds the king of the specified color on the board.
    public Square findKing(int color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = boardMatrix[i][j].getPiece();
                if (piece != null && piece instanceof King && piece.getColor() == color) {
                    return boardMatrix[i][j];
                }
            }
        }
        return null; // Should not happen in a valid game
    }

    // Checks if a square is attacked by a piece of the specified color.
    public boolean isSquareAttacked(Square square, int byColor) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = boardMatrix[i][j].getPiece();
                if (piece != null && piece.getColor() == byColor) {
                    try {
                        if (piece.canMove(square.getSquareSymbol())) {
                            return true;
                        }
                    } catch (InvalidLocationException e) {
                        // Ignore invalid moves
                    }
                }
            }
        }
        return false;
    }

    public boolean isMoveLegal(Square source, String target) {
        ChessBoard clonedBoard = new ChessBoard(this); // Deep copy
        Piece piece = clonedBoard.boardMatrix[source.getColumn()][source.getRow()].getPiece();
        // simulate the move
        try {
            piece.move(target);
        } catch (InvalidLocationException e) {
            System.out.println(e.getMessage());
        }
        if(piece instanceof Pawn && ((Pawn)piece).isPromoted) {
            clonedBoard.deletePromotedPawn(piece.getLocation());
        }
        return !clonedBoard.isKingInCheck(piece.getColor());
    }

    public boolean isDeadPosition() {
        // Count pieces and check for insufficient material
        int whitePieces = 0, blackPieces = 0;
        int whiteBishops = 0, blackBishops = 0;
        int whiteKnights = 0, blackKnights = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = boardMatrix[i][j].getPiece();
                if (piece != null) {
                    if (piece instanceof King)
                        continue;
                    if (piece.getColor() == WHITE) {
                        whitePieces++;
                        if (piece instanceof Bishop)
                            whiteBishops++;
                        if (piece instanceof Knight)
                            whiteKnights++;
                    } else {
                        blackPieces++;
                        if (piece instanceof Bishop)
                            blackBishops++;
                        if (piece instanceof Knight)
                            blackKnights++;
                    }
                }
            }
        }
        // Only kings
        if (whitePieces == 0 && blackPieces == 0)
            return true;
        // King + bishop or knight vs king
        if ((whitePieces == 1 && (whiteBishops == 1 || whiteKnights == 1) && blackPieces == 0) ||
                (blackPieces == 1 && (blackBishops == 1 || blackKnights == 1) && whitePieces == 0))
            return true;
        // King + bishop vs king + bishop (same color bishops)
        if (whitePieces == 1 && whiteBishops == 1 && blackPieces == 1 && blackBishops == 1) {
            // Optionally check if both bishops are on same color
            return true;
        }
        return false;
    }

    public boolean hasAnyLegalMove(int color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = boardMatrix[i][j].getPiece();
                if (piece != null && piece.getColor() == color) {
                    String from = boardMatrix[i][j].getSquareSymbol();
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            String to = boardMatrix[x][y].getSquareSymbol();
                            // Skip if source and target are the same
                            if (from.equals(to))
                                continue;
                            try {
                                if (piece.canMove(to) && isMoveLegal(piece.getLocation(), to)) {
                                    return true;
                                }
                            } catch (InvalidLocationException e) {
                                // Ignore invalid moves
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void nextPlayer() {
        currentPlayer = (WHITE + BLACK) - currentPlayer;
    }

    public boolean isWhitePlaying() {
        return currentPlayer == WHITE;
    }

    // game ends when no pieces exist from one color.
    public boolean isGameEnded() {
        return whiteCount == 0 || blackCount == 0;
    }

    public void deletePromotedPawn(Square location) {
        location.setPiece(null);
    }

    // prints the board
    @Override
    public String toString() {
        String b = "";
        // top of the board
        b += "    A   B   C   D   E   F   G   H\n";
        b += "   -------------------------------\n";
        // top to bottom enumerating: 8-1
        for (int j = 0; j < 8; j++) { // for every row
            b += (8 - j) + " "; // puts the number of the current row to the beginning of the row
            for (int i = 0; i < 8; i++) { // for every column
                Piece piece = boardMatrix[i][7 - j].getPiece();
                b += "|" + " " + (piece == null ? " " : piece) + " "; // prints the piece if there exists a piece
                                                                      // reference on that square, otherwise puts
                                                                      // spaces.
                if (i == 7)
                    b += "|"; // the bar end of the row
            }
            b += " " + (8 - j) + "\n"; // puts the number of the current row to end of the row
            b += "   -------------------------------\n";
        }

        b += "    A   B   C   D   E   F   G   H"; // bottom of the board
        return b;
    }

    public String printBoardMap() {
        return boardMap.toString();
    }

    

}
