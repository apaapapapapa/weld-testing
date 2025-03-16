package com.example.space;

import jakarta.inject.Inject;

public class Starship { // We want to test this!

    @Inject
    private WarpDrive engine;
  
    public void start() {
      engine.start();
    }
  }