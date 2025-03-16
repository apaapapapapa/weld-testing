package com.example.space;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses(WarpDrive.class)
class StarshipWarpDriveTest {

  @Inject
  Starship starship;

  @Test
  void testStart() {

    Exception exception = assertThrows(UnsupportedOperationException.class, () -> starship.start());

    assertEquals("Unimplemented WarpDrive method 'start'", exception.getMessage());
    
  }
}