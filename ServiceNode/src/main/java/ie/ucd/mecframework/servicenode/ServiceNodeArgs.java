package ie.ucd.mecframework.servicenode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServiceNodeArgs {
    private URI serverUri;
    private File serviceFile;
    private File serviceState;
    private URI serviceAddress;
    private String nodeLabel;
    private int latencyDelay;
    private boolean startService;
    private double cpuLoadIncrease;
    private boolean maxOutMemoryLoad;

    public ServiceNodeArgs(File argsFile){
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(argsFile);
            System.out.println("------------------ FileReader is ready " + reader.ready());
            JSONObject args = (JSONObject) jsonParser.parse(reader);
            System.out.println("------------------ JSON Object args is empty " + args.isEmpty());
            System.out.println("------------------ Server URI is " + args.get("orhestrator"));
            serverUri = new URI(args.get("orhestrator").toString());
            serviceFile = new File(args.get("serviceFile").toString());
            serviceState = new File(args.get("serviceState").toString());
            serviceAddress = new URI(args.get("serviceAddress").toString());
            nodeLabel = args.get("nodeLabel").toString();
            latencyDelay = new Integer(args.get("latencyDelay").toString());
            startService = new Boolean(args.get("startService").toString());
            cpuLoadIncrease = new Double(args.get("cpuLoadIncrease").toString());
            maxOutMemoryLoad = new Boolean(args.get("maxOutMemoryLoad").toString());
        } catch (IOException | ParseException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getServerURI(){ return serverUri; }

    public File getServiceFile() { return serviceFile; }

    public File getServiceState() { return serviceState; }

    public URI getServiceAddress() { return serviceAddress; }

    public String getNodeLabel() { return nodeLabel; }

    public int getLatencyDelay() { return latencyDelay; }

    public boolean isStartService() { return startService; }

    public double getCpuLoadIncrease() { return cpuLoadIncrease; }
    public boolean isMaxOutMemoryLoad() { return maxOutMemoryLoad; }
}
