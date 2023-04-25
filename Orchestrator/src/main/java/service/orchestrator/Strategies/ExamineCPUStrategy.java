package service.orchestrator.Strategies;

import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public class ExamineCPUStrategy implements ExamineStrategy {
    @Override
    public ServiceNode examine(Collection<ServiceNode> hostingNodes) {
        return null;
    }
}
