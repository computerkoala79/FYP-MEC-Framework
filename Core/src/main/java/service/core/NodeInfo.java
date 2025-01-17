package service.core;

import org.java_websocket.WebSocket;
import service.util.Debugger;

import java.net.InetAddress;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

// todo remove unnecessary fields
public class NodeInfo extends Message {
    private UUID uuid;
    private WebSocket webSocket;
    private boolean serviceRunning;
    private boolean serviceInstalled;
    private List<Double> cpuLoad = Collections.emptyList();
    private List<Double> memoryLoad = Collections.emptyList();
    private List<Long> mainMemory = Collections.emptyList();
    private List<Long> storage = Collections.emptyList();
    private Map<UUID, List<Long>> latencies = Collections.emptyMap();
    private URI serviceHostAddress;

    // todo Only reason why this is here is so that the Orchestrator can update globalIpAddress in its
    //  ServiceNodeRegistry
    //  Not a good enough reason to include it as a field in a message shared between hosts. Remove this field.
    private InetAddress globalIpAddress;

    public NodeInfo() {
        super(Message.MessageTypes.NODE_INFO);
    }

    public NodeInfo(UUID uuid, boolean serviceRunning, URI serviceUri) {
        this();
        this.uuid = uuid;
        this.serviceRunning = serviceRunning;
        this.serviceHostAddress = serviceUri;
    }

    private static String mapToString(Map<?, ?> map) {
        String mapContents = map.entrySet().stream()
                .map(e -> new StringBuilder().append(e.getKey()).append(": ").append(e.getValue()).append(' '))
                .reduce(new StringBuilder(), StringBuilder::append)
                .toString();
        return "{ " + mapContents + " }";
    }

    public Map<UUID, List<Long>> getLatencies() {
        return latencies;
    }

    public void setLatencies(Map<UUID, List<Long>> latencies) {
        Collection<List<Long>> l = latencies.values();

        this.latencies = latencies;
    }

    public double getAverageLatency(){
        StringBuilder s = new StringBuilder();
        s.append("Average Latency: ");
        Long[] latencyArray = latencies.values().stream().flatMap(List::stream).toArray(Long[]::new);
        double count = latencyArray.length;
        long totalLatency = 0;
        for(long latency : latencyArray){
            totalLatency += latency;
            s.append(latency + " ");
        }
        Debugger.write(s.toString());
        return totalLatency / count;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public boolean isServiceRunning() {
        return serviceRunning;
    }

    public boolean isServiceInstalled() {
        return serviceInstalled;
    }

    public void setServiceInstalled(boolean serviceInstalled) {
        this.serviceInstalled = serviceInstalled;
    }

    public UUID getUuid() {
        return uuid;
    }

    public URI getServiceHostAddress() {
        return serviceHostAddress;
    }

    public List<Double> getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(List<Double> cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public List<Double> getMemoryLoad() {
        return memoryLoad;
    }

    public void setMemoryLoad(List<Double> memoryLoad) {
        this.memoryLoad = memoryLoad;
    }

    public List<Long> getMainMemory() {
        return mainMemory;
    }

    public void setMainMemory(List<Long> mainMemory) {
        this.mainMemory = mainMemory;
    }

    public List<Long> getStorage() {
        return storage;
    }

    public void setStorage(List<Long> storage) {
        this.storage = storage;
    }

    public InetAddress getGlobalIpAddress() {
        return globalIpAddress;
    }

    public void setGlobalIpAddress(InetAddress globalIpAddress) {
        this.globalIpAddress = globalIpAddress;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "uuid=" + uuid +
                ", servicePort=" + serviceHostAddress.getPort() +
                ", serviceRunning=" + serviceRunning +
                ", serviceInstalled=" + serviceInstalled +
                ", cpuLoad=" + cpuLoad +
                ", memoryLoad=" + memoryLoad +
                ", mainMemory=" + mainMemory +
                ", storage=" + storage +
                ", latencies=" + mapToString(latencies) +
                '}';
    }

}
