package org.educa.dao;

import generated.Costes;
import generated.Producto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExcelDao {

    private static final String[] COLUMNAS = {
            "Codigo",
            "Número de Serie",
            "Precio",
            "Descuento",
            "Precio Final",
            "Costes Envío",
            "Costes Almacenaje",
            "Beneficio"
    };

    public void exportarExcel(
            List<ProductoEntity> productos,
            String fecha,
            String path
    ) throws IOException {

        Path carpeta = Paths.get(path);
        Files.createDirectories(carpeta);

        Path archivoSalida =
                carpeta.resolve("export_" + fecha + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet hoja = workbook.createSheet("Productos");

            CellStyle estiloCabecera = workbook.createCellStyle();

            Font fuenteCabecera = workbook.createFont();
            fuenteCabecera.setBold(true);

            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setAlignment(HorizontalAlignment.CENTER);
            estiloCabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloCabecera.setWrapText(true);
            estiloCabecera.setFillForegroundColor(
                    IndexedColors.LIGHT_GREEN.getIndex()
            );
            estiloCabecera.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            ponerBordes(estiloCabecera);

            CellStyle estiloPar =
                    crearEstiloDatos(workbook, IndexedColors.WHITE);

            CellStyle estiloImpar =
                    crearEstiloDatos(workbook, IndexedColors.LIGHT_GREEN);

            DataFormat formato = workbook.createDataFormat();


            CellStyle monedaPar =
                    crearEstiloDatos(workbook, IndexedColors.WHITE);
            monedaPar.setDataFormat(
                    formato.getFormat("#,##0.00 \"€\"")
            );

            CellStyle monedaImpar =
                    crearEstiloDatos(workbook, IndexedColors.LIGHT_GREEN);
            monedaImpar.setDataFormat(
                    formato.getFormat("#,##0.00 \"€\"")
            );


            CellStyle porcentajePar =
                    crearEstiloDatos(workbook, IndexedColors.WHITE);
            porcentajePar.setDataFormat(
                    formato.getFormat("0.00%")
            );

            CellStyle porcentajeImpar =
                    crearEstiloDatos(workbook, IndexedColors.LIGHT_GREEN);
            porcentajeImpar.setDataFormat(
                    formato.getFormat("0.00%")
            );


            Row cabecera = hoja.createRow(0);
            cabecera.setHeightInPoints(32);

            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(COLUMNAS[i]);
                celda.setCellStyle(estiloCabecera);
            }

            int numeroFila = 1;

            for (ProductoEntity productoEntity : productos) {

                Producto producto = productoEntity.getProducto();
                Costes costes = producto.getCostes();

                boolean filaAlterna = numeroFila % 2 == 0;

                CellStyle estiloDatos =
                        filaAlterna ? estiloImpar : estiloPar;

                CellStyle estiloMoneda =
                        filaAlterna ? monedaImpar : monedaPar;

                CellStyle estiloPorcentaje =
                        filaAlterna ? porcentajeImpar : porcentajePar;

                Row fila = hoja.createRow(numeroFila);

                escribirTexto(
                        fila, 0, producto.getCodigo(), estiloDatos
                );

                escribirTexto(
                        fila, 1, producto.getNumeroSerie(), estiloDatos
                );


                escribirNumero(
                        fila, 2, producto.getPrecio(), estiloMoneda
                );

                // El porcentaje se almacena como decimal en Excel.
                BigDecimal descuento = producto.getDescuento() != null
                        ? producto.getDescuento()
                        : BigDecimal.ZERO;

                escribirNumero(
                        fila,
                        3,
                        descuento.divide(new BigDecimal("100")),
                        estiloPorcentaje
                );

                // Precio final calculado por ProductoService.
                escribirNumero(
                        fila,
                        4,
                        productoEntity.getPrecioFinal(),
                        estiloMoneda
                );

                // Costes de envío.
                escribirNumero(
                        fila,
                        5,
                        costes != null && costes.getCostesEnvio() != null
                                ? costes.getCostesEnvio()
                                : BigDecimal.ZERO,
                        estiloMoneda
                );

                // Costes de almacenaje.
                escribirNumero(
                        fila,
                        6,
                        costes != null && costes.getCostesAlmacenaje() != null
                                ? costes.getCostesAlmacenaje()
                                : BigDecimal.ZERO,
                        estiloMoneda
                );

                // Beneficio calculado por ProductoService.
                escribirNumero(
                        fila,
                        7,
                        productoEntity.getProfit(),
                        estiloMoneda
                );

                numeroFila++;
            }

            // Mantener visible la cabecera al desplazarse.
            hoja.createFreezePane(0, 1);

            // Ajustar el ancho de las columnas.
            for (int i = 0; i < COLUMNAS.length; i++) {
                hoja.autoSizeColumn(i);

                if (hoja.getColumnWidth(i) > 9000) {
                    hoja.setColumnWidth(i, 9000);
                }
            }

            // Guardar el fichero Excel.
            try (java.io.OutputStream salida =
                         Files.newOutputStream(archivoSalida)) {
                workbook.write(salida);
            }
        }
    }

    private static CellStyle crearEstiloDatos(
            Workbook workbook,
            IndexedColors color
    ) {
        CellStyle estilo = workbook.createCellStyle();

        estilo.setFillForegroundColor(color.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setAlignment(HorizontalAlignment.CENTER);

        ponerBordes(estilo);

        return estilo;
    }

    private static void ponerBordes(CellStyle estilo) {
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
    }

    private static void escribirTexto(
            Row fila,
            int columna,
            String valor,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor != null ? valor : "");
        celda.setCellStyle(estilo);
    }

    private static void escribirNumero(
            Row fila,
            int columna,
            BigDecimal valor,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(
                valor != null ? valor.doubleValue() : 0.0
        );
        celda.setCellStyle(estilo);
    }
}
