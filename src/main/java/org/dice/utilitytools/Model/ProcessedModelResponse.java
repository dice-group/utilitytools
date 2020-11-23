package org.dice.utilitytools.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.rdf.model.Statement;

public class ProcessedModelResponse {
  private HashMap<String, RDFProcessEntity> intendedStatements;
  private List<Statement> unIntendedStatements;

  public ProcessedModelResponse() {
    intendedStatements = new HashMap<String, RDFProcessEntity>();
    unIntendedStatements = new ArrayList<Statement>();
  }

  public HashMap<String, RDFProcessEntity> getIntendedStatements() {
    return intendedStatements;
  }

  public void setIntendedStatements(HashMap<String, RDFProcessEntity> intendedStatements) {
    this.intendedStatements = intendedStatements;
  }

  public void Put(String key, RDFProcessEntity value) {
    this.intendedStatements.put(key, value);
  }

  public RDFProcessEntity Get(String key) {
    return this.intendedStatements.get(key);
  }

  public List<Statement> getUnIntendedStatements() {
    return unIntendedStatements;
  }

  public void setUnIntendedStatements(List<Statement> unIntendedStatements) {
    this.unIntendedStatements = unIntendedStatements;
  }

  public void Add(Statement value) {
    this.unIntendedStatements.add(value);
  }
}
