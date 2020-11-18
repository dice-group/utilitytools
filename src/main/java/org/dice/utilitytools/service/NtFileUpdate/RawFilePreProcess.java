package org.dice.utilitytools.service.NtFileUpdate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class RawFilePreProcess {
  public String Process(String fileName) {
    String[] fileNameParts = fileName.split("\\.");
    // Iterator<Entry<String, RDFProcessEntity>> iterator = proccessMap.entrySet().iterator();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();

    String PreProccessedFileName = fileNameParts[0] + dtf.format(now) + "." + fileNameParts[1];
    // PreProccessedFileName = PreProccessedFileName.replace("/", "");
    try (BufferedReader br =
            new BufferedReader(
                new InputStreamReader(
                    // this.getClass().getResourceAsStream(fileName)
                    new FileInputStream("./" + fileName)));
        FileWriter fw = new FileWriter("./" + PreProccessedFileName)) {
      String st;
      // https://www.utf8-chartable.de/
      while ((st = br.readLine()) != null) {
        fw.write(
            st.replaceAll("\\s+", "")
                .replaceAll("Å", "ō")
                .replaceAll("Ä", "č")
                .replaceAll("Ä‡", "ć")
                .replaceAll("%C3%A1", "á")
                .replaceAll("%C3%A4", "ä")
                .replaceAll("%C3%A7", "ç")
                .replaceAll("%C3%A9", "é")
                .replaceAll("%C3%AD", "í")
                .replaceAll("%C3%B6", "ö")
                .replaceAll("%C3%B8", "ø")
                .replaceAll("%C5%8D", "ō")
                .replaceAll("%C5%8C", "Ō")
                .replaceAll("%C3%89", "É")
                .trim()
                .concat("\n"));
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return PreProccessedFileName;
  }
}
