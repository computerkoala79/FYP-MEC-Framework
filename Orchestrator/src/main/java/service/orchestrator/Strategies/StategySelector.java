package service.orchestrator.Strategies;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public class StategySelector {
    private SelectStrategy strategy;

    public StategySelector(SelectStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SelectStrategy strategy) {
        this.strategy = strategy;
    }

    public void select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current){
        strategy.select(nodes,mobileClient,current);
    }
}
