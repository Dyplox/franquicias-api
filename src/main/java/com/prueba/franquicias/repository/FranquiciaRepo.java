package com.prueba.franquicias.repository;

import com.prueba.franquicias.model.Franquicia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FranquiciaRepo extends JpaRepository<Franquicia, Long> {
}
