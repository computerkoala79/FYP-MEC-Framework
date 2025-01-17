package ie.ucd.mecframework.metrics.latency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

public class WebSocketPingClient extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketPingClient.class);
    private final PingTask pingTask;
    private final CountDownLatch finishedPingLatch;
    private final PingResult result = new PingResult();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();


    public WebSocketPingClient(URI serverUri, PingTask pingTask, CountDownLatch finishedPingLatch) {
        super(serverUri);
        this.pingTask = pingTask;
        this.finishedPingLatch = finishedPingLatch;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        Instant timeReceived = Instant.now();

        gson.fromJson(message, PingMessage.class);  // check if message was a PingMessage. Throw an error otherwise

        result.finishTime = timeReceived;
        pingTask.submitPingResult(result);
        finishedPingLatch.countDown();
        System.out.println("--------------------onMessage---------------");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("Finished. Closing. code={} reason={} remote?={}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error(ex.getMessage(), ex);
    }

    /**
     * Creates a record in this WebSocketPingClient's ping cache and sends a {@code PingMessage} to the server.
     */
    public void sendLoadedPing() {
        sendAndTime(new PingMessage());
    }

    private void sendAndTime(PingMessage message) {
        String json = gson.toJson(message);
        result.startTime = Instant.now();
        send(json);
    }
}
