package com.example.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

public class ExampleWebService {
    
    @Context
    HttpServletRequest request;
  
    @Context
    HttpServletResponse response;
  
    @Context
    ServletContext context;
  
    @Context
    UriInfo uriInfo;
  
    @Context
    Request jaxRsRequest;
  
    @Context
    SecurityContext securityContext;
  
    @Context
    Providers providers;
  
    @Context
    HttpHeaders headers;
  
  }