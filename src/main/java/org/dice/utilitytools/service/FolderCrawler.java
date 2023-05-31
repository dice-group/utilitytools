package org.dice.utilitytools.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dice.utilitytools.mapper.TranslatedResult2ElasticMapper;
import org.dice.utilitytools.service.handler.ITaskHandler;
import org.dice.utilitytools.service.handler.Translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**

 This class is responsible for crawling through a directory and its sub-directories.
 */
public class FolderCrawler {
    private ITaskHandler taskHandler;
    public FolderCrawler(ITaskHandler taskHandler) {
        this.taskHandler = taskHandler;
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
                taskHandler.handleTask(file);
            }
        }
    }
}
