package com.example.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
@AdditionalClasses({TaskRepository.class, EntityManagerProducer.class})
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

    @BeforeEach
    @DataSet(value = "datasets/tasks.yml")
    void setup(){
    }

    @InRequestScope
    @Test
    @ExpectedDataSet("datasets/expected_tasks_after_insert.yml")
    void testSaveTask() {

        entityManager.getTransaction().begin(); 

        Task task = new Task();
        task.setTitle("task3");
        assertDoesNotThrow(() -> taskRepository.create(task));

        entityManager.getTransaction().commit();
    }

    @InRequestScope
    @Test
    void testFindAll() {
        List<Task> tasks = taskRepository.findAll();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }

    @InRequestScope
    @Test
    void testFindById() {
        Optional<Task> retrievedTask = taskRepository.findById(101);
        assertTrue(retrievedTask.isPresent());
        assertEquals("task1", retrievedTask.get().getTitle());
    }

    @InRequestScope
    @Test
    @ExpectedDataSet("datasets/expected_tasks_after_update.yml")
    void testUpdateTask() {

        entityManager.getTransaction().begin(); 

        Task updatedTask = taskRepository.update(101, "updatedTask");
        assertNotNull(updatedTask);
        assertEquals("updatedTask", updatedTask.getTitle());

        entityManager.getTransaction().commit();
    }

    @InRequestScope
    @Test
    @ExpectedDataSet("datasets/expected_tasks_after_delete.yml")
    void testDeleteTask() {

        entityManager.getTransaction().begin(); 

        assertDoesNotThrow(() -> taskRepository.delete(101));

        entityManager.getTransaction().commit();
    }

    @InRequestScope
    @Test
    void testSequentialCreateTasks() {

        entityManager.getTransaction().begin(); // ✅ トランザクション開始

        // 1回目のタスク作成
        Task task1 = new Task();
        task1.setTitle("Task 1");
        taskRepository.create(task1);

        // 2回目のタスク作成
        Task task2 = new Task();
        task2.setTitle("Task 2");
        taskRepository.create(task2);

        entityManager.getTransaction().commit(); // ✅ トランザクション終了

        // ✅ 2つのタスクが保存されていることを確認
        assertThat(task1.getId()).isNotZero();
        assertThat(task2.getId()).isNotZero();

        // ✅ Task2 の ID は Task1 の ID +1 であることを確認
        assertEquals(task1.getId() + 1, task2.getId(), "Task2 ID should be Task1 ID + 1");

    }

}
