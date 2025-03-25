package com.example.space;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
class StarshipWithMockTest {

  @Inject
  Starship starship;

  @Produces
  @Mock
  Engine engine;

  @Test
  void testStart() {
    starship.start();
    Mockito.verify(engine, Mockito.atLeastOnce()).start();
  }

  @Test
  void failToStart(){
    doThrow(new RuntimeException("Engine failed to start")).when(engine).start();
    assertThrows(RuntimeException.class, () -> starship.start());
  }
}