package com.example.task;

import com.example.util.MockFacesContext;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.github.database.rider.junit5.api.DBRider;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.deltaspike.SupportDeltaspikeData;
import io.github.cdiunit.deltaspike.SupportDeltaspikeJpa;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TaskBean のテストクラス.
 */
@SupportDeltaspikeJpa
@SupportDeltaspikeData
@ExtendWith({DBUnitExtension.class, CdiJUnit5Extension.class})
@Disabled
class TaskBeanTest {

    @PersistenceContext
    private EntityManagerFactory emf;

    @Produces
    @RequestScoped
    EntityManager createEntityManager() {
      return emf.createEntityManager();
    }

    private TaskController taskController;

    private FacesContext facesContext;

    private TaskBean taskBean;

    @BeforeEach
    void setUp() {

        // Mock FacesContext
        facesContext = MockFacesContext.mock();

        taskBean = new TaskBean(taskController);

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
    @DataSet(value = "datasets/empty-tasks.yml", transactional = true) // テスト前に tasks テーブルを空にする
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
    void testDeleteTask() {

        // Arrange
        String taskId = "1";
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
    void testUpdateTask() {
        // Arrange
        String taskId = "1";
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

    /**
     * @return EntityManager を生成するファクトリ関数.
     */
    static Function<InjectionPoint, Object> getPUFactory() {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test-PU");
        
        return ip -> {
            EntityManager em = emf.createEntityManager();
            
            return em; 
        };
    }
}