package com.example.deltaspike;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.deltaspike.SupportDeltaspikeData;
import io.github.cdiunit.deltaspike.SupportDeltaspikeJpa;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.transaction.Transactional;

@SupportDeltaspikeJpa
@SupportDeltaspikeData
@ExtendWith(CdiJUnit5Extension.class)
class DeltaspikeTransactionsTest {

    @Inject
    HumanRepository er;

    @Produces
    @RequestScoped
    EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("DefaultPersistenceUnit").createEntityManager();
    }

    @InRequestScope
    @Transactional
    @Test
    void testSaveHuman() {
        Human human = new Human();
        assertDoesNotThrow(() -> er.save(human));
    }

    @InRequestScope
    @Transactional
    @Test
    void testSelectHuman() {
        assertDoesNotThrow(() -> er.findAll());
    }
    
}
