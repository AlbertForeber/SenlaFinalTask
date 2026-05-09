package com.chump.common.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T, ID> {

    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> batchFindAll(int batchSize, int offset);
    T save(T entity);
    void update(T entity);
    void delete(ID id);
    T getReference(ID id);
    void refresh(T entity);
}
