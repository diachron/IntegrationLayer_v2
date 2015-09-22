package rdfization;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import management.configuration.PropertiesManager;
import org.athena.imis.diachron.archive.core.dataloader.StoreFactory;
import org.athena.imis.diachron.archive.datamapping.MultidimensionalConverter;
import org.athena.imis.diachron.archive.datamapping.OntologyConverter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class DiachronizationModule
{
    public void uploadDiachronizeAndStore(String datasetName,String inPath, String outPath,String label,String creator, String converterType)
    {
        FileInputStream fis = null;
        PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
        String serverLocationFolder = propertiesManager.getPropertyValue("ServerTempFolder");
        String diachronicURL = new String(propertiesManager.getPropertyValue("http://www.diachron-fp7.eu/resource/diachronicDataset/")
                                            + datasetName + "/" + label);

        System.out.println(" ---------" + outPath + " " + diachronicURL);
                
        try
        {
//            if(createDiachronicDataset(datasetName, label, creator))
//            {                
                fis = new FileInputStream(inPath);
                FileOutputStream fos = new FileOutputStream(outPath);
                
                if(converterType.equals("ontology"))
                {
                    OntologyConverter converter = new OntologyConverter();                                   
                    converter.convert(fis, fos, datasetName);
                }
                else if(converterType.equals("multidimensional"))
                {
                    MultidimensionalConverter converter = new MultidimensionalConverter();
                    converter.convert(fis, fos, "rdf", datasetName);
                }

                fis.close();
                fos.close();            
                System.out.println(" ---------" + outPath + " " + diachronicURL);
                
/*
                System.out.println(" ---------" + outPath + " " + diachronicURL);
                FileInputStream fis2 = new FileInputStream(outPath);
                StoreFactory.createDataLoader().loadData(fis2,
                                            diachronicURL,
                                            "RDF/XML");          
        */
  //          }
        } 

        catch (Exception ex) {
            Logger.getLogger(GeneralUploadRDFResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private boolean createDiachronicDataset(String datasetName, String label, String creator)
    throws JSONException
    {
        JSONObject jsonOutputMessage = null;
        Client c = Client.create();

        PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
        String archiveDatasetResourceAddress = propertiesManager.getPropertyValue("ArchiveDatasetResourceAddress");
                
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("datasetName", datasetName);
        formData.add("label", label);
        formData.add("creator", creator);        
        
        WebResource r = c.resource(archiveDatasetResourceAddress);

        ClientResponse response = r.type("application/x-www-form-urlencoded")
                                                  .post(ClientResponse.class, formData);
        
        jsonOutputMessage = new JSONObject((String)response.getEntity(String.class));
        
        System.err.println(jsonOutputMessage.toString());
        return jsonOutputMessage.getBoolean("success");
    } 
    
    private void saveFile(InputStream uploadedInputStream, String filename)
    {
        try
        {            
            OutputStream outpuStream = null;
            int read = 0;
            byte[] bytes = new byte[1024];

            outpuStream = new FileOutputStream(new File(filename));
            while ((read = uploadedInputStream.read(bytes)) != -1)
            {
                outpuStream.write(bytes, 0, read);
            }

            outpuStream.flush();
            outpuStream.close();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }    
    
}
