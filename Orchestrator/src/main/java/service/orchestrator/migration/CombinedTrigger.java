package service.orchestrator.migration;

import service.orchestrator.Strategies.*;
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
import service.orchestrator.properties.TriggerType;

public class CombinedTrigger implements Trigger {
    private final Selector selector;
    private final Migrator migrator;
    private static final Logger logger = LoggerFactory.getLogger(CombinedTrigger.class);

    public CombinedTrigger(Selector selector, Migrator migrator){
        this.selector = selector;
        this.migrator = migrator;
    }

    @Override
    public void examine(Collection<ServiceNode> hostingNodes){
        OrchestratorProperties properties = OrchestratorProperties.get();
        TriggerType type = TriggerType.valueOf(properties.getTriggerType());

        logger.debug("Current Trigger Type: " + type.toString());
        logger.debug("{} nodes in examine", hostingNodes.size());

        Examiner examiner = null;

        switch (type){
            case LATENCY: examiner = new Examiner(new ExamineLatencyStrategy());break;
            case JITTER:  examiner = new Examiner(new ExamineJitterStrategy());break;
            case CPU:     examiner = new Examiner(new ExamineCPUStrategy());break;
            case MEMORY:  examiner = new Examiner(new ExamineMemoryStrategy());break;
            case COMBINED: break;
        }

        ServiceNode current = null;
        if(examiner != null) current = examiner.examine(hostingNodes);

        ServiceNode target = null;
        if(current != null) {
            switch (type){
                case LATENCY: triggerMigration(current,new SelectLatencyStrategy());break;
                case JITTER:  triggerMigration(current,new SelectJitterStrategy());break;
                case CPU:     triggerMigration(current,new SelectCPUStrategy());break;
                case MEMORY:  triggerMigration(current,new SelectMemoryStrategy());break;
                case COMBINED: break;
            }
        }

        if(target != null) migrator.migrate(current,target);
    }
    // the null mobile client will need to be replaced with the getStrugglingMobileClient method in production
    private void triggerMigration(ServiceNode current, SelectStrategy strategy){
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
        strategy.select(allServiceNodes,null,current);
    }

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
