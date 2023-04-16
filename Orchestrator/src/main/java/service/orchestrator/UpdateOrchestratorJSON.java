package service.orchestrator;

import org.json.simple.JSONObject;
import service.orchestrator.nodes.ServiceNode;
import service.orchestrator.properties.OrchestratorProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class UpdateOrchestratorJSON {
    public static void write(Orchestrator orchestrator, Collection<ServiceNode> hostingNodes){
        System.out.println("-=-=-=-=-=-= writing to orch_console -=-=-=-==-=");
        JSONObject j = new JSONObject();
        OrchestratorProperties p = OrchestratorProperties.get();
        j.put("port",orchestrator.getPort());
        j.put("examineCount",hostingNodes.size());
        j.put("triggertype",p.getTriggerType());
//        j.put("triggerType",orchestrator.getCurrentTrigger().toString());
//        switch (orchestrator.getCurrentTrigger()){
//            case JITTER: j.put("triggerValue",p.getMaxJitter());
//        }
        try {
            FileWriter writer = new FileWriter("orch_console.json");
            writer.write(j.toJSONString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
