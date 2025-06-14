package com.example.messaging_stomp_websocket;

public record ChessMoveInformation(String source, String target, String piece, String oldPos, String orientation){}