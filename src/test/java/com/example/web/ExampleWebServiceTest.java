package com.example.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.jaxrs.SupportJaxRs;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

@ExtendWith({CdiJUnit5Extension.class})
@SupportJaxRs
@Disabled
class ExampleWebServiceTest {

    @Inject
    ExampleWebService exampleWebService;

    @Produces
    @Mock
    HttpHeaders mockHeaders;

    @InRequestScope
    @Test
    void testGetClientInfo() {

        when(mockHeaders.getRequestHeader("User-Agent")).thenReturn(Collections.singletonList("Test-Agent"));

        Response response = exampleWebService.getClientInfo();

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("{\"userAgent\": \"Test-Agent\"}", response.getEntity().toString());
    }
    
}
