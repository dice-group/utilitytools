package org.dice.utilitytools.service.load;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class IOService {
    public void writeListAsFile(List<String> input, String path,boolean append) throws IOException {
        FileWriter fw = new FileWriter(path, append); //the true will append the new data
        for(String s : input){
            fw.write(s);
            fw.write("\n");
        }
        fw.close();
        System.out.println("the writing of a file is done ");
    }

    public File readFile(String path){
        File file = new File(path);
        return file;
    }
}
