package org.dice.utilitytools.service.transform;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class NTTransformer {
    public String transformAndAddPredicate(String input, String separatorCharacter,String predicate){
        String[] parts = input.split(separatorCharacter);

        if(predicate.charAt(0) != '<'){
            predicate = "<" + predicate;
        }

        if(predicate.charAt(predicate.length()-1) != '>'){
            predicate = predicate + ">";
        }

        return parts[0]+" "+predicate+" "+parts[1]+" .";
    }

    // accept file for each line do the transformation
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
}
