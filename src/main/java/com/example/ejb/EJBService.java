package com.example.ejb;

import jakarta.ejb.EJB;
import lombok.Getter;

@Getter
public class EJBService {

    @EJB
    EJBI inject;

    @EJB(beanName = "statelessNamed")
    EJBI injectNamed;

    @EJB(beanName = "EJBByClass")
    EJBI injectStateless;
    
}
