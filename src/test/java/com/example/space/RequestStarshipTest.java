package com.example.space;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ContextController;
import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses({RequestScopedWarpDrive.class})
class RequestStarshipTest {

    @Inject
    ContextController contextController; //Obtain an instance of the context controller.

    @Inject
    RequestStarship requestStarship;

    @Test
    @InRequestScope //This test will be run within the context of a request
    void testStartWithAnnotation() {
        assertRequestStarshipStart();
    }

    @Test
    void testStartWithContextController() {
        contextController.openRequest(); //Start a new request
        assertRequestStarshipStart();
        contextController.closeRequest(); //Close the current request.
    }

    void assertRequestStarshipStart(){

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> requestStarship.start());

        assertEquals("Unimplemented RequestScopedWarpDrive method 'start'", exception.getMessage());

    }
    
}
