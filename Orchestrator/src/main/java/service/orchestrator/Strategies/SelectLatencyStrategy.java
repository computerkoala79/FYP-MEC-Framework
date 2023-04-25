package service.orchestrator.Strategies;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.migration.Selector;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;
import service.util.Debugger;

import java.util.Collection;

public class SelectLatencyStrategy implements SelectStrategy {
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current) {
        Debugger.write("Inside Latency Strategy Selector");
        OrchestratorProperties p = OrchestratorProperties.get();
        Debugger.write("Latency Target: " + p.getMaxLatency());
        double lowestAvgLatency = p.getMaxLatency();
        ServiceNode returnNode = null;
        for(ServiceNode node : nodes){
            if(!node.equals(current)){
                Debugger.write("Node Average Latency: "+ node.getAverageLatency());
                if(node.getAverageLatency() < lowestAvgLatency) {
                    lowestAvgLatency = node.getAverageLatency();
                    returnNode = node;
                }
            }
        }
        return returnNode;
    }
}
