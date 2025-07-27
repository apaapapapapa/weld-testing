package com.example.task;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class TaskBean implements Serializable {

    private List<Task> allTasks;

    @Inject
    private TaskController controller;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private java.time.LocalDate dueDate;

    @Getter
    @Setter
    private boolean completed;

    @PostConstruct
    public void postConstruct() {
        refresh();
    }

    public TaskBean() {
    }

    public void delete() {
        runWithMessage(() -> {
            controller.delete(id);
            refresh();
            addMessage("Task " + id + " deleted");
        }, "Error deleting the Task by Id.");
    }

    public void add() {
        runWithMessage(() -> {
            controller.add(title, dueDate, completed);
            refresh();
            addMessage("Task with title " + title + " created");
        }, "Error adding a new Task.");
    }

    public void update() {
        runWithMessage(() -> {
            controller.update(id, title, dueDate, completed);
            refresh();
            addMessage("Task " + id + " updated");
        }, "Error updating the Task by Id.");
    }

    private void runWithMessage(Runnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            addMessage(errorMessage + " " + e.getLocalizedMessage());
        }
    }

    public void refresh() {
        this.allTasks = controller.loadAll();
    }

    public List<Task> getAllTasks() {
        return allTasks;
    }

    /**
     * 全タスクの進捗率（パーセンテージ, 0.0～100.0）を返す
     */
    public double getProgressRate() {
        return controller.calculateProgressRate();
    }

    private void addMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
    }
}
