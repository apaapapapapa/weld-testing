package com.example.task;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
