package org.dice.utilitytools.service.transform;

import java.io.File;
import java.util.*;

public class SplitSimpleTransformer implements ITransform <List<String>, File>{
    @Override
    public List<String> Transform(File input, String splitter) throws Exception {
        List<String> transformed = new ArrayList<>();
        Scanner scanner = new Scanner(input);
        String line = "";
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            String[] parts = line.split(splitter);
            for (String p: parts) {
                transformed.add(p);
            }
        }
        scanner.close();
        return transformed;
    }
}
