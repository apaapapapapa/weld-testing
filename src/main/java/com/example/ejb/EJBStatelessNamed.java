package com.example.ejb;

import java.io.Serializable;

import jakarta.ejb.Stateless;

@Stateless(name = "statelessNamed")
public class EJBStatelessNamed implements EJBI, Serializable {
    
}
