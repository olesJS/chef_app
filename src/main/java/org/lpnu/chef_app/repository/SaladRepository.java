package org.lpnu.chef_app.repository;

import org.lpnu.chef_app.model.Salad;

import java.util.List;

public interface SaladRepository {

    List<Salad> findAll();
    void save(Salad salad);
    void update(Salad salad);
    void delete(Long id);

}
