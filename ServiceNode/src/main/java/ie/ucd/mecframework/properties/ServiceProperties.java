package ie.ucd.mecframework.properties;

import ie.ucd.mecframework.servicenode.ServiceNodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;


import static java.util.Objects.isNull;

public class ServiceProperties {

    private final OS os;
    private static final Logger logger = LoggerFactory.getLogger(ServiceProperties.class);
    private static String filename;
    private static final String DEFAULT_DOUBLE = Double.valueOf(1.0).toString();
    private static ServiceProperties instance;
    private static Properties properties;

    private ServiceProperties(String filename){
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            os = OS.WINDOWS;
        } else if (osName.contains("Linux")) {
            os = OS.LINUX;
        } else {
            throw new RuntimeException("Couldn't determine OS in " + ServiceNodeProperties.class.getSimpleName());
        }

        this.filename = filename;
        properties = new Properties();
        logger.debug("-=-= Making Service Node Properties from " +
                filename + " -=-=-=-=-");
        try {
            FileReader reader = new FileReader(filename);
            properties.load(reader);
        } catch(IOException ioe) {
            logger.error("Couldn't load " + filename, ioe);
            throw new MissingResourceException(
                    String.format("No Service Node properties file found: %s", ioe.getMessage()),
                    ServiceProperties.class.getSimpleName(),
                    filename
            );
        }
    }

    public static ServiceProperties get(String filename) {
        if(isNull(instance)){
            instance = new ServiceProperties(filename);
            return instance;
        }
        updateProperties();
        return instance;
    }

    public static void updateProperties(){
        try{
            FileReader reader = new FileReader(filename);
            properties.load(reader);
        } catch(IOException ioe) {
            logger.error("Couldn't load " + filename, ioe);
            throw new MissingResourceException(
                    String.format("No Service Node properties file found: %s", ioe.getMessage()),
                    ServiceProperties.class.getSimpleName(),
                    filename
            );
        }
    }



    private enum OS {WINDOWS, LINUX}



}
