package com.diachron.integration.diachronintegrationlayer.services.complexchanges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.athena.imis.diachron.archive.api.QueryLib;
import org.diachron.detection.exploit.ArchiveExploiter;
import org.diachron.detection.exploit.ChangesExploiter;
import org.diachron.detection.exploit.DetChange;

@Path("dataset")
public class DatasetResource 
{

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of DatasetResource
     */
    public DatasetResource() {
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getDataset() {
        QueryLib querylib = new QueryLib();
        try {
            String response = querylib.listDiachronicDatasets();
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (Exception ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)        
    public Response getDatasetById(@QueryParam("id") String id)
    {
        QueryLib querylib = new QueryLib();
        
        System.err.println(" id = " + id);
        String response = querylib.getDatasetVersionById(id);        
        
        return Response.status(Response.Status.OK).entity(response).build();
    }
    
    @GET
    @Path("/changes")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getChangesBetweeenVersions(@QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("id") String _id)
    {
        Properties prop = new Properties();
        InputStream inputStream;
        
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream("diachron.properties");
            System.err.println(" ------ " + inputStream.toString());
            prop.load(inputStream);  
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        String json="";
        try {        
            ChangesExploiter exploiter = new ChangesExploiter(prop, _id, true);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Set<DetChange> changeSet = exploiter.fetchChangesBetweenVersions(from, to, null, null, 1000);
            json = gson.toJson(changeSet);
        } catch (Exception ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        return Response.status(Response.Status.OK).entity(json).build();
    }
    
    @GET
    @Path("/changes/any")
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getChangesBetweeenVersionsAnyID(@QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("id") String _id)
    {
        Properties prop = new Properties();
        InputStream inputStream;
        
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream("diachron.properties");
            System.err.println(" ------ " + inputStream.toString());
            prop.load(inputStream);  
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        String json="";
        try {        
            ChangesExploiter exploiter = new ChangesExploiter(prop, _id, true);
            ArchiveExploiter archexploiter = new ArchiveExploiter(prop);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            from = archexploiter.fetchChDetectVersion(from, _id);
            to   = archexploiter.fetchChDetectVersion(to, _id);
            Set<DetChange> changeSet = exploiter.fetchChangesBetweenVersions(from, to, null, null, 1000);
            json = gson.toJson(changeSet);
        } catch (Exception ex) {
            Logger.getLogger(DatasetResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        return Response.status(Response.Status.OK).entity(json).build();
    }

   
}
