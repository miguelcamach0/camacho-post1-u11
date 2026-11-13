package com.universidad.refactoringu11.reportes;

import com.universidad.refactoringu11.inventario.InventarioService;
import com.universidad.refactoringu11.inventario.Producto;
import org.springframework.stereotype.Service;

@Service
public class ReporteBajoStockService {

    private final InventarioService inventarioService;

    public ReporteBajoStockService(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    public String generarReporte() {
        StringBuilder reporte = new StringBuilder("REPORTE DE BAJO STOCK");
        boolean hayProductos = false;

        for (Producto producto : inventarioService.obtenerProductos()) {
            if (producto.getStock() < producto.getStockMinimo()) {
                reporte.append("\n- ")
                        .append(producto.getNombre())
                        .append(" | Stock: ")
                        .append(producto.getStock())
                        .append(" | Minimo: ")
                        .append(producto.getStockMinimo());

                hayProductos = true;
            }
        }

        if (!hayProductos) {
            reporte.append("\nSin productos bajo stock minimo.");
        }

        return reporte.toString();
    }
}

