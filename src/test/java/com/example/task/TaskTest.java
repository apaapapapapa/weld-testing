package com.example.task;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.stream.Stream;

class TaskTest {

    @Nested
    @DisplayName("Task#isTrulyCompletedのテスト")
    class IsTrulyCompletedTest {

        @ParameterizedTest(name = "{index}: {6}")
        @MethodSource("provideNormalCases")
        @DisplayName("正常系: 全ての条件を満たす場合はtrue")
        void testIsTrulyCompletedNormal(
                Boolean completed,
                java.time.LocalDate dueDate,
                String title,
                Boolean parentCompleted,
                Boolean[] childrenCompleted,
                boolean expected,
                String description
        ) {
            assertIsTrulyCompleted(completed, dueDate, title, parentCompleted, childrenCompleted, expected, description);
        }

        @ParameterizedTest(name = "{index}: {6}")
        @MethodSource("provideAbnormalCases")
        @DisplayName("異常系: 不正な条件の場合はfalse")
        void testIsTrulyCompletedAbnormal(
                Boolean completed,
                java.time.LocalDate dueDate,
                String title,
                Boolean parentCompleted,
                Boolean[] childrenCompleted,
                boolean expected,
                String description
        ) {
            assertIsTrulyCompleted(completed, dueDate, title, parentCompleted, childrenCompleted, expected, description);
        }

        // 共通のアサートロジックをヘルパーメソッドに抽出
        private void assertIsTrulyCompleted(
                Boolean completed,
                java.time.LocalDate dueDate,
                String title,
                Boolean parentCompleted,
                Boolean[] childrenCompleted,
                boolean expected,
                String description
        ) {
            Task task = createTask(completed, dueDate, title, parentCompleted, childrenCompleted);
            assertEquals(expected, task.isTrulyCompleted(), description);
        }

        @Test
        @DisplayName("例外系: サブタスクにnullが含まれる場合はfalse")
        void testChildContainsNull() {
            // Arrange
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");
            task.setParent(null);

            Task child1 = new Task();
            child1.setCompleted(true);
            // childrenにnull要素を含める
            task.setChildren(Arrays.asList(child1, null));

            // Act & Assert
            assertFalse(task.isTrulyCompleted());
        }

        @Test
        @DisplayName("例外系: 親タスクのcompletedがnullの場合はfalse")
        void testParentCompletedIsNull() {
            // Arrange
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");

            Task parent = new Task();
            parent.setCompleted(null);
            task.setParent(parent);

            task.setChildren(Arrays.asList());

            // Act & Assert
            assertFalse(task.isTrulyCompleted());
        }

        @Test
        @DisplayName("例外系: サブタスクのcompletedがnullの場合はfalse")
        void testChildCompletedIsNull() {
            // Arrange
            Task task = new Task();
            task.setCompleted(true);
            task.setDueDate(java.time.LocalDate.now());
            task.setTitle("Task");
            task.setParent(null);

            Task child1 = new Task();
            child1.setCompleted(null);

            task.setChildren(Arrays.asList(child1));

            // Act & Assert
            assertFalse(task.isTrulyCompleted());
        }

        static Stream<Arguments> provideNormalCases() {
            java.time.LocalDate today = java.time.LocalDate.now();
            return Stream.of(
                Arguments.of(true, today, "Task", true, new Boolean[]{true, true}, true, "全て完了"),
                Arguments.of(true, today, "A", true, new Boolean[]{true, true}, true, "タイトル1文字"),
                Arguments.of(true, today, "A".repeat(50), true, new Boolean[]{true, true}, true, "タイトル50文字"),
                Arguments.of(true, today, "Task", true, null, true, "サブタスクなし"),
                Arguments.of(true, today, "Task", null, new Boolean[]{true, true}, true, "親タスクなし")
            );
        }

        static Stream<Arguments> provideAbnormalCases() {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate future = today.plusDays(1);
            return Stream.of(
                Arguments.of(true, today, "Task", true, new Boolean[]{true, false}, false, "サブタスクが未完了"),
                Arguments.of(true, today, "Task", false, new Boolean[]{true, true}, false, "親タスクが未完了"),
                Arguments.of(true, future, "Task", true, new Boolean[]{true, true}, false, "期日が未来"),
                Arguments.of(true, today, "WIP Task", true, new Boolean[]{true, true}, false, "タイトルにWIP含む"),
                Arguments.of(true, today, "A".repeat(51), true, new Boolean[]{true, true}, false, "タイトルが長すぎ"),
                Arguments.of(true, today, null, true, new Boolean[]{true, true}, false, "タイトルがnull"),
                Arguments.of(false, today, "Task", true, new Boolean[]{true, true}, false, "completedがfalse"),
                Arguments.of(true, null, "Task", true, new Boolean[]{true, true}, false, "dueDateがnull"),
                Arguments.of(true, today, "", true, new Boolean[]{true, true}, false, "タイトルが空文字")
            );
        }
    }

    // テスト用Task生成ユーティリティ
    private Task createTask(Boolean completed, java.time.LocalDate dueDate, String title, Boolean parentCompleted, Boolean[] childrenCompleted) {
        Task task = new Task();
        task.setCompleted(completed);
        task.setDueDate(dueDate);
        task.setTitle(title);

        if (parentCompleted != null) {
            Task parent = new Task();
            parent.setCompleted(parentCompleted);
            task.setParent(parent);
        }

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
        return task;
    }

}
