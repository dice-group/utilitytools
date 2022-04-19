package org.dice.utilitytools.service.NtFileUpdate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.stereotype.Component;

@Component
public class ModelBuilder {

  private Model model;

  public void build(String fileName) throws FileNotFoundException {
    model = ModelFactory.createDefaultModel();
    model.read(
        // getClass().getResourceAsStream("/org/dice/utilitytools/NtfileUpdate/" + fileName),
        new FileInputStream( fileName), "", "N-TRIPLE");
    System.out.println("Model built");
  }

  public Model getModel() {
    return model;
  }
}
