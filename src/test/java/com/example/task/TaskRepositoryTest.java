package com.example.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ExtendWith({CdiJUnit5Extension.class})
@AdditionalClasses({EntityManagerProducer.class})
@DBRider
class TaskRepositoryTest {

    @Inject
    TaskRepository taskRepository;

    @Inject
    EntityManager entityManager;

    @BeforeAll
    @DataSet(executeScriptsBefore = "datasets/create_task.sql")
    static void initial(){
    }

    @InRequestScope
    @Test
    @ExpectedDataSet("datasets/expected_tasks_after_insert.yml")
    void testSaveTask() {

        entityManager.getTransaction().begin(); 

        Task task1 = new Task();
        task1.setTitle("task1");
        taskRepository.create(task1);

        Task task2 = new Task();
        task2.setTitle("task2");
        taskRepository.create(task2);

        entityManager.getTransaction().commit();

        assertThat(task1.getId()).isNotZero();
        assertThat(task2.getId()).isNotZero();

        assertEquals(task1.getId() + 1, task2.getId(), "Task2 ID should be Task1 ID + 1");
    }

    @InRequestScope
    @Test
    @DataSet(value = "datasets/tasks.yml")
    void testFindAll() {
        List<Task> tasks = taskRepository.findAll();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }

    @InRequestScope
    @Test
    @DataSet(value = "datasets/tasks.yml")
    void testFindById() {
        Optional<Task> retrievedTask = taskRepository.findById(1);
        assertTrue(retrievedTask.isPresent());
        assertEquals("task1", retrievedTask.get().getTitle());
    }

    @InRequestScope
    @Test
    @DataSet(value = "datasets/tasks.yml")
    @ExpectedDataSet("datasets/expected_tasks_after_update.yml")
    void testUpdateTask() {

        entityManager.getTransaction().begin(); 

        // 既存のdueDateとcompleted値を仮で指定（必要に応じて修正）
        java.time.LocalDate dueDate = java.time.LocalDate.of(2025, 7, 31);
        boolean completed = false;
        Task updatedTask = taskRepository.update(1, "updatedTask", dueDate, completed);
        assertNotNull(updatedTask);
        assertEquals("updatedTask", updatedTask.getTitle());

        entityManager.getTransaction().commit();
    }

    @InRequestScope
    @Test
    @DataSet(value = "datasets/tasks.yml")
    @ExpectedDataSet("datasets/expected_tasks_after_delete.yml")
    void testDeleteTask() {

        entityManager.getTransaction().begin(); 

        assertDoesNotThrow(() -> taskRepository.delete(1));

        entityManager.getTransaction().commit();
    }

}
