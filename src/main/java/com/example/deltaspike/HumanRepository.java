package com.example.deltaspike;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface HumanRepository extends EntityRepository<Human, Integer> {
    List<Human> findByName(String name);

}