package service.orchestrator.migration;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.nodes.ServiceNodeRegistry;
import service.orchestrator.properties.OrchestratorProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.util.Objects.nonNull;

public class JitterTrigger implements Trigger{

    private FileWriter file;

    private static final Logger logger = LoggerFactory.getLogger(LatencyTrigger.class);

    private final Selector selector;
    private final Migrator migrator;

    public JitterTrigger(Selector selector, Migrator migrator){
        this.selector = selector;
        this.migrator = migrator;
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

                boolean jittery = hasTheJitters(node);
                if (jittery) {
                    logger.debug("{} latency values are jittery", mcLatencyEntry.getKey());
//                    findBetterServiceNodeForClient(mcLatencyEntry.getKey(), node);
//                    triggerMigration(node);
                    mockTriggerMigration(node);
                } else {
                    logger.debug("{} latency values are not jittery", mcLatencyEntry.getKey());
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

    private void mockTriggerMigration(ServiceNode currentServiceNode){
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
//        ServiceNode migrationTarget = selector.select(allServiceNodes,null);
        ServiceNode migrationTarget = ((JitterSelector)selector).mockSelect(allServiceNodes,null,currentServiceNode);
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    @Override
    public void run() {
        examine(ServiceNodeRegistry.get().getHostingAndStableServiceNodes());
    }
}
