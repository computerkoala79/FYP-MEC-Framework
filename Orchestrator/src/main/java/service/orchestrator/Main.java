package service.orchestrator;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import service.orchestrator.clients.MobileClient;
import service.orchestrator.migration.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@CommandLine.Command(name = "cmMain", mixinStandardHelpOptions = true, version = "0.8")
public class Main implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
    @Parameters(index = "0", paramLabel = "port", description = "The port the orchestrator should run on")
    private int port;


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
        Selector selector = getSelector();
        Orchestrator orchestrator = new Orchestrator(port, selector);
        Trigger trigger = getTrigger(selector, orchestrator);
        scheduledService.scheduleAtFixedRate(trigger, 5, 5, TimeUnit.SECONDS);

        orchestrator.run();
    }

    private Selector getSelector() {
        return new JitterSelector();
//        return new SimpleSelector();
//        return new LatencySelector();
//        return new CpuSelector();
//        return new MainMemorySelector();
//        return new MainMemorySelector();
//        return new HighAvailabilitySelector(new LatencySelector());
//        return new CombinedSelector(new LatencySelector(), new CpuSelector());
    }

    private Trigger getTrigger(Selector selector, Orchestrator orchestrator) {
        return new JitterTrigger(selector,orchestrator);
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
