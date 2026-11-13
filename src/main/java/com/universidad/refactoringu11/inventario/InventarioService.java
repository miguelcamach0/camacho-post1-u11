package com.universidad.refactoringu11.inventario;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.universidad.refactoringu11.reportes.ReporteBajoStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Service
public class InventarioService {

    private final List<Producto> productos = new ArrayList<>();
    private final List<String> movimientos = new ArrayList<>();

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    private ReporteBajoStockService reporteBajoStockService;

    @Autowired(required = false)
    public void setReporteBajoStockService(
            @Lazy ReporteBajoStockService reporteBajoStockService) {
        this.reporteBajoStockService = reporteBajoStockService;
    }

    public List<Producto> obtenerProductos() {
        return List.copyOf(productos);
    }

    // Metodo principal refactorizado
    public String actualizarInventario(Long productoId, int cantidad, String tipoMovimiento) {
        if (!datosValidos(productoId, cantidad, tipoMovimiento))
            return "ERROR_DATOS_INVALIDOS";
        Producto producto = buscarProducto(productoId);
        if (producto == null)
            return "ERROR_PRODUCTO_NO_EXISTE";
        return procesarMovimiento(producto, cantidad, tipoMovimiento);
    }

    // Validar datos
    private boolean datosValidos(Long productoId, int cantidad, String tipoMovimiento) {
        return !(productoId == null || cantidad == 0
                || (!tipoMovimiento.equals("ENTRADA") && !tipoMovimiento.equals("SALIDA")));
    }

    // Buscar producto
    private Producto buscarProducto(Long productoId) {
        for (Producto producto : productos) {
            if (producto.getId().equals(productoId)) {
                return producto;
            }
        }
        return null;
    }

    // Procesar movimiento
    private String procesarMovimiento(Producto producto, int cantidad, String tipoMovimiento) {
        int nuevoStock = calcularNuevoStock(producto, cantidad, tipoMovimiento);

        if (tipoMovimiento.equals("SALIDA") && nuevoStock < 0) {
            return "ERROR_STOCK_INSUFICIENTE";
        }

        producto.setStock(nuevoStock);
        int cantidadReorden = calcularCantidadReorden(producto, nuevoStock);
        registrarMovimiento(producto, cantidad, tipoMovimiento, nuevoStock);

        return generarResultado(producto, nuevoStock, cantidadReorden);
    }

    // Calcular stock
    private int calcularNuevoStock(Producto producto, int cantidad, String tipoMovimiento) {
        if (tipoMovimiento.equals("ENTRADA")) {
            return producto.getStock() + cantidad;
        }
        return producto.getStock() - cantidad;
    }

    // Calcular reorden
    private int calcularCantidadReorden(Producto producto, int nuevoStock) {
        boolean necesitaReorden = nuevoStock < producto.getStockMinimo();
        return necesitaReorden
                ? (producto.getStockMinimo() * 3) - nuevoStock
                : 0;
    }

    // Registrar movimiento
    private void registrarMovimiento(Producto producto, int cantidad,
            String tipoMovimiento, int nuevoStock) {
        String registro = tipoMovimiento + " de " + cantidad + " unidades de "
                + producto.getNombre() + " -> stock final: " + nuevoStock;
        movimientos.add(registro);
        System.out.println(registro);
    }

    private String generarResultado(Producto producto, int nuevoStock, int cantidadReorden) {

        if (nuevoStock < producto.getStockMinimo()) {

            if (reporteBajoStockService != null) {
                System.out.println(reporteBajoStockService.generarReporte());
            }

            System.out.println("ALERTA: " + producto.getNombre()
                    + " bajo stock minimo. Reordenar " + cantidadReorden + " unidades.");

            return "OK_BAJO_STOCK_REORDEN_" + cantidadReorden;
        }

        return "OK_" + nuevoStock;
    }

}
