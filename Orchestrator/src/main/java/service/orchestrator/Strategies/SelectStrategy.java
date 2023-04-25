package service.orchestrator.Strategies;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;

public interface SelectStrategy {
    ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current);
}
