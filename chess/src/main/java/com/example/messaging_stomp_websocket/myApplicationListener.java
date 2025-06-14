package com.example.messaging_stomp_websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class myApplicationListener implements ApplicationListener {
    SimpMessagingTemplate template;
    GameRooms gameRooms;

    @Autowired
    public myApplicationListener(GameRooms gameRooms, SimpMessagingTemplate template) {
        this.gameRooms = gameRooms;
        this.template = template;
    }

    // if a user terminates a WebSocket connection, an event is fired and this
    // onApplicationEvent handles it by deleting the game associated with this user.
    // the opponent is informed
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof SessionDisconnectEvent) {
            String sessionId = ((SessionDisconnectEvent) event).getSessionId();
            System.out.println(sessionId + "has disconnected");
            if (gameRooms.users.get(sessionId) == null) {
                System.out.println(gameRooms.games);
                System.out.println(gameRooms.users);
                return;
            }
            String opponent = gameRooms.deleteGame(sessionId);
            if (opponent != null) {
                SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
                headerAccessor.setSessionId(opponent);
                headerAccessor.setLeaveMutable(true);

                template.convertAndSendToUser(
                        opponent,
                        "/queue/gameState",
                        new Message("", "", "OPPONENT_LEFT"),
                        headerAccessor.getMessageHeaders());
            }
            System.out.println(gameRooms.games);
            System.out.println(gameRooms.users);
        }
    }
}
