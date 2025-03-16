package com.example.space;

import jakarta.inject.Inject;

public class Starship { // We want to test this!

    @Inject
    Engine engine;
  
    public void start() {
      engine.start();
    }
  }