package service.orchestrator.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.nodes.ServiceNodeRegistry;
import service.orchestrator.properties.OrchestratorProperties;
import service.util.Debugger;

import java.util.Collection;

import static java.util.Objects.nonNull;

public class MainMemoryTrigger implements Trigger {
    private static final Logger logger = LoggerFactory.getLogger(MainMemoryTrigger.class);

    private final Selector selector;
    private final Migrator migrator;

    public MainMemoryTrigger(Selector selector, Migrator migrator) {
        this.selector = selector;
        this.migrator = migrator;
    }

    @Override
    public void examine(Collection<ServiceNode> hostingNodes) {
        OrchestratorProperties properties = OrchestratorProperties.get();
        logger.debug("{} nodes in examine", hostingNodes.size());

        Debugger.write("Check Properties\nMax Mem: " + properties.getMaxMemory()
                + "\nMin Mem: " + properties.getMinMemoryGibibytes()
                + "\nHosting Node Count: " + hostingNodes.size());

        for (ServiceNode node : hostingNodes) {
            Debugger.write("Inside For Each Node Loop");
            logger.debug("examining {}", node.uuid);
            // for test setup use the second ramScore, for production use the first ramScore

//            double ramScore = node.getMainMemoryPercentFree();
            double ramScore = 0;
            if(!node.ramLoad.isEmpty()) ramScore = node.ramLoad.get(0);
            Debugger.write("Ram Score: " + ramScore);
            double unusedMemory = node.getMainMemoryInGibibytes();

            if (ramScore > properties.getMaxMemory() || unusedMemory < properties.getMinMemoryGibibytes()) {
                logger.debug("{} has memory issues: utilization={} free={} GiB", node.uuid, ramScore, unusedMemory);
                triggerMigration(node);
            } else {
                logger.debug("{} has low memory usage {}", node.uuid, ramScore);
            }
        }
    }

    private void triggerMigration(ServiceNode currentServiceNode) {
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
        Debugger.write("Migration Triggered, Available Service Node Count: " + allServiceNodes.size());
        ServiceNode migrationTarget = selector.mockSelect(allServiceNodes, null, currentServiceNode);
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    @Override
    public void run() {
        examine(ServiceNodeRegistry.get().getHostingAndStableServiceNodes());
    }
}
