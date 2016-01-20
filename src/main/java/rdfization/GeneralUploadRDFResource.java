/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdfization;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.athena.imis.diachron.archive.core.dataloader.StoreFactory;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataParam;
import io.FileManagement;
import javax.ws.rs.core.MediaType;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import management.configuration.PropertiesManager;
import org.athena.imis.diachron.archive.datamapping.OntologyConverter;
import org.athena.imis.diachron.archive.datamapping.MultidimensionalConverter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.diachron.detection.exploit.ArchiveExploiter;
import java.net.URI;
import java.util.*;


@Path("GeneralUploadRDF")
public class GeneralUploadRDFResource
{
    @Context
    private UriInfo context;

    public GeneralUploadRDFResource()
    {
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)    
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDiachronizeAndStore(
    		@FormDataParam("file") InputStream fileInputStream,
                @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
                @FormDataParam("datasetName") String datasetName,
                @FormDataParam("label") String label,
                @FormDataParam("creator") String creator,
                @FormDataParam("converterType") String converterType,
                @FormDataParam("reasoner") String reasoner,
                @FormDataParam("filters") String filtersString,
                @FormDataParam("format") String format)
    {
        FileInputStream fis = null;
        JSONObject jsonOutputMessage = new JSONObject();        
        PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();
        String serverLocationFolder = propertiesManager.getPropertyValue("ServerTempFolder");
        System.err.println(" ---- " + serverLocationFolder);
        String inPath  = serverLocationFolder + contentDispositionHeader.getFileName();
        String outPath = serverLocationFolder + "dian" + contentDispositionHeader.getFileName();
        String diachronicURL = null;
        Response responseMessage = null;
        Status returnStatus = null;
        Collection<URI> filters = null;
        List l = new ArrayList();
        
        try
        {
            
            //createDiachronicDataset("test", "098F6BCD4621D373CADE4E832627B4F6", "cre");
            if((diachronicURL=createDiachronicDataset(datasetName, label, creator))!=null)
            {
                FileManagement.storeFile(fileInputStream, inPath);
                
                fis = new FileInputStream(inPath);
                FileOutputStream fos = new FileOutputStream(outPath);
                
                if(converterType.equals("ontology"))
                {
                    OntologyConverter converter = new OntologyConverter();
                    
                    if(!filtersString.equals(""))
                    {
                        l = new ArrayList<String>(Arrays.asList(filtersString.split(",")));
                        converter.convert(fis, fos,datasetName, l, reasoner);                    
                    }
                    else
                    {
                        converter.convert(fis, fos,datasetName, reasoner);
                    }
                    //converter.convert(fis, fos, datasetName);
                }
                else if(converterType.equals("multidimensional"))
                {
                    MultidimensionalConverter converter = new MultidimensionalConverter();
                    converter.convert(fis, fos, "rdf", datasetName);
                }

                fis.close();
                fos.close();            

                FileInputStream fis2 = new FileInputStream(outPath);
                System.err.println(" ---------" + outPath + " " + diachronicURL);                
                System.err.println(" ---------" + (fis2==null) + " " + diachronicURL);

                StoreFactory.createDataLoader().loadData(fis2,
                                            diachronicURL,
                                            format);
                
                fis2.close();
                
                FileManagement.deleteFile(inPath);
                FileManagement.deleteFile(outPath);
                
                Properties chDet = new Properties();
                InputStream input = this.getClass().getClassLoader().getResourceAsStream("diachron.properties");                
                chDet.load(input);

                ArchiveExploiter expl = new ArchiveExploiter(chDet);
                expl.addDiachronicDataset(diachronicURL, datasetName);
                                
                jsonOutputMessage.put("Status", diachronicURL + " is stored");
                returnStatus = Response.Status.OK;
                //System.err.println("added to changes ontology as well");
                //System.err.println(jsonOutputMessage.toString());
                
                    if(!filtersString.equals(""))
                    {
                        System.err.println("Filters on");
                    }
                    else
                    {
                        System.err.println("Filters off");                        
                    }                
            }
            else 
            {
                jsonOutputMessage.put("Status", diachronicURL + " already exist so no operation occured");
                returnStatus = Response.Status.PRECONDITION_FAILED;
                System.err.println(jsonOutputMessage.toString());                
            }            
        }
        catch (Exception ex)
        {
            try {            
                jsonOutputMessage.put("Status", "Error in processing");
                returnStatus = Response.Status.INTERNAL_SERVER_ERROR;                
                System.err.println(jsonOutputMessage.toString());                
            } catch (JSONException ex1) {
                Logger.getLogger(GeneralUploadRDFResource.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(GeneralUploadRDFResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.status(returnStatus).entity(jsonOutputMessage.toString()).build();
    }

    private String createDiachronicDataset(String datasetName, String label, String creator)
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
        return jsonOutputMessage.getString("data");
    } 

    public static void main(String[] args)
    {
        FileInputStream fis = null;
        String inPath  = new String("/home/panos/Downloads/dp/maires.2012.ttl");
        String outPath = new String("/home/panos/Downloads/dp/maires.2012.ttl.test");
        String diachronicURL = null;
        String converterType = new String("multidimensional");
        GeneralUploadRDFResource g = new GeneralUploadRDFResource();
        String dataset = new String("multitest");
        String reasoner = new String("hermit");
        
        try
        {
            //if((diachronicURL=g.createDiachronicDataset(dataset, "multi", "peter"))!=null)
            //{
                //FileManagement.storeFile(fileInputStream, inPath);
                
                fis = new FileInputStream(inPath);
                FileOutputStream fos = new FileOutputStream(outPath);
                
                if(converterType.equals("ontology"))
                {
                    OntologyConverter converter = new OntologyConverter();                                   
                    converter.convert(fis, fos, dataset, reasoner);
                }
                else if(converterType.equals("multidimensional"))
                {
                    MultidimensionalConverter converter = new MultidimensionalConverter();
                    converter.convert(fis, fos, "rdf", dataset);
                }

                fis.close();
                fos.close();            

                /*
                FileInputStream fis2 = new FileInputStream(outPath);
                System.err.println(" ---------" + outPath + " " + diachronicURL);                
                System.err.println(" ---------" + (fis2==null) + " " + diachronicURL);
                
                StoreFactory.createDataLoader().loadData(fis2,
                                            diachronicURL,
                                            "RDF/XML");
                
                fis2.close();
                */
                //FileManagement.deleteFile(inPath);
                //FileManagement.deleteFile(outPath);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }        
    }
}
