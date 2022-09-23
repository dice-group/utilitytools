package org.dice.utilitytools.service.transform;

import java.io.File;
import java.io.FileInputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.stereotype.Component;

@Component
public class RDFModelTransform{
    String type = "Turtle";

    public RDFModelTransform() {
        type = "Turtle";
    }

    public RDFModelTransform(String type) {
        this.type = type;
    }

    public Model transform(File input, String splitter) {
        try {
            Model model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(input), "",type);
            return model;
        }
        catch(Exception ex){
            String ddd = ex.getMessage();
        }
        return null;
    }
}
