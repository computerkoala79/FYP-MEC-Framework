package ie.ucd.mecframework.migration;

import ie.ucd.mecframework.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class MigrationManager {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private MigrationStrategy strategy;

    public MigrationManager(MigrationStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Stops the running service, launches a {@code TransferServer} and returns the {@code InetSocketAddress} that the
     * {@code TransferClient} on the target node can use to connect to the {@code TransferServer}.
     *
     * @return the address that the {@code TransferClient} can use to connect to the {@code TransferServer}.
     * The {@code TransferServer} port number might not be the same as the advertised port number because of NAT rules.
     */
    public List<InetSocketAddress> migrateService() {
        logger.debug("- - - - - - Migration manager migrate service - ---- ");
        return strategy.migrateService();
    }

    /**
     * Makes this node set up a {@code TransferClient} and waits for the client to finish accepting the migrated service.
     */
    public void acceptService(List<InetSocketAddress> serverAddress) {
        strategy.acceptService(serverAddress);
    }
}
