package service.edge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import service.core.*;
import service.edge.transferServices.TransferClient;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class Edge extends WebSocketClient {

    SystemInfo nodeSystem = new SystemInfo();
    DockerController dockerController;
    private File service;
    private UUID assignedUUID;

    public Edge(URI serverUri){//, File service) {
        super(serverUri);
        dockerController=new DockerController();
        //this.service = service;//service is stored in edge node
    }

    public static void main(String[] args) {

    }

    public void serviceRequestor() {
        Gson gson = new Gson();

        ServiceRequest serviceRequest = new ServiceRequest(assignedUUID,"docker.tar");//atm assumes there is only 1 service and leaves it up to orchestrator to find it
        System.out.println(serviceRequest.getType());
        String jsonStr = gson.toJson(serviceRequest);
        for(int i=0;i<10000;i++){

        }
        send(jsonStr);

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("connected to orchestrator");
        System.out.println(Edge.this.getLocalSocketAddress());//this is the local address in theory
    }

    @Override
    public void onMessage(String message) {
        RuntimeTypeAdapterFactory<Message> adapter = RuntimeTypeAdapterFactory
                .of(Message.class, "type")
                .registerSubtype(NodeInfo.class, Message.MessageTypes.NODE_INFO)
                .registerSubtype(Service.class, Message.MessageTypes.SERVICE)
                .registerSubtype(ServiceRequest.class, Message.MessageTypes.SERVICE_REQUEST)
                .registerSubtype(ServiceResponse.class, Message.MessageTypes.SERVICE_RESPONSE)
                .registerSubtype(NodeInfoRequest.class, Message.MessageTypes.NODE_INFO_REQUEST);

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();

        Message messageObj = gson.fromJson(message, Message.class);

        System.out.println(messageObj.getType());
        System.out.println(message);

        //this routes inbound messages based on type and then moves them to other methods
        switch (messageObj.getType()) {
            case Message.MessageTypes.NODE_INFO_REQUEST:
                NodeInfoRequest infoRequest = (NodeInfoRequest) messageObj;
                assignedUUID = infoRequest.getAssignedUUID();
                gson = new Gson();
                NodeInfo nodeInfo = new NodeInfo(assignedUUID, nodeSystem ,null);
                String jsonStr = gson.toJson(nodeInfo);
                send(jsonStr);
                serviceRequestor();
                break;
            case Message.MessageTypes.SERVICE_REQUEST:
                gson = new Gson();
                Service serviceToReturn = new Service(assignedUUID, service);
                jsonStr = gson.toJson(serviceToReturn);
                send(jsonStr);
                break;
            case Message.MessageTypes.SERVICE_RESPONSE:
                //this gives the proxy address we want
                ServiceResponse response = (ServiceResponse) messageObj;
                System.out.println(response);
                System.out.println(response.getServiceOwnerAddress());

                try {
                    launchTransferClient(response.getServiceOwnerAddress());

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void launchTransferClient(String serverAddress) throws URISyntaxException {
        System.out.println("GOT HERE");
        System.out.println(serverAddress);
        TransferClient transferClient = new TransferClient(new URI("ws://localhost:6969"),dockerController);
        transferClient.connect();

    }

    public void getSystemSpecs() {
        HardwareAbstractionLayer hal = nodeSystem.getHardware();
        System.out.println("processor" + hal.getProcessor().toString());
    }


    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}