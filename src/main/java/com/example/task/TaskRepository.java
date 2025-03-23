package com.example.task;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@Stateless
public class TaskRepository {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Inject
    private EntityManager em;

    public Task create(Task task) {
        logger.info("Creating task" + task.getTitle());
        
        em.persist(task);
        em.flush();
        em.refresh(task);

        return task;
    }

    public List<Task> findAll() {
        logger.info("Getting all task");
        return em.createQuery("SELECT c FROM task c", Task.class).getResultList();
    }

    public Optional<Task> findById(Long id) {
        logger.info("Getting task by id " + id);
        return Optional.ofNullable(em.find(Task.class, id));
    }

    public void delete(Long id) {
        logger.info("Deleting task by id " + id);
        var task = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        em.remove(task);
    }

    public Task update(Long id, String title) {
        logger.info("Updating task " + title);
        Task ref = em.getReference(Task.class, id);
        ref.setTitle(title);
        return em.merge(ref);
    }

    
}