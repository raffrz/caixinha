package com.farias.caixinha.common;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Setter;

@Setter
@Transactional
public abstract class CrudService<E extends BaseEntity> {

    @Autowired
    protected JpaRepository<E, UUID> repository;

    public E create(E entity) {
        return repository.save(entity);
    }

    public List<E> listAll() {
        return repository.findAll();
    }

    public Optional<E> findById(UUID id) {
        return repository.findById(id);
    }

    public E update(E entity) {
        UUID id = entity.getId();
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(String.format("Entity with id %s not found", id));
        }
        return repository.save(entity);
    }

    public void deleteById(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(String.format("Entity with id %s not found", id));
        }
        repository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
