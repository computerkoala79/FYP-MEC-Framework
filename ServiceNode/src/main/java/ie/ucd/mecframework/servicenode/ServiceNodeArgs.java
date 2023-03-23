package ie.ucd.mecframework.servicenode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonToken;
import ie.ucd.mecframework.Main;
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
    private List<Long> latencies = new ArrayList<>();
    private boolean startService;
    private double cpuLoadIncrease;
    private boolean maxOutMemoryLoad;

    private Main.NodeType nodeType;

    public ServiceNodeArgs(File argsFile){
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader reader = new FileReader(argsFile);
            System.out.println("------------------ FileReader is ready " + reader.ready());
            JSONObject args = (JSONObject) jsonParser.parse(reader);
            System.out.println("------------------ JSON Object args is empty " + args.isEmpty());
            System.out.println("------------------ Server URI is " + args.get("orhestrator"));
            int typeSelector = Integer.parseInt(args.get("nodeType").toString());

            System.out.println(" **** ----- Node Type number is " + typeSelector);
            serverUri = new URI(args.get("orhestrator").toString());
            serviceFile = new File(args.get("serviceFile").toString());
            serviceState = new File(args.get("serviceState").toString());
            serviceAddress = new URI(args.get("serviceAddress").toString());
            nodeLabel = args.get("nodeLabel").toString();
//            latencyDelay = new Integer(args.get("latencyDelay").toString());
            JSONArray jray = (JSONArray) args.get("latencyDelay");
            for(int i = 0; i < jray.size(); i++){
                latencies.add(new Long(jray.get(i).toString()));
            }
            startService = new Boolean(args.get("startService").toString());
            cpuLoadIncrease = new Double(args.get("cpuLoadIncrease").toString());
            maxOutMemoryLoad = new Boolean(args.get("maxOutMemoryLoad").toString());
            if(typeSelector == 2){
                nodeType = Main.NodeType.CLIENT;
                System.out.println(" ***(*(*((*((*(*  I'm a Client **(*()*))*))**)) ");
            } else if(typeSelector == 3){
                nodeType = Main.NodeType.CLOUD;
            } else {
                nodeType = Main.NodeType.SERVICE;
                System.out.println(" ***(*(*((*((*(*  I'm a Service **(*()*))*))**)) ");
            }
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

    public List<Long> getLatencies() {return latencies;}

    public boolean isStartService() { return startService; }

    public double getCpuLoadIncrease() { return cpuLoadIncrease; }
    public boolean isMaxOutMemoryLoad() { return maxOutMemoryLoad; }

    public Main.NodeType getNodeType() { return nodeType; }

}
