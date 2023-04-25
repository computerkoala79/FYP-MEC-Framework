package service.orchestrator.Strategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.clients.MobileClient;

import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;

import java.util.*;

public class SelectJitterStrategy implements SelectStrategy{
    private static final Logger logger = LoggerFactory.getLogger(SelectJitterStrategy.class);
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient, ServiceNode current) {
        ServiceNode returnNode = null;
        OrchestratorProperties p = OrchestratorProperties.get();
        long maxJitterFromProperties = p.getMaxJitter();
        for(ServiceNode node : nodes){
            if(!node.equals(current)){
                Map<UUID, List<Long>> latencies = node.getMobileClientLatencies();
                Collection<List<Long>> latencyCollection = latencies.values();
                for(List<Long> latencyList : latencyCollection){
                    // sort the list then subtract the highest from the lowest value to get the max difference
                    latencyList.sort(Comparator.naturalOrder());
                    long maxJitter = latencyList.get(latencyList.size() - 1) - latencyList.get(0);
                    logger.debug("-=-=-=- {} has Max Jitter of {} -=-=-=-=",node.uuid,maxJitter);
                    if(maxJitterFromProperties > maxJitter) returnNode = node;
                }
            }
        }
        return returnNode;
    }
}
