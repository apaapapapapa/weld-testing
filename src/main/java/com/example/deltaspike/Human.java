package com.example.deltaspike;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Human {

    @Id
    private int id;

    @Column
    private String name;
}