package com.prueba.franquicias.controller;

import com.prueba.franquicias.model.Franquicia;
import com.prueba.franquicias.model.Producto;
import com.prueba.franquicias.model.ProductoMayorStock;
import com.prueba.franquicias.model.Sucursal;
import com.prueba.franquicias.repository.FranquiciaRepo;
import com.prueba.franquicias.repository.ProductoRepo;
import com.prueba.franquicias.repository.SucursalRepo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class FranquiciaController {

    private final FranquiciaRepo franquicias;
    private final SucursalRepo sucursales;
    private final ProductoRepo productos;

    public FranquiciaController(FranquiciaRepo franquicias, SucursalRepo sucursales, ProductoRepo productos) {
        this.franquicias = franquicias;
        this.sucursales = sucursales;
        this.productos = productos;
    }

    @PostMapping("/franquicias")
    @ResponseStatus(HttpStatus.CREATED)
    public Franquicia crearFranquicia(@RequestBody Franquicia franquicia) {
        validarNombre(franquicia.getNombre());
        franquicia.setId(null);
        return franquicias.save(franquicia);
    }

    @GetMapping("/franquicias")
    public List<Franquicia> listarFranquicias() {
        return franquicias.findAll();
    }

    @PutMapping("/franquicias/{id}/nombre")
    public Franquicia cambiarNombreFranquicia(@PathVariable Long id, @RequestParam String nombre) {
        validarNombre(nombre);
        Franquicia franquicia = buscarFranquicia(id);
        franquicia.setNombre(nombre);
        return franquicias.save(franquicia);
    }

    @GetMapping("/franquicias/{id}/mayor-stock")
    public List<ProductoMayorStock> mayorStockPorSucursal(@PathVariable Long id) {
        buscarFranquicia(id);

        List<ProductoMayorStock> resultado = new ArrayList<>();
        for (Sucursal sucursal : sucursales.findByFranquiciaIdOrderById(id)) {
            productos.findFirstBySucursalIdOrderByStockDesc(sucursal.getId())
                    .ifPresent(producto -> resultado.add(new ProductoMayorStock(
                            sucursal.getNombre(), producto.getNombre(), producto.getStock())));
        }
        return resultado;
    }

    @PostMapping("/franquicias/{id}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Sucursal crearSucursal(@PathVariable Long id, @RequestBody Sucursal sucursal) {
        validarNombre(sucursal.getNombre());
        buscarFranquicia(id);
        sucursal.setId(null);
        sucursal.setFranquiciaId(id);
        return sucursales.save(sucursal);
    }

    @GetMapping("/franquicias/{id}/sucursales")
    public List<Sucursal> listarSucursales(@PathVariable Long id) {
        buscarFranquicia(id);
        return sucursales.findByFranquiciaIdOrderById(id);
    }

    @PutMapping("/sucursales/{id}/nombre")
    public Sucursal cambiarNombreSucursal(@PathVariable Long id, @RequestParam String nombre) {
        validarNombre(nombre);
        Sucursal sucursal = buscarSucursal(id);
        sucursal.setNombre(nombre);
        return sucursales.save(sucursal);
    }

    @PostMapping("/sucursales/{id}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(@PathVariable Long id, @RequestBody Producto producto) {
        validarNombre(producto.getNombre());
        validarStock(producto.getStock());
        buscarSucursal(id);
        producto.setId(null);
        producto.setSucursalId(id);
        return productos.save(producto);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        productos.delete(buscarProducto(id));
    }

    @PutMapping("/productos/{id}/stock")
    public Producto cambiarStock(@PathVariable Long id, @RequestParam int stock) {
        validarStock(stock);
        Producto producto = buscarProducto(id);
        producto.setStock(stock);
        return productos.save(producto);
    }

    @PutMapping("/productos/{id}/nombre")
    public Producto cambiarNombreProducto(@PathVariable Long id, @RequestParam String nombre) {
        validarNombre(nombre);
        Producto producto = buscarProducto(id);
        producto.setNombre(nombre);
        return productos.save(producto);
    }

    private Franquicia buscarFranquicia(Long id) {
        return franquicias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La franquicia no existe"));
    }

    private Sucursal buscarSucursal(Long id) {
        return sucursales.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La sucursal no existe"));
    }

    private Producto buscarProducto(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
    }

    private void validarStock(int stock) {
        if (stock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");
        }
    }
}
