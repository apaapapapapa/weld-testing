package com.example.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ExtendWith({CdiJUnit5Extension.class})
@AdditionalClasses({TaskRepository.class, EntityManagerProducer.class})
class TaskRepositoryTest {

    @Inject
    TaskRepository taskRepository;

    @Inject
    EntityManager entityManager;

    @InRequestScope
    @Test
    void testSaveTask() {

        entityManager.getTransaction().begin(); 

        Task task = new Task();
        task.setTitle("task1");
        assertDoesNotThrow(() -> taskRepository.create(task));

        entityManager.getTransaction().commit();
    }

}
