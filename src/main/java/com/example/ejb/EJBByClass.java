package com.example.ejb;

import java.io.Serializable;

import jakarta.ejb.Stateless;

@Stateless(name = "EJBByClass")
public class EJBByClass implements EJBI, Serializable {
    
}
