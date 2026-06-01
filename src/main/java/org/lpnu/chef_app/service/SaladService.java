package org.lpnu.chef_app.service;

import org.lpnu.chef_app.model.Salad;
import java.util.List;

public interface SaladService {

    List<Salad> getAllSalads();
    void saveSalad(Salad salad);
    void deleteSalad(Long id);

}