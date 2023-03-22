package ie.ucd.clientnode.websocket;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

//For returning the ping sent by the service node to calculate latency
public class PingServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(PingServer.class);

    public PingServer(InetSocketAddress address) {
        super(address);
        logger.debug("Starting ping server on {}", address);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake handshake) {
        logger.debug("PingServer has client {}", webSocket.getRemoteSocketAddress());
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        logger.debug("Just got a ping.");
        // just pong it back
        connection.send(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
        logger.debug(" on start ");
    }
}