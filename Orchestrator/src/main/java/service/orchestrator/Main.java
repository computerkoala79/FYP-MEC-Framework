package service.orchestrator;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import service.orchestrator.migration.*;
import service.orchestrator.properties.TriggerType;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@CommandLine.Command(name = "cmMain", mixinStandardHelpOptions = true, version = "0.8")
public class Main implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
    private Properties properties;
    public Properties getProperties() { return properties; }
    public void setProperties(Properties properties) { this.properties = properties; }

    @Parameters(index = "0", paramLabel = "port", description = "The port the orchestrator should run on")
    private int port;

    @Parameters(index = "1", paramLabel = "triggerType", description = "Active Trigger")



    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        JSONObject jsonObject = new JSONObject();
        TestGsonOut testGsonOut = new TestGsonOut(1001,"I'm the test.");
        try {
            FileWriter file = new FileWriter("test_json_output.json");
            jsonObject.put("id",testGsonOut.getId());
            jsonObject.put("name",testGsonOut.getName());
            file.write(jsonObject.toJSONString());
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        logger.info("Starting Orchestrator");
        Properties p = new Properties();

        try {
            FileReader reader = new FileReader("orchestrator.properties");
            p.load(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.debug("-=-=-=-=-=-=-= Trigger Type " + p.getProperty("application.trigger.type").toString() + "-=-=-=-=-==-");
        TriggerType triggerType = TriggerType.valueOf(p.getProperty("application.trigger.type"));
        Selector selector = getSelector(triggerType);

        port = Integer.parseInt(p.getProperty("orchestrator.port"));
        System.out.println("Orch Port: " + port);
        Orchestrator orchestrator = new Orchestrator(port, selector);
        Trigger trigger = getTrigger(triggerType,selector, orchestrator);
        scheduledService.scheduleAtFixedRate(trigger, 5, 5, TimeUnit.SECONDS);

        orchestrator.run();
    }

    private Selector getSelector(TriggerType triggerType) {
        switch (triggerType){
            case LATENCY: return new LatencySelector();
            case JITTER: return new JitterSelector();
            case CPU: return new CpuSelector();
            case MEMORY: return new MainMemorySelector();
            case COMBINED: return new CombinedSelector(new LatencySelector(),new JitterSelector(),new CpuSelector(),new MainMemorySelector());
            default: return new LatencySelector();
        }
//        return new JitterSelector();
//        return new SimpleSelector();
//        return new LatencySelector();
//        return new CpuSelector();
//        return new MainMemorySelector();
//        return new MainMemorySelector();
//        return new HighAvailabilitySelector(new LatencySelector());
//        return new CombinedSelector(new LatencySelector(), new CpuSelector());
    }

    private Trigger getTrigger(TriggerType triggerType, Selector selector, Orchestrator orchestrator) {
        switch (triggerType){
            case LATENCY: return new LatencyTrigger(selector, orchestrator);
            case JITTER: return new JitterTrigger(selector,orchestrator);
            case CPU: return new CpuTrigger(selector, orchestrator);
            case MEMORY: return new MainMemoryTrigger(selector, orchestrator);
            case COMBINED: return new CombinedTrigger(selector,orchestrator);
            default: return new LatencyTrigger(selector, orchestrator);
        }
//        return new JitterTrigger(selector,orchestrator);
//        return new LatencyTrigger(selector, orchestrator);
//        return new CpuTrigger(selector, orchestrator);
//        return new MainMemoryTrigger(selector, orchestrator);

//        DeferredMigrator deferredMigrator = new DeferredMigrator();
//        Trigger latency = new LatencyTrigger(selector, deferredMigrator);
//        Trigger cpu = new CpuTrigger(selector, deferredMigrator);
//
//        return new CombinedTrigger(selector, orchestrator, deferredMigrator, cpu, latency);
    }


}
