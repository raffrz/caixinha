package com.farias.caixinha.common;

import java.util.List;
import java.util.UUID;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter(onMethod_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class CrudController<E extends BaseEntity, T> {

    private static final String MESSAGE_ENTITY_NOT_FOUND = "Entity not found";
    CrudService<E> crudService;
    ModelMapper modelMapper;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<T> create(@RequestBody T entity) {
        var mappedEntity = modelMapper.map(entity, getEntityClass());
        var persistedEntity = crudService.create(mappedEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(persistedEntity, getDTOClass()));
    }

    @GetMapping
    public List<T> listAll() {
        return crudService.listAll().stream()
                .map(e -> modelMapper.map(e, getDTOClass())).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable("id") UUID id) {
        return crudService.findById(id)
                .map(e -> modelMapper.map(e, getDTOClass()))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND));
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<T> update(@PathVariable("id") UUID id, @RequestBody T entity) {
        E existingEntity = crudService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND));

        E updatedEntity = modelMapper.map(entity, getEntityClass());
        updatedEntity.setId(existingEntity.getId());

        E persistedEntity = crudService.update(updatedEntity);

        return ResponseEntity.ok(modelMapper.map(persistedEntity, getDTOClass()));
    }

    @PatchMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<T> partialUpdate(@PathVariable("id") UUID id, @RequestBody T entity) {
        E existingEntity = crudService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND));

        ModelMapper customModelMapper = new ModelMapper();
        customModelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

        customModelMapper.map(entity, existingEntity);
        existingEntity.setId(id);

        E persistedEntity = crudService.update(existingEntity);

        return ResponseEntity.ok(modelMapper.map(persistedEntity, getDTOClass()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") UUID id) {
        if (!crudService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND);
        }
        crudService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    public abstract Class<E> getEntityClass();

    public abstract Class<T> getDTOClass();
}
