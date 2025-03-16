package com.example.space;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses(WarpDrive.class) // Normally this implementation would be used
class StarshipTranswarpDriveWithMockTest {

  @Inject
  Starship starship;

  @Produces
  @ProducesAlternative // This mock will be used instead!
  @Mock
  Engine engine;

  @Test
  void testStart() {
    starship.start();
    Mockito.verify(engine, Mockito.atLeastOnce()).start();
  }
}