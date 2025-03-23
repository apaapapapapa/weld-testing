package com.example.deltaspike;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ContextController;
import io.github.cdiunit.junit5.CdiJUnit5Extension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses({ ScopedFactory.class, ExcludeExtension.class })
class TestDeltaspikeExclude {

    @Inject
    private ContextController contextController;

    @Inject
    private Provider<Scoped> scoped;

    @Mock
    private Runnable disposeListener;

    @Test
    void testContextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);
        b1.setDisposedListener(disposeListener);
        contextController.closeRequest();
        Mockito.verify(disposeListener).run();
    }

}