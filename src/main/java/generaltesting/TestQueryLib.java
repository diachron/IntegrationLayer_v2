package generaltesting;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.athena.imis.diachron.archive.api.QueryLib;

public class TestQueryLib 
{
    public static void main(String[] args) throws Exception 
    {
        Client client = Client.create();        
        WebResource initRequest = client.resource("http://192.168.3.8:9090/DIACHRONIntegrationLayer/webresources/dataset/");

        System.out.println(initRequest.get(ClientResponse.class).getEntity(String.class));
        
        initRequest = client.resource("http://192.168.3.8:9090/DIACHRONIntegrationLayer/webresources/dataset/version?id=http://www.diachron-fp7.eu/resource/diachronicDataset/test18/DF71DF92C31111F810A7D89BD2C2E35D");

        System.out.println(initRequest.get(ClientResponse.class).getEntity(String.class));        

    }    
}
