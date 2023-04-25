package service.orchestrator.Strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExamineLatencyStrategy implements ExamineStrategy{
    private static final Logger logger = LoggerFactory.getLogger(ExamineLatencyStrategy.class);
    @Override
    public ServiceNode examine(Collection<ServiceNode> hostingNodes) {
        OrchestratorProperties properties = OrchestratorProperties.get();
        for (ServiceNode node : hostingNodes) {
            for (Map.Entry<UUID, List<Long>> mcLatencyEntry : node.latencyEntries()) {
                double latencyAggregate = meanLatency(mcLatencyEntry.getValue());
                if (latencyAggregate > properties.getMaxLatency()) {
                    logger.debug("{} has high latency {}", mcLatencyEntry.getKey(), latencyAggregate);
                    return node;
                } else {
                    logger.debug("{} has low latency {}", mcLatencyEntry.getKey(), latencyAggregate);
                }
            }
        }
        return null;
    }
    private static double meanLatency(List<Long> latencies) {
        return latencies.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }
}
