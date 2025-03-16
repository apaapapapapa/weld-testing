package com.example.space;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.ActivatedAlternatives;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
@ActivatedAlternatives(TranswarpDrive.class)
class StarshipAlternativeTest {

    @Inject
    Starship starship;

    @Test
    void testStart() {

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> starship.start());

        assertEquals("Unimplemented TranswarpDrive method 'start'", exception.getMessage());
        
    }
    
}
