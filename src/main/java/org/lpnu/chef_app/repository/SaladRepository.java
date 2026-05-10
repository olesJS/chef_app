package org.lpnu.chef_app.repository;

import org.lpnu.chef_app.model.Salad;

import java.util.List;
import java.util.Optional;

public interface SaladRepository {
    List<Salad> findAll();
    Optional<Salad> findById(Long id);
    void save(Salad salad);
    void update(Salad salad, Long id);
    void delete(Long id);
}
