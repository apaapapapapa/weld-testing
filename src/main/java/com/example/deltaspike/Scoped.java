package com.example.deltaspike;

import org.apache.deltaspike.core.api.exclude.Exclude;

import lombok.NoArgsConstructor;

@Exclude
@NoArgsConstructor
public class Scoped {

    private Runnable disposeListener;

    public void setDisposedListener(Runnable disposeListener) {
        this.disposeListener = disposeListener;

    }

    public void dispose() {
        disposeListener.run();
    }
}