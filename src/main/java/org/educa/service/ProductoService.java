
package org.educa.service;

import generated.Costes;
import generated.Producto;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ExcelDao;
import org.educa.dao.ProductoDao;
import org.educa.dao.SummaryDao;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    private final ProductoDao productoDao = new ProductoDao();
    private final SummaryDao  summaryDao = new SummaryDao();
    private final ExcelDao excelDao = new ExcelDao();

    public List<ProductoEntity> readFile(String fileXml)
            throws JAXBException {

        List<ProductoEntity> productoEntities = new ArrayList<>();
        List<Producto> productos = productoDao.readFile(fileXml);

        if (productos != null) {
            for (Producto produc : productos) {

                ProductoEntity entity = new ProductoEntity();
                entity.setProducto(produc);

                BigDecimal precio = produc.getPrecio() != null
                        ? produc.getPrecio() : BigDecimal.ZERO;

                BigDecimal descuento = produc.getDescuento() != null
                        ? produc.getDescuento() : BigDecimal.ZERO;

                // Precio después de aplicar el descuento.
                BigDecimal precioFinal = precio
                        .multiply(new BigDecimal("100").subtract(descuento))
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                entity.setPrecioFinal(precioFinal);

                // Costes de envío y almacenaje.
                BigDecimal costes = BigDecimal.ZERO;

                if (produc.getCostes() != null) {
                    Costes c = produc.getCostes();

                    if (c.getCostesAlmacenaje() != null) {
                        costes = costes.add(c.getCostesAlmacenaje());
                    }

                    if (c.getCostesEnvio() != null) {
                        costes = costes.add(c.getCostesEnvio());
                    }
                }

                entity.setCost(costes);
                entity.setProfit(precioFinal.subtract(costes));
                productoEntities.add(entity);
            }
        }

        return productoEntities;
    }

    public void exportSummary(String path, String fileXml)
            throws JAXBException, IOException {

        File xml = new File(fileXml);

        if (!xml.isFile()) {
            throw new IOException(
                    "No se encuentra el fichero XML: "
                            + xml.getAbsolutePath()
            );
        }

        List<ProductoEntity> productos = readFile(fileXml);
        BigDecimal beneficioTotal = BigDecimal.ZERO;

        for (ProductoEntity producto : productos) {
            beneficioTotal = beneficioTotal.add(producto.getProfit());
        }

        beneficioTotal = beneficioTotal.setScale(2, RoundingMode.HALF_UP);

        String nombreFichero = xml.getName();
        int punto = nombreFichero.lastIndexOf('.');

        String nombreSinExtension = punto >= 0
                ? nombreFichero.substring(0, punto)
                : nombreFichero;

        int separador = nombreSinExtension.lastIndexOf('_');

        String fecha = separador >= 0
                ? nombreSinExtension.substring(separador + 1)
                : nombreSinExtension;

        SummaryEntity summary = new SummaryEntity(
                fecha,
                productos.size(),
                beneficioTotal,
                xml.getAbsolutePath(),
                nombreSinExtension,
                Files.size(xml.toPath())
        );

        SummaryDao summaryDao = new SummaryDao();
        summaryDao.exportarFichero(summary, path);
    }

    public void exportExcel(String path, String fileXml)
            throws JAXBException, IOException, ParseException {
        File xml = new File(fileXml);
        if (!xml.isFile()) {
            throw new IOException("No se encuentra el fichero XML: " + xml.getAbsolutePath());
        }
        List<ProductoEntity> productos = readFile(fileXml);
        String nombreFichero = xml.getName();
        int punto = nombreFichero.lastIndexOf('.');
        String nombreSinExtension = punto >= 0 ? nombreFichero.substring(0, punto)
                : nombreFichero;

        int separador = nombreSinExtension.lastIndexOf('_');

        String fecha = separador >= 0
                ? nombreSinExtension.substring(separador + 1)
                : nombreSinExtension;

        excelDao.exportarExcel(productos, fecha, path);
    }
}
