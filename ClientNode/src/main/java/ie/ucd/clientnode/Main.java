package ie.ucd.clientnode;

import ie.ucd.clientnode.websocket.PingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;

public class Main implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(names = {"-o", "--orchestrator"}, defaultValue = "ws://localhost:8080",
            paramLabel = "Orchestrator URI", description = "Should be of the form: ws://{host}[:{port}]")
    private URI orchestratorUri;
    private PingServer pingServer;
    private Thread pingThread;
    private InetSocketAddress pingServerAddress;

//    @Option(names = {"-s","--serviceState"}, paramLabel = "state", defaultValue = "serviceStateFile.txt",
//            description = "The name of the file storing the service state.")
//    private File serviceState;
//
//    @Option(names = {"-sf", "--serviceFile"}, paramLabel = "Serivce File Location",
//            description = "The service file is a jar file which contains the app you wish to run.")
//    private File serviceFile;
//
//    @Option(names = {"-sa", "--serviceAddress"}, paramLabel = "Service Address IP and Port",
//            description = "The address any services will run out of on this machine {ip}:{port}")
//    private URI serviceAddress;
//
//    @Option(names = {"-n", "--nodeLabel"}, defaultValue = "client",
//            description = "An identifying name for this Service Node")
//    private String nodeLabel;
//
//    @Option(names = {"-ss", "--startService"}, defaultValue = "true",  arity = "1",
//            description = "Whether or not to start the service on" +
//                    " initializing this ServiceNode.")
//    private boolean startService;
//
//    private int latencyDelay = 0;

    public static void main(String[] args) {
        System.out.println("afjekla;fej iofjiawopfjeiakof jaeiklofjaeiop;");
        logger.debug("Starting Main Client");
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

//    public void setLatencyDelay(int latencyDelay) { this.latencyDelay = latencyDelay; }

    @Override
    public void run() {
        // take in an address
        logger.debug("Orchestrator address: " + orchestratorUri);
        startPingServer();
//        startApplication();
//        shutdownEverything();
        logger.debug("Finished.");
    }

    private void startPingServer() {
        try{
            pingServerAddress = createAddress();
            pingServer = new PingServer(pingServerAddress);
        } catch (Exception e){
            logger.warn("Couldn't find free port in range, shutting down");
            shutdownEverything();
        }
        pingThread = new Thread(pingServer);
        pingThread.start();
    }

    private InetSocketAddress createAddress() {
        //Getting network ip is difficult due to number of network interface
        return new InetSocketAddress("10.36.5.110", findFreePort());
    }

    //Taken from - https://stackoverflow.com/questions/51099027/find-free-port-in-java
    private static int findFreePort() {
        int port = 0;
        // For ServerSocket port number 0 means that the port number is automatically allocated.
        try (ServerSocket socket = new ServerSocket(0)) {
            // Disable timeout and reuse address after closing the socket.
            socket.setReuseAddress(true);
            port = socket.getLocalPort();
        } catch (IOException ignored) {}
        if (port > 0) {
            return port;
        }
        throw new RuntimeException("Could not find a free port");
    }

    /*
     * Shuts down the PingServer.
     */
    private void shutdownEverything() {
        try {
            final int timeout = 3 * 1000;
            pingServer.stop(timeout);
            pingThread.join(timeout);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    // Only for testing: removes the serviceFile's application state before launching the ServiceNode to ensure that
    //  this run's state is clear. This helps to ensure that the mobile-client is connecting to the service and that
    //  the file migrates correctly.
//    private void removeStateIfExists() {
//        boolean wasDeleted = serviceState.delete();
//        logger.debug("Service state existed?={}, and was deleted", wasDeleted);
//    }
//
//    private ServiceController initializeDockerController() {
//        Path servicePath = getServiceFileCanonicalPath();
//        logger.info(servicePath.toString());
//        ServiceController controller = new DockerController(servicePath);
//
//        if (serviceFile.exists() && startService) {
//            controller.startService();
//        } else logger.info("Did not start service. serviceFile.exists()={} startService={}",
//                serviceFile.exists(), startService);
//        return controller;
//    }
//
//    private Path getServiceFileCanonicalPath() {
//        try {
//            return Paths.get(serviceFile.getCanonicalPath());
//        } catch (IOException e) {
//            throw new RuntimeException("getCanonicalPath error", e);
//        }
//    }
//    private ServiceController initializeJarController() {
//        Path servicePath = getServiceFileCanonicalPath();
//        logger.info(servicePath.toString());
//        ServiceController controller = new JarController(servicePath);
//
//        if (serviceFile.exists() && startService) {
//            controller.startService();
//        } else logger.info("Did not start service. serviceFile.exists()={} startService={}",
//                serviceFile.exists(), startService);
//        return controller;
//    }
}
