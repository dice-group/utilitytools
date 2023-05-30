package org.dice.utilitytools.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dice.utilitytools.mapper.TranslatedResult2ElasticMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**

 This class is responsible for crawling through a directory and its sub-directories, translating and mapping all the JSON files from German to English.
 It is specifically designed for translating a German news dataset.
 */
public class FolderCrawler {
    private Translator translator;
    private TranslatedResult2ElasticMapper translatedResult2ElasticMapper;
    private String destinationPath;
    public FolderCrawler(Translator translator, TranslatedResult2ElasticMapper translatedResult2ElasticMapper,String destinationPath) {
        this.translatedResult2ElasticMapper = translatedResult2ElasticMapper;
        this.translator = translator;
        this.destinationPath = destinationPath;
    }

    public void start(String startPath) throws IOException {
        crawl(new File(startPath));
    }

    private void crawl(File folder) throws IOException {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Folder: " + file.getName());
                crawl(file);
            } else {
                System.out.println("translating this File: " + file.getName());
                String maintextToTranslate = extractTextForTranslate(file,"maintext");
                String descriptionToTranslate = extractTextForTranslate(file,"description");
                String titleToTranslate = extractTextForTranslate(file,"title");

                if(maintextToTranslate == null){
                    System.out.println("error in extracing text for translate from  file: " + file.getAbsolutePath());
                }
                else
                {
                    System.out.println("start translating");
                    String translatedMaintextToTranslate = translator.sendTranslationRequest(maintextToTranslate).replace("\"","");
                    String translatedDescription = translator.sendTranslationRequest(descriptionToTranslate).replace("\"","");;
                    String translatedTitle = translator.sendTranslationRequest(titleToTranslate).replace("\"","");
                    System.out.println("done translating");
                    if(translatedMaintextToTranslate.length()<20){
                        // log as a error
                        System.out.println("error in translation part for this file: " + file.getAbsolutePath()+" text to translate was: "+maintextToTranslate+" translated is : "+ translatedMaintextToTranslate);
                    }else{
                        //String textToSaveAsFile = replaceNode(file,"maintext",translatedResponce);
                        //String textToSaveAsFile = translatedResult2ElasticMapper.map(translatedResponce,url);
                        //saveResultAsFile(textToSaveAsFile, file.getName(),destinationPath);

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(file);

                        // Replace the value of the specified node
                        String newJsonToSave = replaceNodeValue(rootNode, "maintext", translatedMaintextToTranslate,translatedDescription,translatedTitle,maintextToTranslate.replace("\n"," ").trim());

                        boolean isSuccess = saveResultAsFile(newJsonToSave,file.getName(),destinationPath);

                        if(isSuccess){
                            //remove file
                            file.delete();
                        }

                    }
                }
            }
        }
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
