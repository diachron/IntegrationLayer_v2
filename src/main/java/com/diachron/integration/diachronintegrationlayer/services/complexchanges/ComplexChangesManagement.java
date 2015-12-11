package com.diachron.integration.diachronintegrationlayer.services.complexchanges;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.diachron.detection.complex_change.CCDefinitionError.CODE;
import org.diachron.detection.complex_change.CCManager;
import org.diachron.detection.repositories.JDBCVirtuosoRep;
import org.diachron.detection.utils.JSONMessagesParser;
import org.diachron.detection.utils.MCDUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import management.configuration.PropertiesManager;
import utils.Utils;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("complex_change")
public class ComplexChangesManagement {

    PropertiesManager propertiesManager = PropertiesManager.getPropertiesManager();

    public ComplexChangesManagement() {
    }

    @GET
    public Response getCCJSON(@QueryParam("name") String name,
            @QueryParam("dataset_uri") String datasetUri) {
        boolean result = false;
        String message = null;
        int code = 0;
        JDBCVirtuosoRep jdbcRep;
        try {
            jdbcRep = new JDBCVirtuosoRep(propertiesManager.getProperties());
        } catch (Exception ex) {
            result = false;
            String json = "{ \"Message\" : \"Exception Occured: " + ex.getMessage() + ", \"Result\" : " + result + " }";
            return Response.status(400).entity(json).build();
        }
        if (datasetUri == null) {
            datasetUri = propertiesManager.getPropertyValue("Dataset_URI");
        }
        String ontologySchema = Utils.getDatasetSchema(datasetUri);
        String query = "select ?json from <" + ontologySchema + "> where { ?s co:name \"" + name + "\"; co:json ?json. }";
        ResultSet res = jdbcRep.executeSparqlQuery(query, false);
        try {
            if (res.next()) {
                message = (String) res.getString("json");
                result = true;
                code = 200;
            } else {
                message = "\"Complex change was not found in the ontology of changes.\"";
                result = false;
                code = 204;
            }
        } catch (SQLException ex) {
            message = ex.getMessage();
            result = false;
            code = 400;
        }
        jdbcRep.terminate();
        String json = "{ \"Message\" : " + message + ", \"Result\" : " + result + " }";
        return Response.status(code).entity(json).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCCJSON(@QueryParam("name") String name,
            @QueryParam("dataset_uri") String datasetUri) {
        String message = null;
        int code;
        boolean result = false;
        if (datasetUri == null) {
            datasetUri = propertiesManager.getPropertyValue("Dataset_URI");
        }
        try {
            MCDUtils utils = new MCDUtils(propertiesManager.getProperties(), datasetUri, false);
            result = utils.deleteCC(name);
            message = null;
            if (result) {
                code = 200;
                message = "Complex Change was successfully deleted from the ontology of changes.";
            } else {
                code = 204;
                message = "Complex Change was not found in the ontology of changes.";
            }
            String json = "{ \"Message\" : \"" + message + "\", \"Result\" : " + result + " }";
            utils.terminate();
            return Response.status(code).entity(json).build();
        } catch (Exception ex) {
            result = false;
            String json = "{ \"Message\" : \"Exception Occured: " + ex.getMessage() + ", \"Result\" : " + result + " }";
            return Response.status(400).entity(json).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response defineCCJSON(String inputMessage) {
        JSONParser jsonParser = new JSONParser();
        CCManager ccDef;
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(inputMessage);
            String datasetUri = (String) jsonObject.get("Dataset_URI");
            if (datasetUri == null) {
                datasetUri = propertiesManager.getPropertyValue("Dataset_URI");
            }
            String changesOntologySchema = Utils.getDatasetSchema(datasetUri);
            String ccJson = (String) jsonObject.get("CC_Definition");
            ccDef = JSONMessagesParser.createCCDefinition(propertiesManager.getProperties(), ccJson, changesOntologySchema);
        } catch (Exception ex) {
            boolean result = false;
            String json = "{ \"Message\" : \"Exception Occured: " + ex.getMessage() + ", \"Result\" : " + result + " }";
            return Response.status(400).entity(json).build();
        }
        if (ccDef == null) {
            String message = "JSON input message could not be parsed.";
            String json = "{ \"Message\" : \"" + message + "\", \"Success\" : false }";
            return Response.status(400).entity(json).build();
        } else {
            String message = null;
            ccDef.insertChangeDefinition();
            int code;
            boolean result;
            if (ccDef.getCcDefError().getErrorCode() == CODE.NON_UNIQUE_CC_NAME) {
                code = 204;
                message = ccDef.getCcDefError().getDescription();
                result = false;
            } else if (ccDef.getCcDefError().getErrorCode() == null) {
                code = 200;
                message = "Complex Change's definition was inserted in the ontology of changes.";
                result = true;
            } else {
                code = 400;
                message = ccDef.getCcDefError().getDescription();
                result = false;
            }
            String json = "{ \"Message\" : \"" + message + "\", \"Success\" : " + result + " }";
            ccDef.terminate();
            return Response.status(code).entity(json).build();
        }
    }
}
