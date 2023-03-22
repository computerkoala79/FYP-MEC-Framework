package service.orchestrator.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.clients.MobileClient;
import service.orchestrator.nodes.ServiceNode;

import java.util.*;

public class JitterSelector implements Selector{

    private static final Logger logger = LoggerFactory.getLogger(LatencyTrigger.class);
    @Override
    public ServiceNode select(Collection<ServiceNode> nodes, MobileClient mobileClient) {
        ServiceNode nonJitteryNode = null;
        for(ServiceNode node : nodes){
            Map<UUID, List<Long>> latencies = node.getMobileClientLatencies();
            long hardCodedMaxJitter = 500;
            Collection<List<Long>> l = latencies.values();
            for(List<Long> list : l){
                list.sort(Comparator.naturalOrder());
                long maxJitter = list.get(list.size() - 1) - list.get(0);
                logger.debug("-=-=-=- {} has Max Jitter of {} -=-=-=-=",node.uuid,maxJitter);
                if(hardCodedMaxJitter > maxJitter) nonJitteryNode = node;
            }
        }
        return nonJitteryNode;
    }

    public ServiceNode mockSelect(Collection<ServiceNode> nodes, MobileClient mobileClient, UUID currentUUID){
        ServiceNode nonJitteryNode = null;
        for(ServiceNode node : nodes){
            if(node.uuid != currentUUID){
                Map<UUID, List<Long>> latencies = node.getMobileClientLatencies();
                long hardCodedMaxJitter = 500;
                Collection<List<Long>> l = latencies.values();
                for(List<Long> list : l){
                    list.sort(Comparator.naturalOrder());
                    long maxJitter = list.get(list.size() - 1) - list.get(0);
                    logger.debug("-=-=-=- {} has Max Jitter of {} -=-=-=-=",node.uuid,maxJitter);
                    if(hardCodedMaxJitter > maxJitter) nonJitteryNode = node;
                }
            }
        }
        return nonJitteryNode;
    }
}
