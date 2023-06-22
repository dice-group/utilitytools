package org.dice.utilitytools.service;

import org.dice.utilitytools.service.handler.ITaskHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**

 This class is responsible for crawling through a directory and its sub-directories.
 */
public class FolderCrawler {
    HashMap<String,Boolean> paths = new HashMap<>();
    private List<ITaskHandler> taskHandler;
    public FolderCrawler(List<ITaskHandler> taskHandler) {
        this.taskHandler = taskHandler;
    }

    public void start(String startPath) throws IOException {
        crawl(new File(startPath));
        int numberOfSubGroups = taskHandler.size();

        if(numberOfSubGroups==1){
            taskHandler.get(0).handleTask(paths);
        }else{
            List<HashMap<String,Boolean>> subgroups = splitThePaths(paths,numberOfSubGroups);

            ExecutorService executor = Executors.newFixedThreadPool(subgroups.size());

            for (int  subGroupC = 0 ; subGroupC<subgroups.size() ; subGroupC++) {
                int finalSubGroupC = subGroupC;
                executor.submit(() -> {
                    taskHandler.get(finalSubGroupC).handleTask(subgroups.get(finalSubGroupC));
                });
            }
            executor.shutdown();
        }
    }

    private List<HashMap<String, Boolean>> splitThePaths(HashMap<String, Boolean> paths, int numberOfSubGroups) {
        List<HashMap<String, Boolean>> lhm = new ArrayList<>();
        for(int i = 0 ; i < numberOfSubGroups ; i++){
            lhm.add(new HashMap<>());
        }
        int counter = 0;
        for (Map.Entry<String, Boolean> entry:paths.entrySet()) {
            lhm.get(counter%numberOfSubGroups).put(entry.getKey(),entry.getValue());
            counter++;
        }
        return lhm;
    }

    private void crawl(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Folder: " + file.getName());
                crawl(file);
            } else {
                paths.put(file.getPath(),false);
            }
        }
    }
}
