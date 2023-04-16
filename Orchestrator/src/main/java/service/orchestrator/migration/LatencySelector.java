package service.orchestrator.migration;

import service.orchestrator.properties.OrchestratorProperties;
import service.util.Debugger;
import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;
import java.util.UUID;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.nonNull;

public class LatencySelector implements Selector {
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
        NodeLatencyPair nodeLatencyPair = nodes.stream()
                .map(node -> new NodeLatencyPair(node, mobileClient.uuid))
                .min(naturalOrder())
                .orElse(null);


        return nonNull(nodeLatencyPair) ? nodeLatencyPair.node : null;
    }

    public ServiceNode mockSelect(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode badNode){
        Debugger.write("Inside Mock Latency Selector");
        OrchestratorProperties p = OrchestratorProperties.get();
        Debugger.write("Latency Target: " + p.getMaxLatency());
        double lowestAvgLatency = p.getMaxLatency();
        ServiceNode returnNode = null;
        for(ServiceNode node : nodes){
            if(!node.equals(badNode)){
                Debugger.write("Node Average Latency: "+ node.getAverageLatency());
                if(node.getAverageLatency() < lowestAvgLatency) {
                    lowestAvgLatency = node.getAverageLatency();
                    returnNode = node;
                }
            }
        }
        return returnNode;
    }

    private static class MockNodeLatencyPair implements Comparable<MockNodeLatencyPair> {
        private final double latency;
        ServiceNode node;

        MockNodeLatencyPair(ServiceNode node){
            this.node = node;
            this.latency = node.getAverageLatency();
        }
        @Override
        public int compareTo(MockNodeLatencyPair other) {
            return Double.compare(latency, other.latency);
        }
    }

    private static class NodeLatencyPair implements Comparable<NodeLatencyPair> {
        private final double latency;
        ServiceNode node;

        NodeLatencyPair(ServiceNode node, UUID clientUuid) {
            this.node = node;
            this.latency = node.getMeanLatency(clientUuid);
        }

        @Override
        public int compareTo(NodeLatencyPair other) {
            return Double.compare(latency, other.latency);
        }
    }
}
