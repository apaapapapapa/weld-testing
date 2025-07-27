package com.example.task;

import com.example.util.MockFacesContext;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TaskBean のテストクラス.
 */
@ExtendWith({CdiJUnit5Extension.class})
@AdditionalClasses({TaskController.class, TaskRepository.class})
class TaskBeanTest {

    @Produces
    @RequestScoped
    EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("TaskPersistenceUnit").createEntityManager();
    }

    // --- 追加: getter系の個別テスト ---

    @Test
    void testGetProgressRate() {
        // Arrange
        TaskController mockController = mock(TaskController.class);
        when(mockController.calculateProgressRate()).thenReturn(55.5);

        TaskBean bean = new TaskBean();
        try {
            java.lang.reflect.Field controllerField = TaskBean.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(bean, mockController);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertEquals(55.5, bean.getProgressRate());
    }

    @Test
    void testGetDelayRiskRate() {
        // Arrange
        TaskController mockController = mock(TaskController.class);
        when(mockController.calculateDelayRiskRate()).thenReturn(23.4);

        TaskBean bean = new TaskBean();
        try {
            java.lang.reflect.Field controllerField = TaskBean.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(bean, mockController);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertEquals(23.4, bean.getDelayRiskRate());
    }

    @Test
    void testGetHighRiskTasks() {
        // Arrange
        TaskController mockController = mock(TaskController.class);
        List<Task> highRiskTasks = List.of(new Task());
        when(mockController.findHighRiskTasks()).thenReturn(highRiskTasks);

        TaskBean bean = new TaskBean();
        try {
            java.lang.reflect.Field controllerField = TaskBean.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(bean, mockController);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Act & Assert
        assertEquals(highRiskTasks, bean.getHighRiskTasks());
    }

    @Inject
    TaskController taskController;

    @Inject
    TaskRepository taskRepository;

    private FacesContext facesContext;

    private TaskBean taskBean;

    @BeforeEach
    void setUp() {

        // Mock FacesContext
        facesContext = MockFacesContext.mock();

        taskBean = new TaskBean();
        // フィールドインジェクションの代用
        java.lang.reflect.Field controllerField = null;
        try {
            controllerField = TaskBean.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(taskBean, taskController);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @AfterEach
    void tearDown() {

        facesContext.release();
    }

    /**
     * TaskBean の add() メソッドのテスト.
     */
    @InRequestScope
    @Transactional
    @DBRider
    //@DataSet(value = "datasets/empty-tasks.yml", transactional = true) // テスト前に tasks テーブルを空にする
    @Disabled
    void testAddTask() {

        // Arrange
        String taskTitle = "新しいタスク";
        taskBean.setTitle(taskTitle);

        // Act
        taskBean.add();

        // Assert
        List<Task> allTasks = taskBean.getAllTasks();
        assertNotNull(allTasks);
        assertEquals(1, allTasks.size());
        assertEquals(taskTitle, allTasks.get(0).getTitle());

        verify(facesContext).addMessage(eq(null), argThat(message ->
                message.getSummary().equals("Task with title " + taskTitle + " created")));
    }

    /**
     * TaskBean の delete() メソッドのテスト. 
     */
    @InRequestScope
    @Transactional
    @DBRider
    @DataSet(value = "datasets/existing-tasks.yml", transactional = true) // テスト前に既存のタスクをロード
    @Disabled
    void testDeleteTask() {

        // Arrange
        int taskId = 1;
        taskBean.setId(taskId);

        // Act
        taskBean.delete();

        // Assert
        List<Task> allTasks = taskBean.getAllTasks();
        
        assertNotNull(allTasks);
        assertEquals(1, allTasks.size());
        assertEquals("初期タスク2", allTasks.get(0).getTitle());

        verify(facesContext).addMessage(eq(null), argThat(message ->
                message.getSummary().equals("Task " + taskId + " deleted")));
    }

    /**
     * TaskBean の update() メソッドのテスト.
     */
    @InRequestScope
    @Transactional
    @DBRider
    @DataSet(value = "datasets/existing-tasks.yml", transactional = true)
    @Disabled
    void testUpdateTask() {
        // Arrange
        int taskId = 1;
        String updatedTitle = "更新されたタスクタイトル";
        taskBean.setId(taskId);
        taskBean.setTitle(updatedTitle);

        // Act
        taskBean.update();

        // Assert
        List<Task> allTasks = taskBean.getAllTasks();
        assertNotNull(allTasks);
        assertEquals(2, allTasks.size());
        assertEquals(updatedTitle, allTasks.get(0).getTitle());

        verify(facesContext).addMessage(eq(null), argThat(message ->
                message.getSummary().equals("Task " + taskId + " updated")));
    }

}
