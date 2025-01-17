package service.orchestrator.migration;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.clients.MobileClient;
import service.orchestrator.clients.MobileClientRegistry;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.nodes.ServiceNodeRegistry;
import service.orchestrator.properties.OrchestratorProperties;
import service.util.Debugger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;

// separate Trigger and TriggerStrategy -> Latency, CPU, Memory, Storage, Combined (AND/OR)
public class LatencyTrigger implements Trigger {

    private FileWriter file;
    private static final Logger logger = LoggerFactory.getLogger(LatencyTrigger.class);

    private final Selector selector;
    private final Migrator migrator;

    public LatencyTrigger(Selector selector, Migrator migrator) {
        this.selector = selector;
        this.migrator = migrator;
    }

    private static double meanLatency(List<Long> latencies) {
        return latencies.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    @Override
    public void examine(Collection<ServiceNode> hostingNodes) {
        JSONObject outjson = new JSONObject();
        try {
            file = new FileWriter("test_json_output_examine.json",true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OrchestratorProperties properties = OrchestratorProperties.get();
        logger.debug("{} nodes in examine", hostingNodes.size());

        for (ServiceNode node : hostingNodes) {
            logger.debug("examining {}", node.uuid);
            outjson.put("uuid",node.uuid);
            for (Map.Entry<UUID, List<Long>> mcLatencyEntry : node.latencyEntries()) {
                logger.debug("{} has {} latencies", mcLatencyEntry.getKey(), mcLatencyEntry.getValue().size());
                outjson.put("latency",mcLatencyEntry.getValue().size());

                double latencyAggregate = meanLatency(mcLatencyEntry.getValue());
                if (latencyAggregate > properties.getMaxLatency()) {
                    logger.debug("{} has high latency {}", mcLatencyEntry.getKey(), latencyAggregate);
//                    findBetterServiceNodeForClient(mcLatencyEntry.getKey(), node);
//                    triggerMigration(node);
                    mockTriggerMigration(node);
                } else {
                    logger.debug("{} has low latency {}", mcLatencyEntry.getKey(), latencyAggregate);
                }
            }
        }
        try {
            if(hostingNodes.size() > 0){
                file.write(outjson.toJSONString());
            }
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void mockTriggerMigration(ServiceNode currentServiceNode){
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
        ServiceNode migrationTarget = ((LatencySelector)selector).mockSelect(allServiceNodes, null,currentServiceNode);
        Debugger.write(migrationTarget.toString());
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    private void triggerMigration(ServiceNode currentServiceNode) {
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
        ServiceNode migrationTarget = selector.select(allServiceNodes, null);
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    private void findBetterServiceNodeForClient(UUID clientUuid, ServiceNode currentServiceNode) {
        MobileClient mobileClient = MobileClientRegistry.get().get(clientUuid);
        Collection<ServiceNode> nonHostingNodes = ServiceNodeRegistry.get().getNonHostingNodes();

        ServiceNode migrationTarget = selector.select(nonHostingNodes, mobileClient);
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    @Override
    public void run() {
        examine(ServiceNodeRegistry.get().getHostingAndStableServiceNodes());
    }
}
