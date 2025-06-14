package com.example.messaging_stomp_websocket;

import com.fasterxml.jackson.core.JsonProcessingException;

import chess.src.board.ChessBoard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {
    private final SimpMessagingTemplate template;
    private final GameRooms gameRooms; // holds the games

    @Autowired // assigns GameRooms and SimpMessagingTemplate singleton objects to fields above
    public GameController(GameRooms gameRooms, SimpMessagingTemplate template) {
        this.template = template;
        this.gameRooms = gameRooms;
    }

    @GetMapping("/") // GET Request -- when user refreshes the page or visits it for the first time
                     // returns the index, which contains buttons to select a side
    public String index() {
        return "index";
    }

    @MessageMapping("/addUser") // WebSocket STOMP endpoint -- after a side has been selected, a new user with
                                // preferred side is added to the game. If the preferred side is already chosen,
                                // new user becomes the opponent.
    @SendToUser("/queue/newpos")
    public Message addUser(Message message, @Header("simpSessionId") String sessionId)
            throws JsonProcessingException {
        if (gameRooms.addUserToGame(sessionId, message.orientation())) { // if the user does not exist with that
                                                                         // simpSessionId, that id is created
                                                                         // when a new
                                                                         // STOMP over WebSocket connection is
                                                                         // established
            System.out.println(gameRooms.games);
            System.out.println(gameRooms.users);
            return new Message(gameRooms.getOrientation(sessionId), gameRooms.users.get(sessionId).json,
                    "This is to " + sessionId); // user is informed and its table is oriented
                                                // according to this response
                                                // on the front-end side
        }
        return new Message("", "", "user_exists");
    }

    @MessageMapping("/addUserAI")
    public void addUserAI(Message message, @Header("simpSessionId") String sessionId)
            throws JsonProcessingException {

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        if (gameRooms.addUserToGameWithAI(sessionId, message.orientation())) {
            template.convertAndSendToUser(
                    sessionId,
                    "/queue/newpos",
                    new Message(gameRooms.getOrientation(sessionId),
                            gameRooms.users.get(sessionId).json,
                            "This is to " + sessionId),
                    headerAccessor.getMessageHeaders());

            System.out.println(gameRooms.games);
            System.out.println(gameRooms.users);
            GameService game = gameRooms.users.get(sessionId);
            // if ai is white, make the ai play first
            if (game.ai.aiColor == ChessBoard.WHITE) {
                if (game.makeAiMoveFirst()) {
                    if (game.isGameEnded()) {
                        String gameResult = game.gameEnded;
                        String winner = gameResult.equals("Checkmate")
                                ? game.board.getCurrentPlayer().equals("black")
                                        ? "white"
                                        : "black"
                                : "none";

                        template.convertAndSendToUser(
                                sessionId,
                                "/queue/gameState",
                                new Message("", game.json,
                                        gameResult + ", winner: " + winner),
                                SimpMessageHeaderAccessor
                                        .create(SimpMessageType.MESSAGE)
                                        .getMessageHeaders());
                    } else {
                        // send the new position to the user
                        template.convertAndSendToUser(
                                sessionId,
                                "/queue/pos",
                                new Message("", game.json,
                                        "New position. This is to "
                                                + sessionId),
                                headerAccessor.getMessageHeaders());
                    }
                }
            }

        }
        template.convertAndSendToUser(
                sessionId,
                "/queue/newpos",
                new Message("", "", "user_exists"),
                headerAccessor.getMessageHeaders()); // if the user already exists, it is informed
    }

    @MessageMapping("/validate") // WebSocket STOMP endpoint -- after a piece has been dragged and dropped on a
                                 // square, the method below tries to validate the move with the information sent
                                 // with the request body.
    public void validate(ChessMoveInformation moveInformation, @Header("simpSessionId") String sessionId)
            throws JsonProcessingException {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        GameService gameService = gameRooms.users.get(sessionId);

        // check if the user exists
        if (gameService == null) {
            template.convertAndSendToUser(
                    sessionId,
                    "/queue/gameState",
                    new Message("", "", "NOT_A_USER"),
                    headerAccessor.getMessageHeaders());
            return;
        }

        if (gameService.gameEnded != null || gameService.isGameEnded()) {
            System.out.println("Game has ended, not validating move: " + gameService.gameEnded);
            template.convertAndSendToUser(
                    sessionId,
                    "/queue/pos",
                    new Message("", gameService.json,
                            "Game has ended. This is to " + sessionId + ", winner: "
                                    + gameService.gameEnded),
                    headerAccessor.getMessageHeaders());

            // TODO: Terminate the game and connections
            String opponentId = gameRooms.deleteGame(sessionId);

            // Signal both users to disconnect
            template.convertAndSendToUser(
                    sessionId,
                    "/queue/disconnect",
                    "",
                    headerAccessor.getMessageHeaders());

            if (opponentId != null) {
                SimpMessageHeaderAccessor opponentHeaderAccessor = SimpMessageHeaderAccessor
                        .create(SimpMessageType.MESSAGE);
                opponentHeaderAccessor.setSessionId(opponentId);
                opponentHeaderAccessor.setLeaveMutable(true);

                template.convertAndSendToUser(
                        opponentId,
                        "/queue/disconnect",
                        "",
                        headerAccessor.getMessageHeaders());
            }

            return;
        }

        if (!gameRooms.validate(sessionId, moveInformation)) { // if invalid
            System.out.println("INVALID! " + sessionId);

            template.convertAndSendToUser( // inform the sender only
                    sessionId,
                    "/queue/pos",
                    new Message("", gameService.json,
                            "Invalid. This is to " + sessionId),
                    headerAccessor.getMessageHeaders());
        } else { // send the new position information to both sides

            System.out.println("VALID!");
            template.convertAndSendToUser(
                    sessionId,
                    "/queue/pos",
                    new Message("", gameService.json,
                            "New position. This is to " + sessionId),
                    headerAccessor.getMessageHeaders());

            String opponentId = gameRooms.getOpponent(sessionId);
            if (opponentId != null) {
                SimpMessageHeaderAccessor opponentHeaderAccessor = SimpMessageHeaderAccessor
                        .create(SimpMessageType.MESSAGE);
                opponentHeaderAccessor.setSessionId(opponentId);
                opponentHeaderAccessor.setLeaveMutable(true);

                template.convertAndSendToUser(
                        opponentId,
                        "/queue/pos",
                        new Message("", gameService.json,
                                "New position. This is to " + opponentId),
                        opponentHeaderAccessor.getMessageHeaders());
            }

            String gameResult = gameService.gameEnded;
            if (gameResult != null) {
                String winner = gameResult.equals("Checkmate")
                        ? gameRooms.getOrientation(sessionId).equals("black") ? "white"
                                : "black"
                        : "none";

                template.convertAndSendToUser(
                        sessionId,
                        "/queue/gameState",
                        new Message("", gameRooms.users.get(sessionId).json,
                                gameResult + ", winner: " + winner),
                        headerAccessor.getMessageHeaders());

                if (opponentId != null) {
                    SimpMessageHeaderAccessor opponentHeaderAccessor = SimpMessageHeaderAccessor
                            .create(SimpMessageType.MESSAGE);
                    opponentHeaderAccessor.setSessionId(opponentId);
                    opponentHeaderAccessor.setLeaveMutable(true);

                    template.convertAndSendToUser(
                            gameRooms.getOpponent(sessionId),
                            "/queue/gameState",
                            new Message("", gameRooms.users.get(sessionId).json,
                                    gameResult + ", winner: " + winner),
                            headerAccessor.getMessageHeaders());
                }

                // TODO: Terminate the game and connections
                opponentId = gameRooms.deleteGame(sessionId);

                // Signal both users to disconnect
                template.convertAndSendToUser(
                        sessionId,
                        "/queue/disconnect",
                        "",
                        headerAccessor.getMessageHeaders());

                if (opponentId != null) {
                    SimpMessageHeaderAccessor opponentHeaderAccessor = SimpMessageHeaderAccessor
                            .create(SimpMessageType.MESSAGE);
                    opponentHeaderAccessor.setSessionId(opponentId);
                    opponentHeaderAccessor.setLeaveMutable(true);

                    template.convertAndSendToUser(
                            opponentId,
                            "/queue/disconnect",
                            "",
                            headerAccessor.getMessageHeaders());
                }

            }
        }

    }
}