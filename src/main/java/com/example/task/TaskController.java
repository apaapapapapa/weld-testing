package com.example.task;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

@RequestScoped
public class TaskController {

    @Inject
    TaskRepository taskRepository;

    public List<Task> loadAll() {
        return taskRepository.findAll();
    }

    public Task add(String title, java.time.LocalDate dueDate, boolean completed) {
        // 難易度1: タイトルの空チェック
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です。");
        }
        // 難易度2: 期限の過去日付チェック
        if (dueDate == null) {
            throw new IllegalArgumentException("期限(dueDate)は必須です。");
        }
        if (dueDate.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("期限(dueDate)は今日以降の日付を指定してください。");
        }

        final Task newTask = new Task();
        newTask.setTitle(title);
        newTask.setDueDate(dueDate);
        newTask.setCompleted(completed);

        return taskRepository.create(newTask);
    }

    public Task delete(int id) {

        // IDが存在するか確認
        Optional<Task> task = taskRepository.findById(id);
        
        if (!task.isPresent()) {
            throw new EJBException("指定されたIDのタスクが見つかりません: " + id);
        }
        
        taskRepository.delete(id);

        return task.get();
        
    }

    public Task update(int id, String title, java.time.LocalDate dueDate, boolean completed) {
        // 難易度1: タイトルの空チェック
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です。");
        }
        // 難易度2: 期限の過去日付チェック
        if (dueDate == null) {
            throw new IllegalArgumentException("期限(dueDate)は必須です。");
        }
        if (dueDate.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("期限(dueDate)は今日以降の日付を指定してください。");
        }
        try {
            return taskRepository.update(id, title, dueDate, completed);
        } catch (EntityNotFoundException enf) {
            throw new EJBException(enf);
        }
    }
}
