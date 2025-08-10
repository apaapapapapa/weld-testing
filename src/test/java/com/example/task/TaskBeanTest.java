package com.example.task;

import com.example.util.MockFacesContext;

import io.github.cdiunit.InRequestScope;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TasktaskBean のテストクラス.
 */
@ExtendWith(MockitoExtension.class)
class TasktaskBeanTest {

    @Mock
    TaskController controller;

    private FacesContext facesContext;

    private TaskBean taskBean;

    @BeforeEach
    @InRequestScope
    void setUp() {

        taskBean = new TaskBean(controller);

        facesContext = MockFacesContext.mock();
    }

    @AfterEach
    void tearDown() {
        facesContext.release();
    }

    @Test
    void postConstruct_callsRefresh_andLoadsAll() {
        var tasks = List.of(new Task(), new Task());
        when(controller.loadAll()).thenReturn(tasks);

        taskBean.postConstruct();

        assertEquals(tasks, taskBean.getAllTasks());
        verify(controller, times(1)).loadAll();
    }

    @Test
    void add_callsControllerAdd_refreshes_andShowsMessage() {
        taskBean.setTitle("T-1");
        taskBean.setDueDate(LocalDate.of(2025, 8, 1));
        taskBean.setCompleted(true);

        when(controller.loadAll()).thenReturn(List.of());

        taskBean.add();

        verify(controller).add(eq("T-1"), eq(LocalDate.of(2025, 8, 1)), eq(true));
        verify(controller).loadAll();

        var msgCaptor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), msgCaptor.capture());
        assertTrue(msgCaptor.getValue().getSummary().contains("Task with title T-1 created"));
    }

    @Test
    void add_whenControllerThrows_showsErrorMessage() {
        taskBean.setTitle("Oops");
        doThrow(new RuntimeException("boom")).when(controller).add(any(), any(), anyBoolean());

        taskBean.add();

        var msgCaptor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), msgCaptor.capture());
        assertTrue(msgCaptor.getValue().getSummary().startsWith("Error adding a new Task."));
        // 失敗時は refresh(loadAll) が呼ばれないこと（runWithMessage内のactionが例外で中断）
        verify(controller, never()).loadAll();
    }

    @Test
    void update_callsControllerUpdate_refreshes_andShowsMessage() {
        taskBean.setId(10);
        taskBean.setTitle("Renamed");
        taskBean.setDueDate(LocalDate.of(2025, 8, 2));
        taskBean.setCompleted(false);
        when(controller.loadAll()).thenReturn(List.of());

        taskBean.update();

        verify(controller).update(10, "Renamed", LocalDate.of(2025, 8, 2), false);
        verify(controller).loadAll();

        var cap = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), cap.capture());
        assertEquals("Task 10 updated", cap.getValue().getSummary());
    }

    @Test
    void delete_callsControllerDelete_refreshes_andShowsMessage() {
        taskBean.setId(5);
        when(controller.loadAll()).thenReturn(List.of());

        taskBean.delete();

        verify(controller).delete(5);
        verify(controller).loadAll();

        var cap = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), cap.capture());
        assertEquals("Task 5 deleted", cap.getValue().getSummary());
    }

    @Test
    void deleteById_callsControllerDelete_refreshes_andShowsMessage() {
        when(controller.loadAll()).thenReturn(List.of());

        taskBean.deleteById(7);

        verify(controller).delete(7);
        verify(controller).loadAll();

        var cap = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), cap.capture());
        assertEquals("Task 7 deleted", cap.getValue().getSummary());
    }

    @Test
    void addSubtask_callsControllerAddSubtask_clearsForm_andShowsMessage() {
        taskBean.setParentId(100);
        taskBean.setSubtaskTitle("Sub A");
        taskBean.setSubtaskDueDate(LocalDate.of(2025, 9, 1));
        taskBean.setSubtaskCompleted(true);
        when(controller.loadAll()).thenReturn(List.of());

        taskBean.addSubtask();

        verify(controller).addSubtask(100, "Sub A", LocalDate.of(2025, 9, 1), true);
        verify(controller).loadAll();

        // メッセージ
        var cap = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(eq(null), cap.capture());
        assertTrue(cap.getValue().getSummary().contains("Subtask with title Sub A created (parentId=100)"));

        // フォーム値クリア
        assertNull(taskBean.getSubtaskTitle());
        assertNull(taskBean.getSubtaskDueDate());
        assertFalse(taskBean.isSubtaskCompleted());
        assertNull(taskBean.getParentId());
    }

    @Test
    void proxyMethods_delegateToController() {
        // root tasks
        var roots = List.of(new Task());
        when(controller.findRootTasks()).thenReturn(roots);
        assertEquals(roots, taskBean.getRootTasks());
        assertEquals(roots, taskBean.getRootTasksProperty());
        verify(controller, times(2)).findRootTasks();

        // subtasks by id
        var subs = List.of(new Task(), new Task());
        when(controller.findSubtasks(42)).thenReturn(subs);
        assertEquals(subs, taskBean.getSubtasks(42));
        verify(controller).findSubtasks(42);

        // subtasks by Task
        Task parent = new Task();
        parent.setId(42);
        assertEquals(subs, taskBean.getSubtasks(parent));
        verify(controller, times(2)).findSubtasks(42);

        // null parent -> null
        assertNull(taskBean.getSubtasks((Task) null));

        // progress / risk / high risk list
        when(controller.calculateProgressRate()).thenReturn(77.5);
        when(controller.calculateDelayRiskRate()).thenReturn(12.5);
        when(controller.findHighRiskTasks()).thenReturn(List.of(new Task()));

        assertEquals(77.5, taskBean.getProgressRate(), 0.0001);
        assertEquals(12.5, taskBean.getDelayRiskRate(), 0.0001);
        assertEquals(1, taskBean.getHighRiskTasks().size());
    }

    @Test
    void refresh_updatesAllTasksFromController() {
        var tasks = List.of(new Task());
        when(controller.loadAll()).thenReturn(tasks);

        taskBean.refresh();

        assertEquals(tasks, taskBean.getAllTasks());
        verify(controller).loadAll();
    }

}
