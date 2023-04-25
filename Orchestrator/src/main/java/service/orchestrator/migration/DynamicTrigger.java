package service.orchestrator.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.orchestrator.Orchestrator;
import service.orchestrator.Strategies.*;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.nodes.ServiceNodeRegistry;
import service.orchestrator.properties.OrchestratorProperties;
import service.orchestrator.properties.TriggerType;

import java.util.Collection;

public class DynamicTrigger implements Trigger{
    private static final Logger logger = LoggerFactory.getLogger(DynamicTrigger.class);
    private final Selector selector;
    private final Migrator migrator;

    public DynamicTrigger(Selector selector, Migrator migrator){
        this.selector = selector;
        this.migrator = migrator;
    }

    @Override
    public void examine(Collection<ServiceNode> hostingNodes) {
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

        if(current != null) {
            switch (type){
                case LATENCY: triggerMigration(current,new SelectLatencyStrategy());break;
                case JITTER:  triggerMigration(current,new SelectJitterStrategy());break;
                case CPU:     triggerMigration(current,new SelectCPUStrategy());break;
                case MEMORY:  triggerMigration(current,new SelectMemoryStrategy());break;
                case COMBINED: break;
            }
        }
    }

    private void triggerMigration(ServiceNode current, SelectStrategy strategy) {
        Collection<ServiceNode> allServiceNodes = ServiceNodeRegistry.get().getServiceNodes();
        ServiceNode target = strategy.select(allServiceNodes,null,current);
        migrator.migrate(current,target);
    }

    @Override
    public void run() {
        examine(ServiceNodeRegistry.get().getHostingAndStableServiceNodes());
    }
}
