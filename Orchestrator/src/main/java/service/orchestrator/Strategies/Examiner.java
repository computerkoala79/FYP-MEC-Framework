package service.orchestrator.Strategies;

import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public class Examiner {
    private ExamineStrategy strategy;
    public Examiner(ExamineStrategy strategy){
        this.strategy = strategy;
    }

    public void setStrategy(ExamineStrategy strategy){ this.strategy = strategy; }

    // returns a service node in need of migration or null if none
    public ServiceNode examine(Collection<ServiceNode> hostingNodes){
        return strategy.examine(hostingNodes);
    }
}
