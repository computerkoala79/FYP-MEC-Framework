package service.orchestrator.migration;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;
import service.util.Debugger;

import java.util.Collection;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CpuSelector implements Selector {
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
        NodeCpuPair nodeCpuPair = nodes.stream()
                .map(NodeCpuPair::new)
                .min(naturalOrder())
                .orElse(null);
        return nonNull(nodeCpuPair) ? nodeCpuPair.node : null;
    }

    @Override
    public ServiceNode mockSelect(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode badNode) {
//        ServiceNode serviceNode = select(nodes,mobileClient);
        ServiceNode serviceNode = null;
        double lowest = 1.0;
        for(ServiceNode node : nodes){
            if(!node.equals(badNode)){
                double node_cpu = node.cpuLoad.get(0);
                Debugger.write("CPU Load: " + node_cpu);
                if(node_cpu < lowest){
                    serviceNode = node;
                    lowest = node_cpu;
                }
            }
        }
        if(serviceNode.equals(badNode)) return null;
        return serviceNode;
    }

    private static class NodeCpuPair implements Comparable<NodeCpuPair> {
        private final double cpu;
        final ServiceNode node;

        NodeCpuPair(ServiceNode node) {
            this.node = node;
            this.cpu = node.getCpuScore();
        }

        @Override
        public int compareTo(NodeCpuPair other) {
            return Double.compare(cpu, other.cpu);
        }
    }
}
