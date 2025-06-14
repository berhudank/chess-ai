package com.example.messaging_stomp_websocket;

import chess.src.ai.*;
import chess.src.board.ChessBoard;
import chess.src.board.InvalidLocationException;
import chess.src.piece.Pawn;
import chess.src.piece.Piece;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameService {

    public String white, black; // these strings hold the simpSessionIds of the users
    public boolean playerVsAi;

    public AtomicInteger counter = new AtomicInteger(); // for debugging
    protected ChessBoard board = new ChessBoard();
    //protected ChessBoard board = new ChessBoard("8/8/8/8/4b3/5k2/7B/6K1 w - - 0 1");
    public String json = new ObjectMapper().writeValueAsString(board.boardMap);
    public String gameEnded = null;

    public ChessAI ai;

    public GameService() throws JsonProcessingException {
    }

    public GameService(String black, String white) throws JsonProcessingException {
        this.black = black;
        this.white = white;
    }

    public GameService(int color, String sessionId) throws JsonProcessingException {
        if (color == ChessBoard.WHITE) {
            this.white = sessionId;
            this.black = null;
        } else if (color == ChessBoard.BLACK) {
            this.white = null;
            this.black = sessionId;
        }
        this.playerVsAi = true;
        if (playerVsAi) {
            this.ai = new ChessAI(color == ChessBoard.BLACK ? ChessBoard.WHITE : ChessBoard.BLACK, 3);
        }
    }

    public boolean isFull() {
        return white != null && black != null;
    }

    private void updateJson() throws JsonProcessingException {
        this.json = new ObjectMapper().writeValueAsString(board.boardMap);
    }

    public boolean isGameEnded() {
        int currentPlayer = board.isWhitePlaying() ? ChessBoard.WHITE : ChessBoard.BLACK;

        if (!board.hasAnyLegalMove(currentPlayer)) {
            if (board.isKingInCheck(currentPlayer))
                gameEnded = "Checkmate";
            else
                gameEnded = "Stalemate";
            return true;

        } else {
            if (board.isDeadPosition()) {
                gameEnded = "Dead Position";
                return true;
            }
            return false;
        }
    }

    public boolean makeAiMoveFirst() throws JsonProcessingException {
        if (this.playerVsAi && this.ai != null) {
            MoveInformation aiMove = ai.getBestMove(board);
            Piece piece = board.boardMatrix[aiMove.location().getColumn()][aiMove.location().getRow()].getPiece();
            try {
                piece.move(aiMove.target());
            } catch (InvalidLocationException ile) {
                System.out.println(ile.getMessage());
            }
            if(piece instanceof Pawn && ((Pawn)piece).isPromoted) {
                board.deletePromotedPawn(piece.getLocation());
            }
            updateJson();
            System.out.println("AI moved: " + aiMove.location().getPiece() + " to " + aiMove.target());
            return true;
        }
        return false;
    }

    public boolean validateAndMove(ChessMoveInformation move, String orientation) throws JsonProcessingException {
        // all game logic runs here
        if (!board.getCurrentPlayer().equals(orientation))
            return false;

        Piece piece = null;
        boolean invalidLocation = false;
        try {
            piece = board.getPieceAt(move.source());
        } catch (InvalidLocationException ile) {
            invalidLocation = true;
            System.out.println(ile.getMessage());
        }
        if (invalidLocation || piece == null)
            return false;

        boolean canMove = false;
        try {
            canMove = piece.canMove(move.target());
        } catch (InvalidLocationException ile) {
            System.out.println(ile.getMessage());
            invalidLocation = true;
        }
        if (invalidLocation || !canMove)
            return false;

        if (!board.isMoveLegal(piece.getLocation(), move.target())) {
            System.out.println("King in check, move is not allowed");
            return false;
        }

        try {
            piece.move(move.target());
        } catch (InvalidLocationException ile) {
            System.out.println(ile.getMessage());
        }
        if(piece instanceof Pawn && ((Pawn)piece).isPromoted) {
            board.deletePromotedPawn(piece.getLocation());
        }

        updateJson();
        int nextPlayer = board.isWhitePlaying() ? ChessBoard.WHITE : ChessBoard.BLACK;

        if (!board.hasAnyLegalMove(nextPlayer)) {
            if (board.isKingInCheck(nextPlayer))
                gameEnded = "Checkmate";
            else
                gameEnded = "Stalemate";
            return true;
        } else if (board.isDeadPosition()) {
            gameEnded = "Dead Position";
            return true;
        }

        if (this.playerVsAi && nextPlayer == ai.aiColor) {
            MoveInformation aiMove = ai.getBestMove(board);
            piece = board.boardMatrix[aiMove.location().getColumn()][aiMove.location().getRow()].getPiece();
            try {
                piece.move(aiMove.target());
            } catch (InvalidLocationException ile) {
                System.out.println(ile.getMessage());
            }
            if(piece instanceof Pawn && ((Pawn)piece).isPromoted) {
                board.deletePromotedPawn(piece.getLocation());
            }
            updateJson();
            System.out.println("AI moved: " + aiMove.location().getPiece() + " to " + aiMove.target());

            nextPlayer = board.isWhitePlaying() ? ChessBoard.WHITE : ChessBoard.BLACK;

            if (!board.hasAnyLegalMove(nextPlayer)) {
                if (board.isKingInCheck(nextPlayer))
                    gameEnded = "Checkmate";
                else
                    gameEnded = "Stalemate";
            
            } else if (board.isDeadPosition()) {
                gameEnded = "Dead Position";
                
            }

        }
        return true;
    }
}
