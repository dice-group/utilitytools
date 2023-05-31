package org.dice.utilitytools.service.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.dice.utilitytools.mapper.TranslatedResult2ElasticMapper;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Translator implements ITaskHandler<Void, File>{
    private String destinationPath;
    private TranslatedResult2ElasticMapper translatedResult2ElasticMapper;
    RestTemplate restTemplate;
    String url;
    public Translator(String url, TranslatedResult2ElasticMapper translatedResult2ElasticMapper, String destinationPath){
        this.translatedResult2ElasticMapper = translatedResult2ElasticMapper;
        this.restTemplate = new RestTemplate();
        this.url = url;
        this.destinationPath = destinationPath;
        Unirest.setTimeouts(600000,300000);
    }

    public String sendTranslationRequest(String textToTranslate) {
try {
    HttpResponse<String> res = Unirest.post("http://neamt.cs.upb.de:6100/custom-pipeline")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .field("components", "mbart_mt")
            .field("lang", "de")
            .field("query", textToTranslate)
            .asString();
    return res.getBody();

}catch (Exception ex){
    System.out.println(ex.getMessage());
    System.out.println(ex.getStackTrace());
    return "";
}

    }

    @Override
    public Void handleTask(File file) {
        try{
            System.out.println("translating this File: " + file.getName());
            String maintextToTranslate = extractTextForTranslate(file,"maintext");
            String descriptionToTranslate = extractTextForTranslate(file,"description");
            String titleToTranslate = extractTextForTranslate(file,"title");

            if(maintextToTranslate == null){
                System.out.println("error in extracing text for translate from  file: " + file.getAbsolutePath());
            }
            else {
                System.out.println("start translating");
                String translatedMaintextToTranslate = sendTranslationRequest(maintextToTranslate).replace("\"", "");
                String translatedDescription = sendTranslationRequest(descriptionToTranslate).replace("\"", "");
                String translatedTitle = sendTranslationRequest(titleToTranslate).replace("\"", "");
                System.out.println("done translating");
                if (translatedMaintextToTranslate.length() < 20) {
                    // log as a error
                    System.out.println("error in translation part for this file: " + file.getAbsolutePath() + " text to translate was: " + maintextToTranslate + " translated is : " + translatedMaintextToTranslate);
                } else {
                    //String textToSaveAsFile = replaceNode(file,"maintext",translatedResponce);
                    //String textToSaveAsFile = translatedResult2ElasticMapper.map(translatedResponce,url);
                    //saveResultAsFile(textToSaveAsFile, file.getName(),destinationPath);

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(file);

                    // Replace the value of the specified node
                    String newJsonToSave = replaceNodeValue(rootNode, "maintext", translatedMaintextToTranslate, translatedDescription, translatedTitle, maintextToTranslate.replace("\n", " ").trim());

                    boolean isSuccess = saveResultAsFile(newJsonToSave, file.getName(), destinationPath);

                    if (isSuccess) {
                        //remove file
                        file.delete();
                    }

                }
            }
        }catch (Exception exception){

        }
        return null;
    }

    private String replaceNodeValue(JsonNode node, String targetNode, String translatedMaintextToTranslate,String translatedDescription,String translatedTitle,String originalText) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"authors\": [],\n");
        sb = cloneNode(sb,node,"date_download",false);
        sb = cloneNode(sb,node,"date_modify",false);
        sb = cloneNode(sb,node,"date_publish",false);
        sb = cloneNode(sb,node,"description",false,translatedDescription);
        sb = cloneNode(sb,node,"filename",false);
        sb = cloneNode(sb,node,"image_url",false);
        sb = cloneNode(sb,node,"language",false,"en");
        sb = cloneNode(sb,node,"localpath",false);
        sb = cloneNode(sb,node,"maintext",false,translatedMaintextToTranslate);
        sb.append("\"text\": \""+translatedMaintextToTranslate+"\",\n");
        sb.append("\"originaltext\": \""+originalText+"\",\n");
        sb = cloneNode(sb,node,"source_domain",false);
        sb = cloneNode(sb,node,"title",false,translatedTitle);
        sb = cloneNode(sb,node,"title_page",false);
        sb = cloneNode(sb,node,"title_rss",false);
        sb = cloneNode(sb,node,"url",true);


        sb.append("}");
        return sb.toString();
    }

    StringBuilder cloneNode(StringBuilder sb, JsonNode node, String nodename, boolean isfinal){
        sb.append("\""+nodename+"\": \"");
        JsonNode newsTextNode = node.get(nodename);
        if (newsTextNode != null) {
            sb.append(newsTextNode.asText());
        }
        if(!isfinal) {
            sb.append("\",\n");
        }else{
            sb.append("\"\n");
        }
        return sb;
    }

    StringBuilder cloneNode(StringBuilder sb, JsonNode node, String nodename, boolean isfinal,String newValue){
        sb.append("\""+nodename+"\": \"");
        sb.append(newValue);
        if(!isfinal) {
            sb.append("\",\n");
        }else{
            sb.append("\"\n");
        }
        return sb;
    }
    private boolean saveResultAsFile(String textToSaveAsFile, String name, String destinationPath) {
        try {
            String fullPathForFileTosave = destinationPath+name;

            if(!isLastCharSlash(destinationPath)){
                fullPathForFileTosave = destinationPath+"/"+name;
            }
            // Using FileWriter class
            FileWriter fileWriter = new FileWriter(fullPathForFileTosave);
            fileWriter.write(textToSaveAsFile);
            fileWriter.close();
            if(textToSaveAsFile.length()>10) {
                return true;
            }else{
                return false;
            }
            // Or using Files class
            // Files.write(Paths.get(filePath), text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extractTextForTranslate(File file, String node) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode newsTextNode = rootNode.get(node);
            if (newsTextNode != null) {
                return newsTextNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isLastCharSlash(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        char lastChar = str.charAt(str.length() - 1);
        return lastChar == '/';
    }
}
