package ie.ucd.mecframework;

import ie.ucd.mecframework.migration.MigrationManager;
import ie.ucd.mecframework.migration.MigrationStrategy;
import ie.ucd.mecframework.migration.StatefulMigrationStrategy;
import ie.ucd.mecframework.service.DockerController;
import ie.ucd.mecframework.service.JarController;
import ie.ucd.mecframework.service.ServiceController;
import ie.ucd.mecframework.servicenode.ServiceNode;
import ie.ucd.mecframework.servicenode.ServiceNodeArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@CommandLine.Command(name = "ServiceNode Driver", mixinStandardHelpOptions = true, version = "0.1")
public class Main implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private URI serverUri;
    private File serviceFile;
    private File serviceState;
    private URI serviceAddress;
    private String nodeLabel;
    private int latencyDelay;
    private List<Long> latencies = new ArrayList<>();
    private boolean startService;
    private double cpuLoadIncrease;
    private boolean maxOutMemoryLoad;

    private NodeType nodeType;

    @Parameters(index = "0", paramLabel = "Service Node Args File",
        description = "File containing necessary arguments to start a node.",
        defaultValue = "ServiceNodeArgs.json")
    private File argsFile;

    private ServiceNodeArgs args;

    // input parameters
//    @Parameters(index = "0", paramLabel = "orchestrator",
//            description = "The address of the orchestrator. Format ws://{ip address}:{port}")
//    private URI serverUri;
//    @Parameters(index = "1", paramLabel = "file", description = "The name of the file storing the service you wish to run.")
//    private File serviceFile;
//
//    // jklos add a default value to the service state
//    @Parameters(index = "2", paramLabel = "state", description = "The name of the file storing the service state.")
//    private File serviceState;
//    @Parameters(index = "3", paramLabel = "serviceAddress",
//            description = "The address any services will run out of on this machine {ip}:{port}")
//    private URI serviceAddress;
//    @Parameters(index = "4", paramLabel = "nodeLabel", description = "An identifying name for this Service Node",
//            defaultValue = "some-service-node")
//    private String nodeLabel;
//    @Parameters(index = "5", paramLabel = "latencyDelay", description = "An extra delay added on to the latency" +
//            " values collected by this node", defaultValue = "0")
//    private int latencyDelay;
//    @Parameters(index = "6", paramLabel = "startService", description = "Whether or not to start the service on" +
//            " initializing this ServiceNode.", defaultValue = "true")
//    private boolean startService;
//
//    @Parameters(index = "7", paramLabel = "cpuLoadIncrease", description = "amount to add to cpu load of node.",
//        defaultValue = "5")
//    private double cpuLoadIncrease;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("------------------ can read json file " + argsFile.canRead());
        setServiceNodeArguments();
        removeStateIfExists();
        ServiceController serviceController = getServiceController();
        MigrationStrategy migrationStrategy = getMigrationStrategy(serviceController);
        ServiceNode serviceNode =
                new ServiceNode(serverUri, serviceController, new MigrationManager(migrationStrategy),
                        nodeLabel, latencies, cpuLoadIncrease, maxOutMemoryLoad, nodeType
                );
        serviceNode.run();  // run instead of start a Thread to stop the program from finishing immediately
        serviceController.stopService();
    }

    private void setServiceNodeArguments(){
        args = new ServiceNodeArgs(argsFile);
        serverUri = args.getServerURI();
        serviceFile = args.getServiceFile();
        serviceState = args.getServiceState();
        serviceAddress = args.getServiceAddress();
        nodeLabel = args.getNodeLabel();
//        latencyDelay = args.getLatencyDelay();
        latencies = args.getLatencies();
        startService = args.isStartService();
        cpuLoadIncrease = args.getCpuLoadIncrease();
        maxOutMemoryLoad = args.isMaxOutMemoryLoad();
        nodeType = args.getNodeType();
    }

    // Only for testing: removes the serviceFile's application state before launching the ServiceNode to ensure that
    //  this run's state is clear. This helps to ensure that the mobile-client is connecting to the service and that
    //  the file migrates correctly.
    private void removeStateIfExists() {
        boolean wasDeleted = serviceState.delete();
        logger.debug("Service state existed?={}, and was deleted", wasDeleted);
    }

    private MigrationStrategy getMigrationStrategy(ServiceController serviceController) {
//        return new StatelessMigrationStrategy(serviceController, serviceFile);
        return new StatefulMigrationStrategy(serviceController, serviceFile, serviceState);
    }

    private ServiceController getServiceController() {
//        return initializeDockerController();
        return initializeJarController();
    }

    // todo use reflection to keep below DRY. Or use the strategy pattern.

    /**
     * Initializes the {@code ServiceController} for this Node and starts the service if the file exists and
     * {@code startService} was specified on the command line.
     *
     * @return the initialized ServiceController.
     */
    @SuppressWarnings("unused")
    private ServiceController initializeDockerController() {
        Path servicePath = getServiceFileCanonicalPath();
        logger.info(servicePath.toString());
        ServiceController controller = new DockerController(servicePath);

        if (serviceFile.exists() && startService) {
            controller.startService();
        } else logger.info("Did not start service. serviceFile.exists()={} startService={}",
                serviceFile.exists(), startService);
        return controller;
    }

    private ServiceController initializeJarController() {
        Path servicePath = getServiceFileCanonicalPath();
        logger.info(servicePath.toString());
        ServiceController controller = new JarController(servicePath);

        if (serviceFile.exists() && startService) {
            controller.startService();
        } else logger.info("Did not start service. serviceFile.exists()={} startService={}",
                serviceFile.exists(), startService);
        return controller;
    }

    private Path getServiceFileCanonicalPath() {
        try {
            return Paths.get(serviceFile.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException("getCanonicalPath error", e);
        }
    }

    public enum NodeType{
        SERVICE,
        CLOUD,
        CLIENT
    }
}
