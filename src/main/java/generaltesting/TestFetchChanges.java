package generaltesting;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class TestFetchChanges 
{
    public static void main(String[] args) throws Exception 
    {
        Client client = Client.create();        
        //WebResource initRequest = client.resource("http://192.168.3.8:7090/DIACHRONIntegrationLayer/webresources/dataset/changes?from=http://www.diachron-fp7.eu/resource/recordset/efo/2.34&to=http://www.diachron-fp7.eu/resource/recordset/efo/2.35&id=http://www.ebi.ac.uk/efo/");
        //WebResource initRequest = client.resource("http://192.168.3.8:7090/DIACHRONIntegrationLayer/webresources/dataset/changes/any?from=http://dev.diachron-fp7.eu/resource/dataset/efo/2.37&to=http://dev.diachron-fp7.eu/resource/dataset/efo/2.38&id=http://www.diachron-fp7.eu/resource/diachronicDataset/EFO_Test_Strategies/CDAAF2AE5D9F7726789EFE06C84386E8");
        WebResource initRequest = client.resource("http://192.168.3.8:7090/DIACHRONIntegrationLayer/webresources/dataset/changes/any?from=http://www.diachron-fp7.eu/resource/dataset/test23/1449675534090/0EAE4C0A2924CE9E06A5412516A83AC3&to=http://www.diachron-fp7.eu/resource/dataset/test29/1449676054048/6FFB3396E4062749578D80EB8128AB94&id=http://www.diachron-fp7.eu/resource/diachronicDataset/test23/trol");

        System.out.println(initRequest.get(ClientResponse.class).getEntity(String.class));
        
        //initRequest = client.resource("http://192.168.3.8:7090/DIACHRONIntegrationLayer/webresources/dataset/version?id=http://www.diachron-fp7.eu/resource/diachronicDataset/test18/DF71DF92C31111F810A7D89BD2C2E35D");

        //System.out.println(initRequest.get(ClientResponse.class).getEntity(String.class));        

    }    
}
