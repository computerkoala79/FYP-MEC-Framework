package service.orchestrator.migration;

import service.orchestrator.Orchestrator;
import service.orchestrator.clients.MobileClient;
import service.orchestrator.clients.MobileClientRegistry;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.nodes.ServiceNodeRegistry;
import service.orchestrator.properties.OrchestratorProperties;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombinedTrigger implements Trigger {
    private final Selector selector;
    private final Migrator migrator;
    private static final Logger logger = LoggerFactory.getLogger(CombinedTrigger.class);
//    private final DeferredMigrator deferredMigrator;
//    private final List<Trigger> triggers;

//    private final Priority priority;
//    private final Orchestrator orchestrator;
    public CombinedTrigger(Selector selector, Migrator migrator){
        this.selector = selector;
        this.migrator = migrator;
    }

//    public CombinedTrigger(Selector selector, Migrator migrator, DeferredMigrator deferredMigrator,
//                           Trigger... triggers) {
//        this.selector = selector;
//        this.migrator = migrator;
//        this.deferredMigrator = deferredMigrator;
//        this.triggers = Arrays.asList(triggers);
//    }

    @Override
    public void examine(Collection<ServiceNode> hostingNodes){
        OrchestratorProperties properties = OrchestratorProperties.get();
        logger.debug("{} nodes in examine", hostingNodes.size());


    }

    private boolean latencyTrigger(Collection<ServiceNode> hostingNodes){
        return false;
    }

    private boolean jitterTrigger(){
        return false;
    }

    private boolean cpuTrigger(){
        return false;
    }

    private boolean memTrigger(){
        return false;
    }


//    @Override
//    public void examine(Collection<ServiceNode> hostingNodes) {
//        for (Trigger trigger : triggers) {
//            trigger.examine(hostingNodes);
//        }
//
//        List<ServiceNode> triggerableNodes = deferredMigrator.getTriggerableServiceNodes();
//        if (!triggerableNodes.isEmpty()) {
//            findTargetServiceNode(triggerableNodes.get(0));
//        }
//    }

    private void findTargetServiceNode(ServiceNode currentServiceNode) {
        MobileClient mobileClient = getStrugglingMobileClient(currentServiceNode);
        Collection<ServiceNode> nonHostingNodes = ServiceNodeRegistry.get().getNonHostingNodes();

        ServiceNode migrationTarget = selector.select(nonHostingNodes, mobileClient);
        if (nonNull(migrationTarget)) {
            migrator.migrate(currentServiceNode, migrationTarget);
        }
    }

    private MobileClient getStrugglingMobileClient(ServiceNode serviceNode) {
        UUID clientUuid = serviceNode.latencyEntries().stream()
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
        return isNull(clientUuid) ? null : MobileClientRegistry.get().get(clientUuid);
    }

    @Override
    public void run() {
        examine(ServiceNodeRegistry.get().getHostingAndStableServiceNodes());
    }
}
