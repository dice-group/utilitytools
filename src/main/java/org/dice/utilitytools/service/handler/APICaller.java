package org.dice.utilitytools.service.handler;
import org.json.*;
import java.io.FileWriter;
import java.io.IOException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class APICaller implements ITaskHandler <Void, HashMap<String,Boolean>>{
    String url;
    String urlCheckStatus;
    String destinationPath;
    int counter = 0;

    String progressFilePath;

    File progressFile;

    boolean needToSaveProgress;

    public APICaller(String url, String destinationPath,String urlCheckStatus,String progressFilePath) {
        this.url = url;
        this.destinationPath = destinationPath;
        this.urlCheckStatus = urlCheckStatus;
        this.progressFilePath = progressFilePath;
        if(!progressFilePath.isEmpty() ){
            try{
                progressFile = new File(progressFilePath);
                needToSaveProgress  = true;
            }catch (Exception ex){
                ex.printStackTrace();
                needToSaveProgress = false;
            }
        }
    }

    @Override
    public Void handleTask(HashMap<String,Boolean> input) {

        int counter = 0;
        for (Map.Entry<String, Boolean> entry : input.entrySet()) {
            if(needToSaveProgress){
                if(entry.getValue()){
                    System.out.println("previous calculated");
                    continue;
                }
            }
            String json = entry.getKey();
            Boolean value = entry.getValue();
            boolean updateTheProgress = handleJson(json);
            if(updateTheProgress){
                entry.setValue(true);
            }
            counter  = counter+1;
            if(counter%100 == 0) {
                updateProgress(input);
            }
            // write file id in a file name
            JSONObject obj = new JSONObject(json);
            BigInteger id = obj.getBigInteger("id");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.progressFilePath+".ids", true))) {
                writer.write(id.toString());
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public Void handleTaskTSV(HashMap<String,Boolean> input) {

        int counter = 0;
        for (Map.Entry<String, Boolean> entry : input.entrySet()) {
            if(needToSaveProgress){
                if(entry.getValue()){
                    System.out.println("previous calculated");
                    continue;
                }
            }
            String tsv = entry.getKey();
            Boolean value = entry.getValue();
            boolean updateTheProgress = handleTsv(tsv);
            if(updateTheProgress){
                entry.setValue(true);
            }
            counter  = counter+1;
            if(counter%100 == 0) {
                updateProgress(input);
            }
        }
        return null;
    }

    private boolean handleJson(String jsonString) {
        try {
            JSONObject obj = new JSONObject(jsonString);
            BigInteger id = obj.getBigInteger("id");
            String label = obj.getString("label");
            String claim = obj.getString("claim");

            String jsonResponce = sendRequest(claim);
            obj = new JSONObject(jsonResponce);
            String nebulaId = obj.getString("id");
            //TimeUnit.SECONDS.sleep(5);
            try (FileWriter fileWriter = new FileWriter(this.progressFilePath+"_2.tsv", true)) {
                fileWriter.write(id+"\t"+label+"\t"+nebulaId+"\t"+claim);
                fileWriter.write(System.lineSeparator()); // Add a new line after the appended content
                System.out.println("Content appended to the file.");
                counter = counter +1;
                System.out.println(counter);

                boolean isDone = checkStatus(nebulaId);
                while (!isDone) {
                    try {
                        Thread.sleep(1000); // Wait for 1 second before checking again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isDone = checkStatus(nebulaId);
                }
                //update progress
                if(needToSaveProgress){
                    return true;
                }
            } catch (IOException e) {
                System.out.println("An error occurred while appending the file: " + e.getMessage());
                //do not update progress
            }
        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return false;
    }

    private boolean handleTsv(String tsvString) {
        try {
            String[] parts = tsvString.split("\t");
            String id = parts[0];
            String label = parts[1];
            String claim = parts[2];

            String jsonResponce = sendRequest(claim);
            JSONObject obj = new JSONObject(jsonResponce);
            String nebulaId = obj.getString("id");
            //TimeUnit.SECONDS.sleep(5);
            try (FileWriter fileWriter = new FileWriter(this.progressFilePath+"_2.tsv", true)) {
                fileWriter.write(id.toString()+"\t"+label+"\t"+nebulaId+"\t"+claim);
                fileWriter.write(System.lineSeparator()); // Add a new line after the appended content
                System.out.println("Content appended to the file.");
                counter = counter +1;
                System.out.println(counter);

                boolean isDone = checkStatus(nebulaId);
                while (!isDone) {
                    try {
                        Thread.sleep(1000); // Wait for 1 second before checking again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isDone = checkStatus(nebulaId);
                }
                //update progress
                if(needToSaveProgress){
                    return true;
                }
            } catch (IOException e) {
                System.out.println("An error occurred while appending the file: " + e.getMessage());
                //do not update progress
            }
        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }
        return false;
    }


    private boolean checkStatus(String id) {
        try {
            HttpResponse<String> res = Unirest.get(this.urlCheckStatus+urlEncode(id,"UTF-8"))
                    .asString();
            return res.getBody().contains("\"status\":\"done\"")||res.getBody().contains("\"status\":\"error\"");

        } catch (Exception  ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public String sendRequest(String textToquery) {
        try {
            HttpResponse<String> res = Unirest.get(this.url+urlEncode(textToquery,"UTF-8"))
                    .asString();
            return res.getBody();

        }catch (Exception ex){
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
            return "";
        }
    }

    public static String urlEncode(String originalString, String encoding) {
        try {
            return URLEncoder.encode(originalString, encoding);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encoding: " + encoding);
            return originalString;
        }
    }

    public void handleTaskFromFile() {
        HashMap<String,Boolean> progress = new HashMap<>();
        if(!needToSaveProgress){
            System.out.println("there was an error to read a progress file");
        }else{
                if (progressFile.exists()) {
                    try {
                        FileInputStream fileIn = new FileInputStream(this.progressFilePath);
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        progress = (HashMap) in.readObject();
                        in.close();
                        fileIn.close();
                    } catch (IOException i) {
                        i.printStackTrace();
                    } catch (ClassNotFoundException c) {
                        System.out.println("Employee class not found");
                        c.printStackTrace();
                    }
                    handleTask(progress);
                }
        }
    }
    public void handleTaskFromFileTSV() {
        HashMap<String,Boolean> progress = new HashMap<>();
        if(!needToSaveProgress){
            System.out.println("there was an error to read a progress file");
        }else{
            if (progressFile.exists()) {
                try {
                    FileInputStream fileIn = new FileInputStream(this.progressFilePath);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    progress = (HashMap) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (IOException i) {
                    i.printStackTrace();
                } catch (ClassNotFoundException c) {
                    System.out.println("Employee class not found");
                    c.printStackTrace();
                }
                handleTaskTSV(progress);
            }
        }
    }

    private void updateProgress(HashMap<String,Boolean> progress) {
        FileOutputStream fileOut = null;
        try {
            fileOut =
                    new FileOutputStream(this.progressFilePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            System.out.println("updatePr");
            out.writeObject(progress);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
