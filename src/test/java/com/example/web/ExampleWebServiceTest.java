package com.example.web;

import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.jaxrs.SupportJaxRs;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;
import jakarta.jws.WebService;

@ExtendWith({CdiJUnit5Extension.class})
@SupportJaxRs
class ExampleWebServiceTest {

    @Inject
    WebService webService;
    
}
