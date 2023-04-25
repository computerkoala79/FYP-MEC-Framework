package service.orchestrator.Strategies;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public class SelectMemoryStrategy implements SelectStrategy{
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current) {
        ServiceNode serviceNode = null;
        double lowest_mem = 1.0;
        for(ServiceNode node : nodes){
            if(!node.equals(current)){
                double freeMem_node = node.ramLoad.get(0);
                if(freeMem_node < lowest_mem) {
                    serviceNode = node;
                    lowest_mem = freeMem_node;
                }
            }
        }
        if(serviceNode.equals(current)) return null;
        return serviceNode;
    }
}
