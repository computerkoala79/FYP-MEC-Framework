package service.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import service.orchestrator.properties.OrchestratorProperties;

import java.io.IOException;

@CommandLine.Command(name = "cmMain", mixinStandardHelpOptions = true, version = "0.8")
public class Main implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(names = {"--RollingAverage"}, defaultValue = "80", paramLabel = "Rolling Average", description = "The value that should be used in the rolling average, format: input 80 for 80/20 rolling average, Defaults to 80")
    int rollingAverage;

    @Option(names = {"-s", "--secure"},
            description = "Secure mode, only engages with orchestrators using SSL")
    private boolean secure;

    @Parameters(index = "0", paramLabel = "port", description = "The port the orchestrator should run on")
    private int port;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        tryGetProperties();
        if (!secure) {
            // consider spinning up more threads here:
            //      e.g. periodic migration trigger
            //      Node Scorer? -> except this is a job that can be done on demand
            //          A thread would be good here if it was to keep scoring on a rolling basis.

            logger.info("Starting Orchestrator");
            Orchestrator orchestrator = new Orchestrator(port, rollingAverage);
            orchestrator.run();
        } else {
            try {
                new SecureOrchestrator(port, rollingAverage);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // todo remove
    private void tryGetProperties() {
        try {
            OrchestratorProperties ops = OrchestratorProperties.get();
            System.out.printf("%d %f %f %f\n",
                    ops.getMaxLatency(), ops.getMaxCpu(), ops.getMaxMemory(), ops.getMinStorage());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
