package com.example.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class TaskControllerTest {

    private TaskController controller;
    private TaskRepository mockRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new TaskController();
        mockRepository = Mockito.mock(TaskRepository.class);
        // リフレクションでtaskRepositoryフィールドにセット
        Field repoField = TaskController.class.getDeclaredField("taskRepository");
        repoField.setAccessible(true);
        repoField.set(controller, mockRepository);
    }

    @Test
    @DisplayName("タスクが0件の場合は進捗率0.0")
    void testNoTasks() {
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());
        assertEquals(0.0, controller.calculateProgressRate(), 0.0001);
    }

    @Test
    @DisplayName("全て未完了の場合は進捗率0.0")
    void testAllIncomplete() {
        Task t1 = new Task();
        t1.setCompleted(false);
        Task t2 = new Task();
        t2.setCompleted(false);
        Task t3 = new Task();
        t3.setCompleted(false);
        List<Task> tasks = Arrays.asList(t1, t2, t3);
        when(mockRepository.findAll()).thenReturn(tasks);
        assertEquals(0.0, controller.calculateProgressRate(), 0.0001);
    }

    @Test
    @DisplayName("全て完了の場合は進捗率100.0")
    void testAllComplete() {
        Task t1 = new Task();
        t1.setCompleted(true);
        Task t2 = new Task();
        t2.setCompleted(true);
        Task t3 = new Task();
        t3.setCompleted(true);
        List<Task> tasks = Arrays.asList(t1, t2, t3);
        when(mockRepository.findAll()).thenReturn(tasks);
        assertEquals(100.0, controller.calculateProgressRate(), 0.0001);
    }

    @Test
    @DisplayName("1件だけ完了の場合は進捗率33.333...%")
    void testOneComplete() {
        Task t1 = new Task();
        t1.setCompleted(true);
        Task t2 = new Task();
        t2.setCompleted(false);
        Task t3 = new Task();
        t3.setCompleted(false);
        List<Task> tasks = Arrays.asList(t1, t2, t3);
        when(mockRepository.findAll()).thenReturn(tasks);
        assertEquals(33.3333, controller.calculateProgressRate(), 0.01);
    }

    @Test
    @DisplayName("2件完了の場合は進捗率66.666...%")
    void testTwoComplete() {
        Task t1 = new Task();
        t1.setCompleted(true);
        Task t2 = new Task();
        t2.setCompleted(true);
        Task t3 = new Task();
        t3.setCompleted(false);
        List<Task> tasks = Arrays.asList(t1, t2, t3);
        when(mockRepository.findAll()).thenReturn(tasks);
        assertEquals(66.6666, controller.calculateProgressRate(), 0.01);
    }

    @Test
    @DisplayName("completedがnullのタスクが混在する場合")
    void testWithNullCompleted() {
        Task t1 = new Task();
        t1.setCompleted(true);
        Task t2 = new Task();
        t2.setCompleted(null);
        Task t3 = new Task();
        t3.setCompleted(false);
        List<Task> tasks = Arrays.asList(t1, t2, t3);
        when(mockRepository.findAll()).thenReturn(tasks);
        // nullは未完了扱い（filter(Task::getCompleted)で除外される）
        assertEquals(33.3333, controller.calculateProgressRate(), 0.01);
    }
}
