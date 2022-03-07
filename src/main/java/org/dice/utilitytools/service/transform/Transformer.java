package org.dice.utilitytools.service.transform;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// this cllas transform a file

@Component
public class Transformer {
    // transform and add predicate
    private String transformAndAddPredicate(String input, String separatorCharacter,String predicate){
        String[] parts = input.split(separatorCharacter);

        if(predicate.charAt(0) != '<'){
            predicate = "<" + predicate;
        }

        if(predicate.charAt(predicate.length()-1) != '>'){
            predicate = predicate + ">";
        }

        return parts[0]+" "+predicate+" "+parts[1]+" .";
    }

    // accept file for each line do the transformation and add predicate
    public List<String> transformAndAddPredicate(File input, String separatorCharacter, String predicate) throws FileNotFoundException {
        List<String> transformedList = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            transformedList.add(transformAndAddPredicate(line, separatorCharacter, predicate));
        }
        scanner.close();
        return transformedList;
    }

    // accept file for each line do the transformation
    public List<String> transformToList(File input) throws FileNotFoundException {
        List<String> transformedList = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            transformedList.add(line);
        }
        scanner.close();
        return transformedList;
    }
}
