package chess.src.ai;

import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.piece.Bishop;
import chess.src.piece.King;
import chess.src.piece.Knight;
import chess.src.piece.Pawn;
import chess.src.piece.Piece;
import chess.src.piece.Queen;
import chess.src.piece.Rook;
import chess.src.board.Square;

import java.util.ArrayList;
import java.util.List;

public class ChessAI {
    public final int aiColor; // AI's color (ChessBoard.WHITE or ChessBoard.BLACK)
    public final int maxDepth;

    public ChessAI(int aiColor, int maxDepth) {
        this.aiColor = aiColor;
        this.maxDepth = maxDepth;
    }

    // Entry point: returns the best move for the AI
    public MoveInformation getBestMove(ChessBoard board) {
        double bestValue = Double.NEGATIVE_INFINITY;
        MoveInformation bestMove = null;

        List<MoveInformation> legalMoves = generateAllLegalMoves(board, aiColor);

        for (MoveInformation move : legalMoves) {
            ChessBoard boardCopy = new ChessBoard(board); // Deep copy
            Piece piece = boardCopy.boardMatrix[move.location().getColumn()][move.location().getRow()].getPiece();
            // simulate the move
            try {
                piece.move(move.target());
            } catch (InvalidLocationException e) {
                System.out.println(e.getMessage());
            }
            if (piece instanceof Pawn && ((Pawn) piece).isPromoted) {
                boardCopy.deletePromotedPawn(piece.getLocation());
            }
            double value = minimax(boardCopy, maxDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // Minimax with alpha-beta pruning
    private double minimax(ChessBoard board, int depth, double alpha, double beta, boolean maximizingPlayer) {
        int currentColor = maximizingPlayer ? aiColor
                : (aiColor == ChessBoard.WHITE ? ChessBoard.BLACK : ChessBoard.WHITE);

        if (depth == 0 || !board.hasAnyLegalMove(currentColor) || board.isDeadPosition()) {
            return evaluateBoard(board);
        }

        List<MoveInformation> legalMoves = generateAllLegalMoves(board, currentColor);

        if (maximizingPlayer) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (MoveInformation move : legalMoves) {
                ChessBoard boardCopy = new ChessBoard(board); // Deep copy
                Piece piece = boardCopy.boardMatrix[move.location().getColumn()][move.location().getRow()].getPiece();
                // simulate the move
                try {
                    piece.move(move.target());
                } catch (InvalidLocationException e) {
                    System.out.println(e.getMessage());
                }
                if (piece instanceof Pawn && ((Pawn) piece).isPromoted) {
                    boardCopy.deletePromotedPawn(piece.getLocation());
                }

                double eval = minimax(boardCopy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (MoveInformation move : legalMoves) {
                ChessBoard boardCopy = new ChessBoard(board); // Deep copy
                Piece piece = boardCopy.boardMatrix[move.location().getColumn()][move.location().getRow()].getPiece();
                // simulate the move
                try {
                    piece.move(move.target());
                } catch (InvalidLocationException e) {
                    System.out.println(e.getMessage());
                }
                if (piece instanceof Pawn && ((Pawn) piece).isPromoted) {
                    boardCopy.deletePromotedPawn(piece.getLocation());
                }

                double eval = minimax(boardCopy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }

    /* // Simple evaluation function (material only)
    private double evaluateBoard(ChessBoard board) {
        double score = 0.0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.boardMatrix[i][j].getPiece();
                if (piece != null) {
                    double value = getPieceValue(piece);
                    score += (piece.getColor() == aiColor) ? value : -value;
                }
            }
        }
        return score;
    } */

    private double getPieceValue(Piece piece) {
        if (piece instanceof Pawn) {
            return 1.0;
        } else if (piece instanceof Knight) {
            return 3.0;
        } else if (piece instanceof Bishop) {
            return 3.0;
        } else if (piece instanceof Rook) {
            return 5.0;
        } else if (piece instanceof Queen) {
            return 9.0;
        } else if (piece instanceof King) {
            return 100.0;
        } else {
            return 0.0;
        }
    }

    // Generate all legal moves for the given color
    private List<MoveInformation> generateAllLegalMoves(ChessBoard board, int color) {
        List<MoveInformation> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.boardMatrix[i][j].getPiece();
                if (piece != null && piece.getColor() == color) {
                    String from = board.boardMatrix[i][j].getSquareSymbol();
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            String to = board.boardMatrix[x][y].getSquareSymbol();
                            // Skip if source and target are the same
                            if (from.equals(to))
                                continue;
                            try {
                                if (piece.canMove(to) && isMoveLegal(piece.getLocation(), to)) {
                                    moves.add(new MoveInformation(piece.getLocation(), to));
                                }
                            } catch (InvalidLocationException e) {
                                // Ignore invalid moves
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private boolean isMoveLegal(Square source, String target) {
        ChessBoard clonedBoard = new ChessBoard(source.getBoard()); // Deep copy
        Piece piece = clonedBoard.boardMatrix[source.getColumn()][source.getRow()].getPiece();
        // simulate the move
        try {
            piece.move(target);
        } catch (InvalidLocationException e) {
            System.out.println(e.getMessage());
        }
        if (piece instanceof Pawn && ((Pawn) piece).isPromoted) {
            clonedBoard.deletePromotedPawn(piece.getLocation());
        }

        return !clonedBoard.isKingInCheck(piece.getColor());
    }

    // Piece-square tables (example for pawns and knights, expand as needed)
    private static final double[][] PAWN_TABLE = {
            { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
            { 0.5, 1.0, 1.0, -2.0, -2.0, 1.0, 1.0, 0.5 },
            { 0.5, -0.5, -1.0, 0.0, 0.0, -1.0, -0.5, 0.5 },
            { 0.0, 0.0, 0.0, 2.0, 2.0, 0.0, 0.0, 0.0 },
            { 0.5, 0.5, 1.0, 2.5, 2.5, 1.0, 0.5, 0.5 },
            { 1.0, 1.0, 2.0, 3.0, 3.0, 2.0, 1.0, 1.0 },
            { 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0 },
            { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }
    };
    private static final double[][] KNIGHT_TABLE = {
            { -5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0 },
            { -4.0, -2.0, 0.0, 0.0, 0.0, 0.0, -2.0, -4.0 },
            { -3.0, 0.0, 1.0, 1.5, 1.5, 1.0, 0.0, -3.0 },
            { -3.0, 0.5, 1.5, 2.0, 2.0, 1.5, 0.5, -3.0 },
            { -3.0, 0.0, 1.5, 2.0, 2.0, 1.5, 0.0, -3.0 },
            { -3.0, 0.5, 1.0, 1.5, 1.5, 1.0, 0.5, -3.0 },
            { -4.0, -2.0, 0.0, 0.5, 0.5, 0.0, -2.0, -4.0 },
            { -5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0 }
    };
    // Add more tables for other pieces if desired

    private double evaluateBoard(ChessBoard board) {
        double score = 0.0;
        int myMobility = 0, oppMobility = 0;
        int myDevelopment = 0, oppDevelopment = 0;
        int myCenterControl = 0, oppCenterControl = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.boardMatrix[i][j].getPiece();
                if (piece != null) {
                    double value = getPieceValue(piece);

                    // Piece-square table bonus
                    value += getPieceSquareBonus(piece, i, j);

                    // Pawn structure
                    if (piece instanceof Pawn) {
                        value += getPawnStructureBonus(board, i, j, piece.getColor());
                    }

                    // King safety
                    if (piece instanceof King) {
                        value += getKingSafetyBonus(board, i, j, piece.getColor());
                    }

                    // Mobility
                    int mobility = countLegalMoves(board, piece);
                    if (piece.getColor() == aiColor)
                        myMobility += mobility;
                    else
                        oppMobility += mobility;

                    // Development (minor pieces off back rank)
                    if ((piece instanceof Knight || piece instanceof Bishop) &&
                            ((piece.getColor() == ChessBoard.WHITE && j != 0) ||
                                    (piece.getColor() == ChessBoard.BLACK && j != 7))) {
                        if (piece.getColor() == aiColor)
                            myDevelopment++;
                        else
                            oppDevelopment++;
                    }

                    // Center control (e4, d4, e5, d5)
                    String sq = board.boardMatrix[i][j].getSquareSymbol();
                    if (sq.equals("e4") || sq.equals("d4") || sq.equals("e5") || sq.equals("d5")) {
                        if (piece.getColor() == aiColor)
                            myCenterControl++;
                        else
                            oppCenterControl++;
                    }

                    score += (piece.getColor() == aiColor) ? value : -value;
                }
            }
        }

        // Add mobility, development, and center control differences
        score += 0.1 * (myMobility - oppMobility);
        score += 0.2 * (myDevelopment - oppDevelopment);
        score += 0.3 * (myCenterControl - oppCenterControl);

        return score;
    }

    // Piece-square table bonus
    private double getPieceSquareBonus(Piece piece, int x, int y) {
        // Flip table for black
        int row = (piece.getColor() == ChessBoard.WHITE) ? y : 7 - y;
        int col = x;
        if (piece instanceof Pawn) {
            return PAWN_TABLE[row][col] * 0.1;
        } else if (piece instanceof Knight) {
            return KNIGHT_TABLE[row][col] * 0.1;
        }
        // Add more for other pieces if desired
        return 0.0;
    }

    // Pawn structure heuristics
    private double getPawnStructureBonus(ChessBoard board, int x, int y, int color) {
        double bonus = 0.0;
        // Isolated pawn
        boolean isolated = true;
        for (int dx = -1; dx <= 1; dx += 2) {
            int nx = x + dx;
            if (nx >= 0 && nx < 8) {
                for (int j = 0; j < 8; j++) {
                    Piece neighbor = board.boardMatrix[nx][j].getPiece();
                    if (neighbor instanceof Pawn && neighbor.getColor() == color) {
                        isolated = false;
                        break;
                    }
                }
            }
        }
        if (isolated)
            bonus -= 0.2;

        // Doubled pawn
        int count = 0;
        for (int j = 0; j < 8; j++) {
            Piece p = board.boardMatrix[x][j].getPiece();
            if (p instanceof Pawn && p.getColor() == color)
                count++;
        }
        if (count > 1)
            bonus -= 0.2;

        // Passed pawn
        boolean passed = true;
        int dir = (color == ChessBoard.WHITE) ? 1 : -1;
        for (int nx = x - 1; nx <= x + 1; nx++) {
            if (nx < 0 || nx > 7)
                continue;
            for (int j = y + dir; (color == ChessBoard.WHITE ? j < 8 : j >= 0); j += dir) {
                Piece p = board.boardMatrix[nx][j].getPiece();
                if (p instanceof Pawn && p.getColor() != color) {
                    passed = false;
                    break;
                }
            }
        }
        if (passed)
            bonus += 0.3;

        return bonus;
    }

    // King safety heuristics
    private double getKingSafetyBonus(ChessBoard board, int x, int y, int color) {
        double bonus = 0.0;
        // Penalize king with no pawns nearby
        int pawnShield = 0;
        int dir = (color == ChessBoard.WHITE) ? 1 : -1;
        for (int dx = -1; dx <= 1; dx++) {
            int nx = x + dx;
            int ny = y + dir;
            if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                Piece p = board.boardMatrix[nx][ny].getPiece();
                if (p instanceof Pawn && p.getColor() == color)
                    pawnShield++;
            }
        }
        if (pawnShield == 0)
            bonus -= 0.5;
        else if (pawnShield == 1)
            bonus -= 0.2;
        else if (pawnShield == 2)
            bonus += 0.1;
        else if (pawnShield == 3)
            bonus += 0.2;

        // Penalize king in the center in the opening
        if ((x >= 2 && x <= 5) && (y >= 2 && y <= 5))
            bonus -= 0.2;

        return bonus;
    }

    // Mobility: count legal moves for a piece
    private int countLegalMoves(ChessBoard board, Piece piece) {
        int count = 0;
        String from = piece.getLocation().getSquareSymbol();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                String to = board.boardMatrix[x][y].getSquareSymbol();
                if (from.equals(to))
                    continue;
                try {
                    if (piece.canMove(to) && isMoveLegal(piece.getLocation(), to)) {
                        count++;
                    }
                } catch (InvalidLocationException e) {
                    // Ignore
                }
            }
        }
        return count;
    }
}