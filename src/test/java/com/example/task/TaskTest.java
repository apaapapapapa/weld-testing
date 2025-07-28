package com.example.task;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Stream;
import java.time.LocalDate;

class TaskTest {

    @Test
    void testDefaultValues() {
        Task task = new Task();
        assertEquals(0, task.getId());
        assertNull(task.getTitle());
        assertNull(task.getDueDate());
        assertFalse(task.getCompleted());
    }

    @Nested
    class IsTrulyCompletedTest {

        @ParameterizedTest
        @MethodSource("provideTrulyCompletedCases")
        void testIsTrulyCompleted(
                Boolean completed,
                java.time.LocalDate dueDate,
                String title,
                Boolean parentCompleted,
                Boolean[] childrenCompleted,
                boolean expected
        ) {
            Task task = new Task();
            task.setCompleted(completed);
            task.setDueDate(dueDate);
            task.setTitle(title);

            // 親タスク
            if (parentCompleted != null) {
                Task parent = new Task();
                parent.setCompleted(parentCompleted);
                task.setParent(parent);
            }

            // サブタスク
            if (childrenCompleted != null) {
                task.setChildren(
                    Arrays.stream(childrenCompleted)
                            .map(c -> {
                                Task child = new Task();
                                child.setCompleted(c);
                                return child;
                            })
                            .toList()
                );
            }

            assertEquals(expected, task.isTrulyCompleted());
        }

        static Stream<Arguments> provideTrulyCompletedCases() {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate future = today.plusDays(1);

            return Stream.of(
                // すべての条件を満たす
                Arguments.of(true, today, "Task", true, new Boolean[]{true, true}, true),
                // サブタスクが未完了
                Arguments.of(true, today, "Task", true, new Boolean[]{true, false}, false),
                // 親タスクが未完了
                Arguments.of(true, today, "Task", false, new Boolean[]{true, true}, false),
                // 期日が未来
                Arguments.of(true, future, "Task", true, new Boolean[]{true, true}, false),
                // タイトルがWIPを含む
                Arguments.of(true, today, "WIP Task", true, new Boolean[]{true, true}, false),
                // タイトルが長すぎ
                Arguments.of(true, today, "A".repeat(51), true, new Boolean[]{true, true}, false),
                // タイトルがnull
                Arguments.of(true, today, null, true, new Boolean[]{true, true}, false),
                // completedがfalse
                Arguments.of(false, today, "Task", true, new Boolean[]{true, true}, false),
                // dueDateがnull
                Arguments.of(true, null, "Task", true, new Boolean[]{true, true}, false),
                // サブタスクなし
                Arguments.of(true, today, "Task", true, null, true),
                // 親タスクなし
                Arguments.of(true, today, "Task", null, new Boolean[]{true, true}, true),
                // タイトルが1文字（正常系）
                Arguments.of(true, today, "A", true, new Boolean[]{true, true}, true),
                // タイトルが50文字（正常系）
                Arguments.of(true, today, "A".repeat(50), true, new Boolean[]{true, true}, true),
                // タイトルが空文字
                Arguments.of(true, today, "", true, new Boolean[]{true, true}, false)
            );
        }

        @Test
        void testChildContainsNull() {
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");
            task.setParent(null);

            Task child1 = new Task();
            child1.setCompleted(true);
            // childrenにnull要素を含める
            task.setChildren(Arrays.asList(child1, null));

            assertFalse(task.isTrulyCompleted());
        }

        @Test
        void testParentCompletedIsNull() {
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");

            Task parent = new Task();
            parent.setCompleted(null);
            task.setParent(parent);

            task.setChildren(Arrays.asList());

            assertFalse(task.isTrulyCompleted());
        }

        @Test
        void testChildCompletedIsNull() {
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");
            task.setParent(null);

            Task child1 = new Task();
            child1.setCompleted(null);

            task.setChildren(Arrays.asList(child1));

            assertFalse(task.isTrulyCompleted());
        }
    }

    @Test
    void testSettersAndGetters() {
        Task task = new Task();
        task.setId(10);
        task.setTitle("Test Title");
        LocalDate date = LocalDate.of(2025, 7, 28);
        task.setDueDate(date);
        task.setCompleted(true);

        assertEquals(10, task.getId());
        assertEquals("Test Title", task.getTitle());
        assertEquals(date, task.getDueDate());
        assertTrue(task.getCompleted());
    }
}
