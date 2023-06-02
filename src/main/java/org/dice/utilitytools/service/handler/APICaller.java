package org.dice.utilitytools.service.handler;

import java.io.File;

public class APICaller implements ITaskHandler <Void, File>{
    String url;
    String destinationPath;

    public APICaller(String url, String destinationPath) {
        this.url = url;
        this.destinationPath = destinationPath;
    }

    @Override
    public Void handleTask(File input) {
        return null;
    }
}
