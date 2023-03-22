package ie.ucd.clientnode;

import ie.ucd.clientnode.websocket.PingServer;
import ie.ucd.mecframework.migration.MigrationManager;
import ie.ucd.mecframework.migration.MigrationStrategy;
import ie.ucd.mecframework.migration.StatefulMigrationStrategy;
import ie.ucd.mecframework.service.JarController;
import ie.ucd.mecframework.service.ServiceController;
import ie.ucd.mecframework.servicenode.ServiceNode;
import ie.ucd.mecframework.servicenode.ServiceNodeArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import service.core.MobileClientInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

public class Main{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Thread pingThread;
    private PingServer pingServer;
    private URI orchestratorURI;
    private InetSocketAddress pingServerAddress;

    private ServiceNodeArgs args;

    private ServiceController serviceController;

    public static void main(String[] args){
        System.out.println("-------------");
        System.out.println("-------------");
        System.out.println("-------------");

        pingThread = new Thread(new Main().new testRun());
        pingThread.start();

    }

    private void setting() {
        pingServerAddress = new InetSocketAddress("127.0.0.1", 8081);
        pingServer = new PingServer(pingServerAddress);
    }

    private class testRun implements Runnable {
        @Override
        public void run() {
            System.out.println("***************");
            System.out.println("***************");
            System.out.println("***************");
            System.out.println("***************");
            setting();
            System.out.println("Ping Server Address: " + pingServer.getAddress().toString());
            System.out.println("Ping Server Port: " + pingServer.getPort());
            System.out.println("***************");
            System.out.println("***************");

            // temp hard coded elements for testing
            URI orchestrator = null;
            try {
                orchestrator = new URI("ws://localhost:8080");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            File serviceFile = new File("/");
            File serviceState = new File("/state");
            URI serviceAddress = null;
            try {
                serviceAddress = new URI("ws://localhost:8010");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String nodeLabel = "client";
            try {
                serviceController = new JarController(Paths.get(serviceFile.getCanonicalPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            MigrationStrategy migrationStrategy = new StatefulMigrationStrategy(serviceController, serviceFile, serviceState);

            MobileClientInfo mobileClientInfo = new MobileClientInfo(UUID.randomUUID(),pingServerAddress);
            mobileClientInfo.setPingServerAddress(pingServerAddress.getAddress());


            pingServer.start();

        }
    }



}
