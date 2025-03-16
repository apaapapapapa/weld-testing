package com.example.space;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;

@ExtendWith(CdiJUnit5Extension.class)
class TestStarship {

  @Inject
  Starship starship;

  @Test
  void testStart() {
    assertThrows(UnsupportedOperationException.class, () -> starship.start());
  }
}