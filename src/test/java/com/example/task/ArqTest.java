package com.example.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.dataset.DataSet;

import jakarta.inject.Inject;

@ArquillianTest
@DBRider
@Disabled
class ArqTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, TaskRepository.class.getPackage())
                .addAsResource("META-INF/persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeAll
    @DataSet(executeScriptsBefore = "datasets/create_task.sql")
    static void initial(){
    }

    @Inject
    TaskRepository taskRepository;

    @Test
    void addTask() {
        final Task task = new Task();
        task.setTitle("This the test tasks description");
        assertDoesNotThrow(() -> taskRepository.create(task));
    }
    
}