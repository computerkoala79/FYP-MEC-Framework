package service.orchestrator.migration;

import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class CombinedSelector implements Selector {
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
        return null;
    }


    private final Selector[] selectors;

    public CombinedSelector(Selector... selectors) {
        this.selectors = selectors;
    }

    public ServiceNode mockSelect(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode badNode){
        System.out.println("--------- inside mock Combination selector - -------");
        ServiceNode returnNode = null;
        // priority order is set by the order of the list
        for(Selector selector : selectors){
            if(returnNode == null) returnNode = selector.mockSelect(nodes,mobileClient,badNode);
        }

        return returnNode;
    }
//
//    private static ServiceNode getNodeWithMostSelections(Map<ServiceNode, Integer> selectionCounts) {
//        return selectionCounts.entrySet().stream()
//                .sorted((e1, e2) -> -Integer.compare(e1.getValue(), e2.getValue()))
//                .map(Map.Entry::getKey)
//                .findFirst()
//                .orElse(null);
//    }
//
//    @Override
//    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
//        Map<ServiceNode, Integer> selectionCounts = new HashMap<>();
//
//        for (Selector selector : selectors) {
//            ServiceNode selected = selector.select(nodes, mobileClient);
//            if (nonNull(selected)) {
//                selectionCounts.merge(selected, 1, Integer::sum);
//            }
//        }
//
//        ServiceNode mostSelectedNode = getNodeWithMostSelections(selectionCounts);
//        if (nonNull(mostSelectedNode) && selectionCounts.get(mostSelectedNode) == selectors.length) {
//            return mostSelectedNode;
//        }
//        return null;
//    }
}
