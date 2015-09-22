/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generaltesting;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author panos
 */
public class TestChangeDetection
{
    public static void main(String[] args) throws Exception 
    {    
        Client client = Client.create();        
        WebResource initRequest = client.resource("http://192.168.3.8:9090/DIACHRONIntegrationLayer/webresources/dataset/changes?from=http://www.diachron-fp7.eu/resource/recordset/efo/2.34&to=http://www.diachron-fp7.eu/resource/recordset/efo/2.35&id=http://www.ebi.ac.uk/efo");

        System.out.println(initRequest.get(ClientResponse.class).getEntity(String.class));
    }
}
        
