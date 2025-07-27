package com.example.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.EJBException;
import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Nested
    @DisplayName("calculateProgressRateのテスト")
    class CalculateProgressRateTests {
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

    @Nested
    @DisplayName("calculateDelayRiskRateのテスト")
    class CalculateDelayRiskRateTests {
        @Test
        @DisplayName("遅延リスク高タスク割合: タスク0件は0.0")
        void testNoTasks() {
            when(mockRepository.findAll()).thenReturn(Collections.emptyList());
            assertEquals(0.0, controller.calculateDelayRiskRate(), 0.0001);
        }

        @Test
        @DisplayName("遅延リスク高タスク割合: 条件に合うタスクが複数")
        void testHighRiskTasks() {
            LocalDate today = LocalDate.now();
            Task t1 = new Task(); t1.setCompleted(false); t1.setDueDate(today.plusDays(1));
            Task t2 = new Task(); t2.setCompleted(false); t2.setDueDate(today.plusDays(2));
            Task t3 = new Task(); t3.setCompleted(true);  t3.setDueDate(today.plusDays(1));
            Task t4 = new Task(); t4.setCompleted(false); t4.setDueDate(today.plusDays(5)); // 範囲外
            List<Task> tasks = Arrays.asList(t1, t2, t3, t4);
            when(mockRepository.findAll()).thenReturn(tasks);
            // t1, t2が高リスク(未完了かつ3日以内)→2/4=50%
            assertEquals(50.0, controller.calculateDelayRiskRate(), 0.0001);
        }

        @Test
        @DisplayName("遅延リスク高タスク割合: すべて完了済み")
        void testAllCompleted() {
            LocalDate today = LocalDate.now();
            Task t1 = new Task(); t1.setCompleted(true); t1.setDueDate(today.plusDays(1));
            Task t2 = new Task(); t2.setCompleted(true); t2.setDueDate(today.plusDays(2));
            when(mockRepository.findAll()).thenReturn(Arrays.asList(t1, t2));
            assertEquals(0.0, controller.calculateDelayRiskRate(), 0.0001);
        }

        @Test
        @DisplayName("遅延リスク高タスク割合: 期日が範囲外")
        void testDueDateOutOfRange() {
            LocalDate today = LocalDate.now();
            Task t1 = new Task(); t1.setCompleted(false); t1.setDueDate(today.plusDays(10));
            when(mockRepository.findAll()).thenReturn(Arrays.asList(t1));
            assertEquals(0.0, controller.calculateDelayRiskRate(), 0.0001);
        }
    }

    @Nested
    @DisplayName("findHighRiskTasksのテスト")
    class FindHighRiskTasksTests {
        @Test
        @DisplayName("遅延リスク高タスクリスト: 条件に合うタスクのみ抽出")
        void testFindHighRiskTasks() {
            LocalDate today = LocalDate.now();
            Task t1 = new Task(); t1.setCompleted(false); t1.setDueDate(today.plusDays(1));
            Task t2 = new Task(); t2.setCompleted(false); t2.setDueDate(today.plusDays(4)); // 範囲外
            Task t3 = new Task(); t3.setCompleted(true);  t3.setDueDate(today.plusDays(2)); // 完了済み
            List<Task> tasks = Arrays.asList(t1, t2, t3);
            when(mockRepository.findAll()).thenReturn(tasks);
            List<Task> result = controller.findHighRiskTasks();
            assertEquals(1, result.size());
            assertTrue(result.contains(t1));
        }
    }

    @Nested
    @DisplayName("loadAllのテスト")
    class LoadAllTests {
        @Test
        @DisplayName("全タスク取得: findAllの結果がそのまま返る")
        void testLoadAll() {
            Task t1 = new Task();
            Task t2 = new Task();
            List<Task> tasks = Arrays.asList(t1, t2);
            when(mockRepository.findAll()).thenReturn(tasks);
            assertEquals(tasks, controller.loadAll());
        }
    }

    @Nested
    @DisplayName("addのテスト")
    class AddTests {
        @Test
        @DisplayName("タスク追加: 正常系")
        void testNormal() {
            LocalDate due = LocalDate.now().plusDays(1);
            Task created = new Task();
            when(mockRepository.create(any(Task.class))).thenReturn(created);
            Task result = controller.add("タイトル", due, false);
            assertEquals(created, result);
        }

        @Test
        @DisplayName("タスク追加: タイトルnullで例外")
        void testTitleNull() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.add(null, due, false));
        }

        @Test
        @DisplayName("タスク追加: タイトル空で例外")
        void testTitleEmpty() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.add("", due, false));
        }

        @Test
        @DisplayName("タスク追加: タイトル空白のみで例外")
        void testTitleBlank() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.add("   ", due, false));
        }

        @Test
        @DisplayName("タスク追加: 期限nullで例外")
        void testDueDateNull() {
            assertThrows(IllegalArgumentException.class, () -> controller.add("タイトル", null, false));
        }

        @Test
        @DisplayName("タスク追加: 期限が過去で例外")
        void testDueDatePast() {
            LocalDate past = LocalDate.now().minusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.add("タイトル", past, false));
        }
    }

    @Nested
    @DisplayName("deleteのテスト")
    class DeleteTests {
        @Test
        @DisplayName("タスク削除: 正常系")
        void testNormal() {
            Task t = new Task();
            when(mockRepository.findById(1)).thenReturn(Optional.of(t));
            doNothing().when(mockRepository).delete(1);
            Task result = controller.delete(1);
            assertEquals(t, result);
        }

        @Test
        @DisplayName("タスク削除: 存在しないIDで例外")
        void testNotFound() {
            when(mockRepository.findById(999)).thenReturn(Optional.empty());
            assertThrows(EJBException.class, () -> controller.delete(999));
        }
    }

    @Nested
    @DisplayName("updateのテスト")
    class UpdateTests {
        @Test
        @DisplayName("タスク更新: 正常系")
        void testNormal() {
            LocalDate due = LocalDate.now().plusDays(1);
            Task updated = new Task();
            when(mockRepository.update(eq(1), eq("タイトル"), eq(due), eq(true))).thenReturn(updated);
            Task result = controller.update(1, "タイトル", due, true);
            assertEquals(updated, result);
        }

        @Test
        @DisplayName("タスク更新: タイトルnullで例外")
        void testTitleNull() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.update(1, null, due, false));
        }

        @Test
        @DisplayName("タスク更新: タイトル空で例外")
        void testTitleEmpty() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.update(1, "", due, false));
        }

        @Test
        @DisplayName("タスク更新: タイトル空白のみで例外")
        void testTitleBlank() {
            LocalDate due = LocalDate.now().plusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.update(1, "   ", due, false));
        }

        @Test
        @DisplayName("タスク更新: 期限nullで例外")
        void testDueDateNull() {
            assertThrows(IllegalArgumentException.class, () -> controller.update(1, "タイトル", null, false));
        }

        @Test
        @DisplayName("タスク更新: 期限が過去で例外")
        void testDueDatePast() {
            LocalDate past = LocalDate.now().minusDays(1);
            assertThrows(IllegalArgumentException.class, () -> controller.update(1, "タイトル", past, false));
        }

        @Test
        @DisplayName("タスク更新: 存在しないIDで例外")
        void testNotFound() {
            LocalDate due = LocalDate.now().plusDays(1);
            when(mockRepository.update(eq(999), eq("タイトル"), eq(due), eq(false)))
                .thenThrow(new EntityNotFoundException("not found"));
            assertThrows(EJBException.class, () -> controller.update(999, "タイトル", due, false));
        }
    }
}
