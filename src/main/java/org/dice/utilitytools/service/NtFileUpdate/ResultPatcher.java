package org.dice.utilitytools.service.NtFileUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;

@Component
public class ResultPatcher {
  HashMap<String, String> mainFileMap = new HashMap<String, String>();

  public void patch(
      String fileName, HashMap<String, String> replaceStatements, HashSet<String> toRemove) {
    try {
      System.out.println("start patch tsv file");
      mainFileMap = ReadFile(fileName);
      mainFileMap = Replace(mainFileMap, replaceStatements);
      WriteFinalResult(mainFileMap, fileName, toRemove);
      System.out.println("done patch tsv file");
    } catch (Exception exp) {
      System.out.println("face error:" + exp.getMessage());
    }
  }

  void WriteFinalResult(HashMap<String, String> map, String fileName, HashSet<String> toRemove) {

    String[] fileNameParts = fileName.split("\\.");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();

    Iterator<HashMap.Entry<String, String>> itr = map.entrySet().iterator();
    try (FileWriter fw =
        new FileWriter(
            "./" + fileNameParts[0] + "_new_" + dtf.format(now) + "." + fileNameParts[1])) {
      String st;
      while (itr.hasNext()) {
        Entry<String, String> entry = itr.next();
        if (!toRemove.contains(entry.getKey()))
          fw.append(entry.getKey() + "\t" + entry.getValue() + "\n");
      }
      fw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  HashMap<String, String> Replace(
      HashMap<String, String> mainFileMap, HashMap<String, String> replaceStatements) {
    Iterator<HashMap.Entry<String, String>> itr = replaceStatements.entrySet().iterator();
    while (itr.hasNext()) {
      Entry<String, String> entry = itr.next();
      if (!entry.getValue().equals("")) {
        mainFileMap.put(entry.getKey(), entry.getValue());
      } else {
        System.out.println(
            "for key : " + entry.getKey() + " the body is null then nothing replaced");
      }
    }
    return mainFileMap;
  }

  HashMap<String, String> ReadFile(String fileName) {
    HashMap<String, String> map = new HashMap<String, String>();
    File file = new File(fileName);
    if (!file.exists()) {
      return map;
    }
    try (BufferedReader TSVReader = new BufferedReader(new FileReader(file))) {
      String line = null;
      while ((line = TSVReader.readLine()) != null) {
        String[] lineItems = line.split("\t");
        if (isNumeric(lineItems[0])) {
          if (lineItems.length == 2) {
            map.put(lineItems[0], lineItems[1]);
          }
          if (lineItems.length == 3) {
            map.put(lineItems[0], lineItems[1] + "\t" + lineItems[2]);
          }
        }
      }
      return map;
    } catch (Exception e) {
      System.out.println("Something went wrong");
      return null;
    }
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}
