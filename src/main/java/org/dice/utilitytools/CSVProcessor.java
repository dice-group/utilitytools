package org.dice.utilitytools;

import java.io.*;
import java.util.HashMap;

public class CSVProcessor {
    HashMap<String, String> numbers ;
    public void fillTheTemplate(String inputFilePath, String templatePath, String outputResultPath){
        File input = new File(inputFilePath);
        File template = new File(templatePath);
        StringBuilder result = new StringBuilder();

        convertToHashTable(input);

        int lc = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream( templatePath)));
        FileWriter fw = new FileWriter( outputResultPath)) {
            String st;
            // https://www.utf8-chartable.de/
            while ((st = br.readLine()) != null) {
                if(lc<2){
                    lc++;
                    fw.write(st);
                    fw.write("\n");
                }else{
                    String[] parts = st.split(";");
                    String dateddmmyyyy = parts[0];
                    String res = generateTheResults(dateddmmyyyy,parts[1]);
                    fw.write(res);
                }
            }
            fw.close();
            System.out.println("Raw file Preprocessed...");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("ERROR" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void convertToHashTable(File input) {
        numbers = new HashMap<String, String>();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                String date = parts[1];
                numbers.put(date,line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String generateTheResults( String dateddmmyyyy, String uhr) {
        String[] partsDate = dateddmmyyyy.split("\\.");
        String dateyyyymmdd = partsDate[2]+"-"+partsDate[1]+"-"+partsDate[0];
        if(numbers.containsKey(dateyyyymmdd)){
            System.out.println("find");
        }else{
            System.out.println("cantFind");
        }

        String line = numbers.get(dateyyyymmdd);
        String[] parts = line.split(";");
        StringBuilder sb = new StringBuilder();
        sb.append(dateddmmyyyy);
        sb.append(";");
        sb.append(uhr);
        sb.append(";");
        for(int i = 2; i < parts.length-1 ; i++){
            sb.append(parts[i]);
            sb.append(";");
        }
        sb.append(parts[parts.length-1]);
        sb.append("\n");
        return sb.toString();
    }
}
