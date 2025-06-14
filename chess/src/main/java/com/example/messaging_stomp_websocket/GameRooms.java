package com.example.messaging_stomp_websocket;

import com.fasterxml.jackson.core.JsonProcessingException;

import chess.src.board.ChessBoard;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameRooms {
    Map<String, GameService> users = new HashMap<>(); // map each user to its game room (GameService)
    ArrayList<GameService> games = new ArrayList<>(); // games are added and deleted continuously

    public boolean validate(String sessionId, ChessMoveInformation move) throws JsonProcessingException {
        GameService game = users.get(sessionId);
        if(game != null){
            if(game.playerVsAi || game.isFull()){
                return game.validateAndMove(move, ( sessionId.equals(game.white) ? "white" : "black" ) );
            }
        }
        return false;
    }

    public String getOpponent(String sessionId){
        return switch (getOrientation(sessionId)) {
            case "white" -> users.get(sessionId).black;
            case "black" -> users.get(sessionId).white;
            default -> null;
        };
    }

    public String getOrientation(String sessionId){
        GameService game = users.get(sessionId);
        if(game != null){
            return sessionId.equals(game.white) ? "white" : "black";
        }
        return null;
    }

    public boolean addUserToGameWithAI(String sessionId, String color) throws JsonProcessingException {
        if(!users.containsKey(sessionId)){
            GameService newGame;
            int playerColor = color.equals("white") ? ChessBoard.WHITE : ChessBoard.BLACK;
            newGame = new GameService(playerColor, sessionId);
            games.add(newGame);
            users.put(sessionId,newGame);
            System.out.println(sessionId + " has been added!");
            return true;
        }
        System.out.println("User already exists");
        return false;
    }

    public boolean addUserToGame(String sessionId, String color) throws JsonProcessingException {
        if(!users.containsKey(sessionId)){
            boolean isPut = false;
            for (GameService g : games){
                if(!g.isFull() && !g.playerVsAi){
                    switch (color){
                        case "white":
                            if(g.white == null)
                                g.white = sessionId;
                            else
                                g.black = sessionId;
                            break;
                        case "black":
                            if(g.black == null)
                                g.black = sessionId;
                            else
                                g.white = sessionId;
                    }
                    users.put(sessionId, g);
                    isPut = true;
                }
            }
            if (!isPut){
                GameService newGame;
                if(color.equals("white"))
                    newGame = new GameService(null, sessionId);
                else
                    newGame = new GameService(sessionId,null);
                games.add(newGame);
                users.put(sessionId,newGame);
            }
            System.out.println(sessionId + " has been added!");
            return true;
        }
        System.out.println("User already exists");
        return false;
    }

    // when a user establishes a new STOMP over WebSocket connection, this method is called.
    // disconnected user's and its opponent's simpSessionIds are deleted from the map and also the game room is deleted from the ArrayList.

    public String deleteGame(String sessionId){
        GameService game = users.get(sessionId);
        String opponent = getOpponent(sessionId);
        if(opponent != null){
            users.remove(opponent);
        }
        users.remove(sessionId);
        games.remove(game);
        return opponent;

    }

}
