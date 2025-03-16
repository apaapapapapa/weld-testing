package com.example.task;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@NamedQuery(name = Task.FIND_ALL, query = "SELECT t FROM Task t")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    public static final String FIND_ALL = "demo.jsf.Task.ALL";

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

}
