package com.example.ejb;

import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ejb.SupportEjb;
import io.github.cdiunit.junit5.CdiJUnit5Extension;

@ExtendWith(CdiJUnit5Extension.class)
@AdditionalClasses({EJB.class, EJBByClass.class, EJBStatelessNamed.class})
@SupportEjb
class EjbServiceTest {

    @Inject
    EJBService ejbService;

    @Test
    void testDefaultEjb() {
        assertThat(ejbService.getInject()).isNotNull().isInstanceOfAny(EJB.class);
    }

    @Test
    void testNamedEjb() {
        assertThat(ejbService.getInjectNamed()).isNotNull().isInstanceOf(EJBStatelessNamed.class);
    }

    @Test
    void testClassNamedEjb() {
        assertThat(ejbService.getInjectStateless()).isNotNull().isInstanceOf(EJBByClass.class);
    }

}