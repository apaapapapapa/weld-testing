package com.example.space;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class RequestStarship {

    @Inject
    Provider<Engine> engine; //If engine is at request scope then it must be accessed by provider.

    void start() {
        engine.get().start();
    }
}
