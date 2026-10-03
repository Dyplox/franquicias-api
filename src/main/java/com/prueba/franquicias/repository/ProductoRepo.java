package com.prueba.franquicias.repository;

import com.prueba.franquicias.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepo extends JpaRepository<Producto, Long> {

    Optional<Producto> findFirstBySucursalIdOrderByStockDesc(Long sucursalId);
}
