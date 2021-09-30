package gr.tses.calculator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.resteasy.spi.HttpRequest;

@Path("/calc")
public class CalcEngine {

    @Context
    private HttpRequest servletRequest;


    @Inject
    State state;  

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String calc() throws UnknownHostException {
        if (state.getException()==1){
            throw new RuntimeException();
        }
        return  "ID-" + RandomStringUtils.randomAlphanumeric(10)  + "-" + InetAddress.getLocalHost().getHostName();
        
    }


    @GET
    @Path("/status")
    public Response status(@QueryParam("e") String exception,@QueryParam("t") String timeout) throws InterruptedException {   
                
        state.setException(Integer.parseInt(exception));
        state.setTimeout(Integer.parseInt(timeout));
        return Response.ok("TimeOut:" + timeout + " Exception:" + exception).build();       
    }     
}