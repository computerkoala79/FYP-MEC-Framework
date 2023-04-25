package service.orchestrator.Strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;

import java.util.*;

public class ExamineJitterStrategy implements ExamineStrategy{
    private static final Logger logger = LoggerFactory.getLogger(ExamineJitterStrategy.class);
    @Override
    public ServiceNode examine(Collection<ServiceNode> hostingNodes) {
        OrchestratorProperties properties = OrchestratorProperties.get();
        logger.debug("{} nodes in examine", hostingNodes.size());

        for (ServiceNode node : hostingNodes) {
            logger.debug("examining {}", node.uuid);
            for (Map.Entry<UUID, List<Long>> mcLatencyEntry : node.latencyEntries()) {
                logger.debug("{} has {} latencies", mcLatencyEntry.getKey(), mcLatencyEntry.getValue().size());
                boolean jittery = hasTheJitters(node);
                if (jittery) {
                    logger.debug("{} latency values are jittery", mcLatencyEntry.getKey());
                    return node;
                } else {
                    logger.debug("{} latency values are not jittery", mcLatencyEntry.getKey());
                }
            }
        }
        return null;
    }
    private boolean hasTheJitters(ServiceNode node){
        Map<UUID, List<Long>> latencies = node.getMobileClientLatencies();
        long hardCodedMaxJitter = 300;
        hardCodedMaxJitter = OrchestratorProperties.get().getMaxJitter();
        Collection<List<Long>> l = latencies.values();
        for(List<Long> list : l){
            list.sort(Comparator.naturalOrder());
            long maxJitter = list.get(list.size() - 1) - list.get(0);
            if(hardCodedMaxJitter < maxJitter){
                logger.debug("-=-=-=- {} has the Jitters of {} -=-=-=-=",node.uuid,maxJitter);
                return true;
            }
        }
        return false;
    }
}
