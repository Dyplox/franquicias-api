package com.prueba.franquicias.repository;

import com.prueba.franquicias.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepo extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByFranquiciaIdOrderById(Long franquiciaId);
}
