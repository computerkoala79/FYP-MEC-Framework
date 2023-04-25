package service.orchestrator.Strategies;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;
import service.util.Debugger;

import java.util.Collection;

public class SelectCPUStrategy implements SelectStrategy{
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current) {
        ServiceNode serviceNode = null;
        double lowest = 1.0;
        for(ServiceNode node : nodes){
            if(!node.equals(current)){
                double node_cpu = node.cpuLoad.get(0);
                Debugger.write("CPU Load: " + node_cpu);
                if(node_cpu < lowest){
                    serviceNode = node;
                    lowest = node_cpu;
                }
            }
        }
        if(serviceNode.equals(current)) return null;
        return serviceNode;
    }
}
