package ie.ucd.clientnode.websocket;


import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.core.*;
import service.util.Gsons;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

public class CommandLineWsClient extends WebSocketClient {
    private InetSocketAddress pingServer;
    private static Logger logger = LoggerFactory.getLogger(CommandLineWsClient.class);

    private UUID assignedUUID;
    private AtomicReference<URI> cloudService = new AtomicReference<>();
    private ExecutorService hostRequestScheduler = Executors.newSingleThreadExecutor();
    private Gson gson;
    private String serviceName;
    private int latency;

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attempt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public CommandLineWsClient(URI serverUri, String serviceName, int latency, InetSocketAddress pingServer) {
        super(serverUri);
        gson = Gsons.mobileClientGson();
        this.serviceName = serviceName;
        this.latency = latency;
        this.pingServer = pingServer;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.debug("Connection opened to server at " + getConnection().getRemoteSocketAddress());
        hostRequestScheduler.submit(
                this::requestApplicationHost
        );
    }

    @Override
    public void onMessage(String message) {
        Message messageObj = gson.fromJson(message, Message.class);
        logger.debug("Received {}", messageObj);

        switch (messageObj.getType()) {
            case Message.MessageTypes.HEARTBEAT_REQUEST:
                handleServerHeartbeatRequest();
                break;
            case Message.MessageTypes.NODE_INFO_REQUEST:
                handleNodeInfoRequest((NodeInfoRequest) messageObj);
                sendMobileClientInfo();
                break;
            case Message.MessageTypes.HOST_RESPONSE:
                handleHostResponse((HostResponse) messageObj);
                break;
            case Message.MessageTypes.MIGRATION_SUCCESS:
                break;
            case Message.MessageTypes.MIGRATION_ALERT:
                break;
            default:
                logger.warn("Unrecognised message type {}", messageObj.getType());
        }
    }

    //Recieve heartbeat request, return node info
    private void handleServerHeartbeatRequest() {
        if (nonNull(assignedUUID)) {
            sendMobileClientInfo();
        }
    }

    private void handleNodeInfoRequest(NodeInfoRequest request) {
        assignedUUID = request.getUuid();
    }

    public void sendMobileClientInfo() {
        MobileClientInfo info = new MobileClientInfo();
//        MobileClientInfo info = new MobileClientInfo(assignedUUID, pingServer, serviceName, latency);
        sendAsJson(info);
    }

    private void requestApplicationHost() {
        if(cloudService.get()==null){
            logger.info("Request application host at : " + System.currentTimeMillis());
            HostRequest serviceRequest = new HostRequest(assignedUUID);
            sendAsJson(serviceRequest);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestApplicationHost();
        }
    }

    public void handleHostResponse(HostResponse response) {
        URI currentService = getCloudService();
        URI newService = response.getServiceHostAddress();

        if(currentService == null){
            logger.info("Assigned service URI : " + newService);
        }

        if (nonNull(currentService) && !currentService.equals(newService)) {
            logger.info("ServiceHostAddress : " + newService);
            logger.info("Switched to new service provider : " + System.currentTimeMillis());
            logger.debug("New Service Host Address! {} -> {}", currentService, newService);
        }
        setCloudService(newService);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        hostRequestScheduler.shutdown();
        logger.debug("Closing the WebSocketClient: ");
        logger.debug("hostRequestScheduler Shutdown? {} Terminated? {}",
                hostRequestScheduler.isShutdown(), hostRequestScheduler.isTerminated());
        logger.debug("code: " + code);
        logger.debug("reason: " + reason);
        logger.debug("remote: " + remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error(ex.getMessage());
    }

    /**
     * Gets the most recent service {@code URI} allocated to this client by the Orchestrator.
     * <p>
     * There is no guarantee that the URI will be non-null, nor that the URI points to a running service.
     * </p>
     *
     * @return the service's {@code URI} if one has been allocated thusfar, otherwise null.
     */
    public URI getCloudService() {
        return cloudService.get();
    }

    public AtomicReference<URI> getCloudServiceReference() { return cloudService; }

    public UUID getAssignedUUID() {
        return this.assignedUUID;
    }

    private void setCloudService(URI uri) {
        cloudService.set(uri);
    }

    public void sendAsJson(Message message) {
        logger.debug("Sending: {}", message);
        send(gson.toJson(message));
    }
}
