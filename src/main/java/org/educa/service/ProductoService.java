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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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



            File xml = new File(fileXml);

            if (!xml.isFile()) {
                throw new IOException(
                        "No se encuentra el fichero XML: " + xml.getAbsolutePath()
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

            String rutaAbsoluta = xml.getAbsolutePath();
            long tamano = Files.size(xml.toPath());

            Path carpeta = Paths.get(path);
            Files.createDirectories(carpeta);


            Path salida = carpeta.resolve("result_" + fecha + ".txt");

            String salto = System.lineSeparator();

            String contenido =
                    "Fecha: " + fecha + salto +
                            "NumeroDeProductos: " + productos.size() + salto +
                            "BeneficioTotal: " + beneficioTotal + salto +
                            "Ruta del fichero: " + rutaAbsoluta + salto +
                            "Nombre del fichero: " + nombreSinExtension + salto +
                            "Tamaño del fichero: " + tamano + " bytes";

            Files.writeString(salida, contenido, StandardCharsets.UTF_8);
        }


    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }

