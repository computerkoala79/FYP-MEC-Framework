package service.orchestrator.migration;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;
import service.util.Debugger;

import java.util.Collection;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.nonNull;

public class MainMemorySelector implements Selector {
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
        OrchestratorProperties properties = OrchestratorProperties.get();

//        NodeMemory nodeCpuPair = nodes.stream()
//                .map(NodeMemory::new)
//                .filter(mem -> mem.memoryAmount > properties.getMinMemoryGibibytes())
//                .min(naturalOrder())
//                .orElse(null);
//        return nonNull(nodeCpuPair) ? nodeCpuPair.node : null;

        NodeMemory nodeCpuPair = nodes.stream()
                .map(NodeMemory::new)
                .filter(mem -> mem.getMemoryUtilization() < properties.getMaxMemory())
                .min(naturalOrder())
                .orElse(null);
        return nonNull(nodeCpuPair) ? nodeCpuPair.node : null;
    }

    @Override
    public ServiceNode mockSelect(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode badNode) {
//        ServiceNode serviceNode = select(nodes,mobileClient);
        ServiceNode serviceNode = null;
        double lowest_mem = 1.0;
        for(ServiceNode node : nodes){
            if(!node.equals(badNode)){
                double freeMem_node = node.ramLoad.get(0);
                if(freeMem_node < lowest_mem) {
                    serviceNode = node;
                    lowest_mem = freeMem_node;
                }
            }
        }
        if(serviceNode.equals(badNode)) return null;
        return serviceNode;
    }

    private static class NodeMemory implements Comparable<NodeMemory> {
        final ServiceNode node;
        private final double memoryUtilization;
        private final double memoryAmount;

        NodeMemory(ServiceNode node) {
            this.node = node;
            this.memoryUtilization = 1 - node.getMainMemoryPercentFree();
            this.memoryAmount = node.getMainMemoryInGibibytes();
        }

        // getters for testing
        public double getMemoryAmount() {
            return memoryAmount;
        }

        public double getMemoryUtilization() {
            return memoryUtilization;
        }

        @Override
        public int compareTo(NodeMemory other) {
            return Double.compare(memoryUtilization, other.memoryUtilization);
        }
    }
}
