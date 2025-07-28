package com.example.task;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class TaskTest {

    @Nested
    @DisplayName("Task#isTrulyCompletedのテスト")
    class IsTrulyCompletedTest {

        static record TestCase(
            Boolean completed,
            LocalDate dueDate,
            String title,
            Boolean parentCompleted,
            List<Boolean> childrenCompleted,
            boolean expected,
            String description
        ) {
            @Override
            public String toString() {
                return description;
            }
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("provideNormalCases")
        @DisplayName("正常系: 全ての条件を満たす場合はtrue")
        void testAllConditionsSatisfied_returnsTrue(TestCase tc) {

            Task task = new TaskBuilder()
                .completed(tc.completed)
                .dueDate(tc.dueDate)
                .title(tc.title)
                .parentCompleted(tc.parentCompleted)
                .childrenCompleted(tc.childrenCompleted)
                .build();

            boolean actual = task.isTrulyCompleted();

            assertTrue(actual);
        }

        @ParameterizedTest(name = "{index}: {0}")
        @MethodSource("provideAbnormalCases")
        @DisplayName("異常系: 不正な条件の場合はfalse")
        void testInvalidConditions_returnFalse(TestCase tc) {

            Task task = new TaskBuilder()
                .completed(tc.completed)
                .dueDate(tc.dueDate)
                .title(tc.title)
                .parentCompleted(tc.parentCompleted)
                .childrenCompleted(tc.childrenCompleted)
                .build();
                
            boolean actual = task.isTrulyCompleted();

            assertFalse(actual);
        }

        @ParameterizedTest(name = "{index}: {1}")
        @MethodSource("provideInvalidNullPatterns")
        @DisplayName("例外系: サブタスクや親タスク、completedがnullの場合はfalse")
        void testNullPatterns(Task task, String description) {

            boolean actual = task.isTrulyCompleted();

            assertFalse(actual);
        }

        static Stream<TestCase> provideNormalCases() {
            LocalDate today = LocalDate.now();
            return Stream.of(
                testCase(true, today, "Task", true, allTrue(), true, "全て完了"),
                testCase(true, today, "A", true, allTrue(), true, "タイトル1文字"),
                testCase(true, today, "A".repeat(50), true, allTrue(), true, "タイトル50文字"),
                testCase(true, today, "Task", true, null, true, "サブタスクなし"),
                testCase(true, today, "Task", null, allTrue(), true, "親タスクなし")
            );
        }

        static Stream<TestCase> provideAbnormalCases() {
            LocalDate today = LocalDate.now();
            LocalDate future = today.plusDays(1);
            return Stream.of(
                testCase(true, today, "Task", true, mixedTrueFalse(), false, "サブタスクが未完了"),
                testCase(true, today, "Task", false, allTrue(), false, "親タスクが未完了"),
                testCase(true, future, "Task", true, allTrue(), false, "期日が未来"),
                testCase(true, today, "WIP Task", true, allTrue(), false, "タイトルにWIP含む"),
                testCase(true, today, "A".repeat(51), true, allTrue(), false, "タイトルが長すぎ"),
                testCase(true, today, null, true, allTrue(), false, "タイトルがnull"),
                testCase(false, today, "Task", true, allTrue(), false, "completedがfalse"),
                testCase(true, null, "Task", true, allTrue(), false, "dueDateがnull"),
                testCase(true, today, "", true, allTrue(), false, "タイトルが空文字")
            );
        }

        static Stream<Arguments> provideInvalidNullPatterns() {
            LocalDate today = LocalDate.now();
            // サブタスクにnullが含まれる
            Task child1 = new TaskBuilder()
                .completed(true)
                .build();
            Task taskWithNullChild = new TaskBuilder()
                .completed(true)
                .dueDate(today)
                .title("Task")
                .children(Arrays.asList(child1, null))
                .build();

            // 親タスクのcompletedがnull
            Task parentWithNullCompleted = new TaskBuilder()
                .completed(null)
                .build();
            Task taskWithParentCompletedNull = new TaskBuilder()
                .completed(true)
                .dueDate(today)
                .title("Task")
                .parent(parentWithNullCompleted)
                .children(Arrays.asList())
                .build();

            // サブタスクのcompletedがnull
            Task childWithNullCompleted = new TaskBuilder()
                .completed(null)
                .build();
            Task taskWithChildCompletedNull = new TaskBuilder()
                .completed(true)
                .dueDate(today)
                .title("Task")
                .children(Arrays.asList(childWithNullCompleted))
                .build();

            return Stream.of(
                Arguments.of(taskWithNullChild, "サブタスクにnullが含まれる"),
                Arguments.of(taskWithParentCompletedNull, "親タスクのcompletedがnull"),
                Arguments.of(taskWithChildCompletedNull, "サブタスクのcompletedがnull")
            );  
        }

        static List<Boolean> allTrue() {
            return Arrays.asList(true, true);
        }   

        static List<Boolean> mixedTrueFalse() {
            return Arrays.asList(true, false);
        }

        static TestCase testCase(Boolean completed, LocalDate dueDate, String title,
                                        Boolean parentCompleted, List<Boolean> childrenCompleted,
                                        boolean expected, String description) {
            return new TestCase(completed, dueDate, title, parentCompleted, childrenCompleted, expected, description);
        }

    }

    static class TaskBuilder {
        
        private Boolean completed;
        private LocalDate dueDate;
        private String title;
        private Task parent;
        private List<Task> children;
        private Boolean parentCompleted;
        private List<Boolean> childrenCompleted;

        public TaskBuilder completed(Boolean completed) {
            this.completed = completed;
            return this;
        }

        public TaskBuilder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskBuilder parent(Task parent) {
            this.parent = parent;
            return this;
        }

        public TaskBuilder parentCompleted(Boolean parentCompleted) {
            this.parentCompleted = parentCompleted;
            return this;
        }

        public TaskBuilder children(List<Task> children) {
            this.children = children;
            return this;
        }

        public TaskBuilder childrenCompleted(List<Boolean> childrenCompleted) {
            this.childrenCompleted = childrenCompleted;
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.setCompleted(completed);
            task.setDueDate(dueDate);
            task.setTitle(title);

            if (parent != null) {
                task.setParent(parent);
            } else if (parentCompleted != null) {
                Task p = new Task();
                p.setCompleted(parentCompleted);
                task.setParent(p);
            }

            if (children != null) {
                task.setChildren(children);
            } else if (childrenCompleted != null) {
                task.setChildren(
                    childrenCompleted.stream()
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

}
