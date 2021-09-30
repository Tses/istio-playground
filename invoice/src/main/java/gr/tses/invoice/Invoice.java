package gr.tses.invoice;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;



import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;




@Path("/invoice")
public class Invoice {



    @Inject
    @ConfigProperty(name = "gr.tses.calcurl")
    private String calcurl;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String createInvoice(@Context HttpHeaders headers) throws ParseException, ClientProtocolException, IOException {


        String qa = headers.getRequestHeader("qa").size() !=0 ?headers.getRequestHeader("qa").get(0):"";

        return "Invoice " + getId(qa) + " created";
    }


    private String getId(String qa) throws ParseException, ClientProtocolException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpGet request = new HttpGet(calcurl);

            // add request headers
            request.addHeader("qa", qa);
            
            CloseableHttpResponse response = httpClient.execute(request);

            try {
                response.addHeader("Content-Type","application/json");

                // Get HttpResponse Status
                //System.out.println(response.getProtocolVersion());              // HTTP/1.1
                //System.out.println(response.getStatusLine().getStatusCode());   // 200
                //System.out.println(response.getStatusLine().getReasonPhrase()); // OK
                //System.out.println(response.getStatusLine().toString());        // HTTP/1.1 200 OK

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);


                    return result;
                }
                return null;

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }        
    }

}