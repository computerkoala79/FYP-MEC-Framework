package service.orchestrator.Strategies;

import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public interface ExamineStrategy {
    ServiceNode examine(Collection<ServiceNode> hostingNodes);
}
