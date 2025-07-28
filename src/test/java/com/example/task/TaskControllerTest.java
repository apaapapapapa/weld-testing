package com.example.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import jakarta.ejb.EJBException;
import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TaskControllerのテスト")
class TaskControllerTest {

    private static final int EXISTING_TASK_ID = 1;
    private static final int NON_EXISTENT_TASK_ID = 999;
    private static final int PARENT_TASK_ID = 10;
    private static final int SUBTASK_PARENT_ID = 5;
    private static final int INVALID_PARENT_ID = 99;
    private static final double PROGRESS_ZERO = 0.0;
    private static final double PROGRESS_FULL = 100.0;
    private static final double PROGRESS_ONE_THIRD = 33.3333;
    private static final double PROGRESS_TWO_THIRD = 66.6666;
    private static final double DELAY_RISK_ZERO = 0.0;
    private static final double DELAY_RISK_HALF = 50.0;
    private static final double DELTA_STRICT = 0.0001;
    private static final double DELTA_TOLERANT = 0.01;
    private static final String SUBTASK_TITLE = "サブタスク";
    private static final String TASK_TITLE = "タイトル";

    private TaskController controller;
    private TaskRepository mockRepository;

    @BeforeEach
    void setUp() {
        mockRepository = Mockito.mock(TaskRepository.class);
        controller = new TaskController(mockRepository);
    }

    static class TaskFactory {
        // --- TaskFactory: テスト用Task生成ユーティリティ ---
        static Task task(Boolean completed, LocalDate dueDate) {
            Task t = new Task();
            t.setCompleted(completed);
            t.setDueDate(dueDate);
            return t;
        }

        static Task completedTask() {
            return task(true, null);
        }

        static Task incompleteTask() {
            return task(false, null);
        }

        static Task taskWithDue(Boolean completed, LocalDate dueDate) {
            return task(completed, dueDate);
        }
    }

    @Nested
    @DisplayName("calculateProgressRateのテスト")
    class CalculateProgressRateTests {

        @ParameterizedTest(name = "{index}: {3}")
        @MethodSource("com.example.task.TaskControllerTest$CalculateProgressRateTests#provideTasksForProgressRate")
        @DisplayName("[正常系][境界値] calculateProgressRateのパラメータライズドテスト")
        void testCalculateProgressRate(List<Task> tasks, double expected, double delta, String description) {

            when(mockRepository.findAll()).thenReturn(tasks);

            double actual = controller.calculateProgressRate();
            
            assertEquals(expected, actual, delta);
        }

        static Stream<Arguments> provideTasksForProgressRate() {

            return Stream.of(
                Arguments.of(
                    Collections.emptyList(), PROGRESS_ZERO, DELTA_STRICT, "タスクが0件の場合は進捗率0.0"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.incompleteTask(),
                        TaskFactory.incompleteTask(),
                        TaskFactory.incompleteTask()
                    ),
                    PROGRESS_ZERO, DELTA_STRICT, "全て未完了の場合は進捗率0.0"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.completedTask(),
                        TaskFactory.completedTask(),
                        TaskFactory.completedTask()
                    ),
                    PROGRESS_FULL, DELTA_STRICT, "全て完了の場合は進捗率100.0"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.completedTask(),
                        TaskFactory.incompleteTask(),
                        TaskFactory.incompleteTask()
                    ),
                    PROGRESS_ONE_THIRD, DELTA_TOLERANT, "1件だけ完了の場合は進捗率33.333...%"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.completedTask(),
                        TaskFactory.completedTask(),
                        TaskFactory.incompleteTask()
                    ),
                    PROGRESS_TWO_THIRD, DELTA_TOLERANT, "2件完了の場合は進捗率66.666...%"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.completedTask(),
                        TaskFactory.task(null, null),
                        TaskFactory.incompleteTask()
                    ),
                    PROGRESS_ONE_THIRD, DELTA_TOLERANT, "completedがnullのタスクが混在する場合はカウントされない"
                )
            );
        }
    }

    @Nested
    @DisplayName("[正常系][境界値] calculateDelayRiskRateのテスト")
    class CalculateDelayRiskRateTests {

        @ParameterizedTest(name = "{index}: {2}")
        @MethodSource("com.example.task.TaskControllerTest$CalculateDelayRiskRateTests#provideTasksForDelayRiskRate")
        @DisplayName("[境界値] calculateDelayRiskRateのパラメータライズドテスト")
        void testCalculateDelayRiskRate(List<Task> tasks, double expected, String description) {
            when(mockRepository.findAll()).thenReturn(tasks);
            double actual = controller.calculateDelayRiskRate();
            assertEquals(expected, actual, DELTA_STRICT);
        }

        static Stream<Arguments> provideTasksForDelayRiskRate() {
            LocalDate today = LocalDate.now();
            return Stream.of(
                Arguments.of(
                    Collections.emptyList(), DELAY_RISK_ZERO, "遅延リスク高タスク割合: タスク0件は0.0"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.taskWithDue(false, today.plusDays(1)),
                        TaskFactory.taskWithDue(false, today.plusDays(2)),
                        TaskFactory.taskWithDue(true, today.plusDays(1)),
                        TaskFactory.taskWithDue(false, today.plusDays(5))
                    ),
                    DELAY_RISK_HALF, "遅延リスク高タスク割合: 条件に合うタスクが半分"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.taskWithDue(true, today.plusDays(1)),
                        TaskFactory.taskWithDue(true, today.plusDays(2))
                    ),
                    DELAY_RISK_ZERO, "遅延リスク高タスク割合: すべて完了済み"
                ),
                Arguments.of(
                    Arrays.asList(
                        TaskFactory.taskWithDue(false, today.plusDays(10))
                    ),
                    DELAY_RISK_ZERO, "遅延リスク高タスク割合: 期日が範囲外"
                )
            );
        }
    }

    @Nested
    @DisplayName("[正常系] findHighRiskTasksのテスト")
    class FindHighRiskTasksTests {

        @Test
        @DisplayName("[正常系] 遅延リスク高タスクリスト: 条件に合うタスクのみ抽出")
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
    @DisplayName("[正常系] loadAllのテスト")
    class LoadAllTests {

        @Test
        @DisplayName("[正常系] 全タスク取得: findAllの結果がそのまま返る")
        void testLoadAll() {
            Task t1 = new Task();
            Task t2 = new Task();
            List<Task> tasks = Arrays.asList(t1, t2);
            when(mockRepository.findAll()).thenReturn(tasks);

            List<Task> result = controller.loadAll();

            assertEquals(tasks, result);
        }
    }

    @Nested
    @DisplayName("[正常系][異常系] addのテスト")
    class AddTests {

        @Test
        @DisplayName("[正常系] タスク追加: 正常系")
        void testNormal() {
            LocalDate due = LocalDate.now().plusDays(1);
            Task created = new Task();
            when(mockRepository.create(any(Task.class))).thenReturn(created);

            Task result = controller.add("タイトル", due, false);

            assertEquals(created, result);
        }

    // 異常系: タスク追加時の不正なパラメータ組み合わせを網羅的に検証
    @ParameterizedTest(name = "{index}: title={0}, due={1}, 例外={2}")
    @MethodSource("com.example.task.TaskControllerTest$AddTests#provideInvalidAddParams")
    @DisplayName("[異常系] タスク追加: 異常系パラメータライズドテスト")
    void testAddInvalidParams(String title, LocalDate due, boolean completed, Class<? extends Exception> expectedException) {
        assertThrows(expectedException, () -> controller.add(title, due, completed));
    }

        static Stream<Arguments> provideInvalidAddParams() {
            LocalDate future = LocalDate.now().plusDays(1);
            LocalDate past = LocalDate.now().minusDays(1);
            return Stream.of(
                Arguments.of(null, future, false, IllegalArgumentException.class),
                Arguments.of("", future, false, IllegalArgumentException.class),
                Arguments.of("   ", future, false, IllegalArgumentException.class),
                Arguments.of("タイトル", null, false, IllegalArgumentException.class),
                Arguments.of("タイトル", past, false, IllegalArgumentException.class)
            );
        }
    }

    @Nested
    @DisplayName("[正常系][例外系] deleteのテスト")
    class DeleteTests {
        @Test
        @DisplayName("[正常系] タスク削除: 正常系")
        void testNormal() {
            Task t = new Task();
            when(mockRepository.findById(EXISTING_TASK_ID)).thenReturn(Optional.of(t));
            doNothing().when(mockRepository).delete(EXISTING_TASK_ID);

            Task result = controller.delete(EXISTING_TASK_ID);

            assertEquals(t, result);
        }

        @Test
        @DisplayName("[例外系] タスク削除: 存在しないIDで例外")
        void testNotFound() {
            when(mockRepository.findById(NON_EXISTENT_TASK_ID)).thenReturn(Optional.empty());

            assertThrows(EJBException.class, () -> controller.delete(NON_EXISTENT_TASK_ID));
        }
    }

    @Nested
    @DisplayName("[正常系][異常系][例外系] updateのテスト")
    class UpdateTests {
        @Test
        @DisplayName("[正常系] タスク更新: 正常系")
        void testNormal() {
            LocalDate due = LocalDate.now().plusDays(1);
            Task updated = new Task();
            when(mockRepository.update(EXISTING_TASK_ID, TASK_TITLE, due, true)).thenReturn(updated);

            Task result = controller.update(EXISTING_TASK_ID, TASK_TITLE, due, true);

            assertEquals(updated, result);
        }

        @ParameterizedTest(name = "{index}: title={1}, due={2}, 例外={3}")
        @MethodSource("com.example.task.TaskControllerTest$UpdateTests#provideInvalidUpdateParams")
        @DisplayName("[異常系] タスク更新: 異常系パラメータライズドテスト")
        void testUpdateInvalidParams(int id, String title, LocalDate due, boolean completed, Class<? extends Exception> expectedException) {
            assertThrows(expectedException, () -> controller.update(id, title, due, completed));
        }

        static Stream<Arguments> provideInvalidUpdateParams() {
            LocalDate future = LocalDate.now().plusDays(1);
            LocalDate past = LocalDate.now().minusDays(1);
            return Stream.of(
                Arguments.of(EXISTING_TASK_ID, null, future, false, IllegalArgumentException.class),
                Arguments.of(EXISTING_TASK_ID, "", future, false, IllegalArgumentException.class),
                Arguments.of(EXISTING_TASK_ID, "   ", future, false, IllegalArgumentException.class),
                Arguments.of(EXISTING_TASK_ID, TASK_TITLE, null, false, IllegalArgumentException.class),
                Arguments.of(EXISTING_TASK_ID, TASK_TITLE, past, false, IllegalArgumentException.class)
            );
        }

        @Test
        @DisplayName("[例外系] タスク更新: 存在しないIDで例外")
        void testNotFound() {
            LocalDate due = LocalDate.now().plusDays(1);
            when(mockRepository.update(NON_EXISTENT_TASK_ID, TASK_TITLE, due, false))
                .thenThrow(new EntityNotFoundException("not found"));

            assertThrows(EJBException.class, () -> controller.update(NON_EXISTENT_TASK_ID, TASK_TITLE, due, false));
        }
    }

    @Nested
    @DisplayName("[正常系][異常系][例外系] addSubtaskのテスト")
    class AddSubtaskTests {
        @Test
        @DisplayName("[正常系] サブタスク追加: 正常系")
        void testNormal() {
            LocalDate due = LocalDate.now().plusDays(1);
            Task parent = new Task();
            parent.setId(PARENT_TASK_ID);
            when(mockRepository.findById(PARENT_TASK_ID)).thenReturn(Optional.of(parent));
            Task created = new Task();
            when(mockRepository.create(any(Task.class))).thenReturn(created);

            Task result = controller.addSubtask(PARENT_TASK_ID, SUBTASK_TITLE, due, false);

            assertEquals(created, result);
        }

        @Test
        @DisplayName("[例外系] サブタスク追加: 親タスクが存在しない場合はEJBException")
        void testParentNotFound() {
            LocalDate due = LocalDate.now().plusDays(1);
            when(mockRepository.findById(INVALID_PARENT_ID)).thenReturn(Optional.empty());

            assertThrows(EJBException.class, () -> controller.addSubtask(INVALID_PARENT_ID, SUBTASK_TITLE, due, false));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("[異常系] サブタスク追加: タイトル不正(null/空/空白)で例外")
        void testInvalidTitle(String title) {
            LocalDate due = LocalDate.now().plusDays(1);
            when(mockRepository.findById(EXISTING_TASK_ID)).thenReturn(Optional.of(new Task()));

            assertThrows(IllegalArgumentException.class, () -> controller.addSubtask(EXISTING_TASK_ID, title, due, false));
        }

        @ParameterizedTest(name = "{index}: due={1}, 例外={2}")
        @MethodSource("com.example.task.TaskControllerTest$AddSubtaskTests#provideInvalidDueDateParams")
        @DisplayName("[異常系] サブタスク追加: 期限異常系パラメータライズドテスト")
        void testInvalidDueDate(Integer parentId, LocalDate due, Class<? extends Exception> expectedException) {
            when(mockRepository.findById(parentId)).thenReturn(Optional.of(new Task()));

            assertThrows(expectedException, () -> controller.addSubtask(parentId, "サブタスク", due, false));
        }

        static Stream<Arguments> provideInvalidDueDateParams() {
            LocalDate past = LocalDate.now().minusDays(1);
            return Stream.of(
                Arguments.of(EXISTING_TASK_ID, null, IllegalArgumentException.class),
                Arguments.of(EXISTING_TASK_ID, past, IllegalArgumentException.class)
            );
        }
    }

    @Nested
    @DisplayName("[正常系] findSubtasksのテスト")
    class FindSubtasksTests {
        @Test
        @DisplayName("[正常系] 親IDに紐づく子タスクが正しく返る")
        void testFindSubtasks() {
            Task child1 = new Task();
            Task child2 = new Task();
            List<Task> subtasks = Arrays.asList(child1, child2);
            when(mockRepository.findByParentId(SUBTASK_PARENT_ID)).thenReturn(subtasks);

            List<Task> result = controller.findSubtasks(SUBTASK_PARENT_ID);

            assertEquals(subtasks, result);
        }

        @Test
        @DisplayName("[正常系] 子タスクが存在しない場合は空リスト")
        void testNoSubtasks() {
            when(mockRepository.findByParentId(SUBTASK_PARENT_ID)).thenReturn(Collections.emptyList());

            List<Task> result = controller.findSubtasks(SUBTASK_PARENT_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("[正常系] findRootTasksのテスト")
    class FindRootTasksTests {
        @Test
        @DisplayName("[正常系] ルートタスクが正しく返る")
        void testFindRootTasks() {
            Task root1 = new Task();
            Task root2 = new Task();
            List<Task> roots = Arrays.asList(root1, root2);
            when(mockRepository.findRootTasks()).thenReturn(roots);

            List<Task> result = controller.findRootTasks();

            assertEquals(roots, result);
        }

        @Test
        @DisplayName("[正常系] ルートタスクが存在しない場合は空リスト")
        void testNoRootTasks() {
            when(mockRepository.findRootTasks()).thenReturn(Collections.emptyList());

            List<Task> result = controller.findRootTasks();

            assertTrue(result.isEmpty());
        }
    }
}
