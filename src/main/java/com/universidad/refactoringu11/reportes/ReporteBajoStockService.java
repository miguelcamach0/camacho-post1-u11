package com.universidad.refactoringu11.reportes;

import com.universidad.refactoringu11.inventario.InventarioService;
import com.universidad.refactoringu11.inventario.NotificadorBajoStock;
import com.universidad.refactoringu11.inventario.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteBajoStockService implements NotificadorBajoStock {

    private final InventarioService inventarioService;
    private final List<String> alertas = new ArrayList<>();

    public ReporteBajoStockService(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @Override
    public void notificar(Producto producto, int cantidadReorden) {
        String alerta = "ALERTA: " + producto.getNombre()
                + " bajo stock minimo. Reordenar "
                + cantidadReorden + " unidades.";

        alertas.add(alerta);
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

        if (!alertas.isEmpty()) {
            reporte.append("\n\nALERTAS REGISTRADAS:");

            for (String alerta : alertas) {
                reporte.append("\n- ").append(alerta);
            }
        }

        return reporte.toString();
    }
}


