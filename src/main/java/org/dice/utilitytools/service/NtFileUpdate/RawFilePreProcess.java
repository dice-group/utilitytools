package org.dice.utilitytools.service.NtFileUpdate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

// this class read a file and copy it in another file as raw preprocess also it do some character replacement to enhance the file
// we dont want to change the source file then make a copy of it

@Component
public class RawFilePreProcess {
  public String Process(String fileName, String whatTodo) {
    String[] fileNameParts = fileName.split("\\.");
    // Iterator<Entry<String, RDFProcessEntity>> iterator = proccessMap.entrySet().iterator();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();

    String PreProccessedFileName =
        fileNameParts[0] + "_RawPreProcess_" + whatTodo +"-"+ dtf.format(now) + "." + fileNameParts[1];
    try (BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream( fileName)));
        FileWriter fw = new FileWriter( PreProccessedFileName)) {
      String st;
      // https://www.utf8-chartable.de/
      while ((st = br.readLine()) != null) {
        fw.write(Enhance(st));
      }
      fw.close();
      System.out.println("Raw file Preprocessed...");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("ERROR" + e.getMessage());
      e.printStackTrace();
    }

    return PreProccessedFileName;
  }

  public String Enhance(String input) {
    return input
        .replaceAll("\\s+", "")
        .replace("Å", "ō")
        .replace("Ä", "č")
        .replace("Ä‡", "ć")
        .replace("%C3%A1", "á")
        .replace("%C3%A4", "ä")
        .replace("%C3%A7", "ç")
        .replace("%C3%A9", "é")
        .replace("%C3%AD", "í")
        .replace("%C3%B6", "ö")
        .replace("%C3%B8", "ø")
        .replace("%C5%8D", "ō")
        .replace("%C5%8C", "Ō")
        .replace("%C3%89", "É")
        .trim()
        .concat("\n");
  }
}
