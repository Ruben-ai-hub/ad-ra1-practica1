package org.educa.service;

import generated.Costes;
import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDao;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private final ProductoDao productoDao = new ProductoDao();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> productoEntities = new ArrayList<>();
        List<Producto> productos = productoDao.readFile(fileXml);

        if (productos != null) {
            for (Producto produc : productos) {
                ProductoEntity entity = new ProductoEntity();
                entity.setProducto(produc);

                BigDecimal precio = produc.getPrecio() != null ? produc.getPrecio() : BigDecimal.ZERO;
                BigDecimal descuento = produc.getDescuento() != null ? produc.getDescuento() : BigDecimal.ZERO;
                BigDecimal precioFinal = precio.multiply(descuento).divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
                entity.setPrecioFinal(precioFinal);

                BigDecimal costes = BigDecimal.ZERO;
                if (produc.getCostes() != null) {
                    Costes c = produc.getCostes();
                    if (c.getCostesAlmacenaje() != null) costes = costes.add(c.getCostesAlmacenaje());
                    if (c.getCostesEnvio() != null) costes = costes.add(c.getCostesEnvio());
                }
                entity.setCost(costes);
                entity.setProfit(precioFinal.subtract(costes));
                productoEntities.add(entity);
            }
        }
        return productoEntities;
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
