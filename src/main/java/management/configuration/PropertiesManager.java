package management.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author panos
 */
public final class PropertiesManager
{
    private static PropertiesManager propManager = null;
    //private static String initFilePath = "/home/diachron/temp/integration_layer_config.properties";
    //private static String initFilePath = "/home/panos/NetBeansProjects/diachron_v2/integration_layer_config.properties";
    private static Properties prop;
    
    public static PropertiesManager getPropertiesManager()
    {
        if(propManager==null)
        {            
            propManager = new PropertiesManager();
            propManager.readFile();
        }
        
        return propManager;
    }
    
    public void readFile()
    {
            try {
                prop = new Properties();
                InputStream input = this.getClass().getClassLoader().getResourceAsStream("diachron.properties");
                System.err.println(" ------ " + input.toString());
                prop.load(input);                                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }
    
    public String getPropertyValue(String key)
    {
        return prop.getProperty(key);
    }
    
    public Properties getProperties()
    {
        return prop;
    }
}
