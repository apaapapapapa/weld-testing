package com.example.web;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;

@RequestScoped
@Path("/example")
public class ExampleWebService {
  
    @Context
    HttpHeaders headers;

    @GET
    @Path("/client-info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientInfo() {

        List<String> userAgentList = headers.getRequestHeader("User-Agent");
        String userAgent = userAgentList != null && !userAgentList.isEmpty() ? userAgentList.get(0) : "Unknown";

        return Response.ok("{\"userAgent\": \"" + userAgent + "\"}").build();
    }
  
  }