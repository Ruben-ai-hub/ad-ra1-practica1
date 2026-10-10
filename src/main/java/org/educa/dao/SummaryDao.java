package org.educa.dao;

import org.educa.entity.SummaryEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SummaryDao {
    public void exportToFile(SummaryEntity summary, String path) throws IOException {
        Path carpeta = Paths.get(path);
        Files.createDirectories(carpeta);

        Path salida = carpeta.resolve("result_" + summary.getName() + ".txt");
        Files.writeString(salida, summary.toPrint(), StandardCharsets.UTF_8);
    }
}
