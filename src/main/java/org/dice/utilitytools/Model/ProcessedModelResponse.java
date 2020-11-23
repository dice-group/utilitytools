package org.dice.utilitytools.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.rdf.model.Statement;

public class ProcessedModelResponse {
  private HashMap<String, RDFProcessEntity> intendedStatements;
  private List<Statement> unIntendedStatements;
  private HashSet<String> shouldRemoveKeys;

  public ProcessedModelResponse() {
    intendedStatements = new HashMap<String, RDFProcessEntity>();
    unIntendedStatements = new ArrayList<Statement>();
    shouldRemoveKeys = new HashSet<String>();
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

  public HashSet<String> getShouldRemoveKeys() {
    return shouldRemoveKeys;
  }

  public void setShouldRemoveKeys(HashSet<String> shouldRemoveKeys) {
    this.shouldRemoveKeys = shouldRemoveKeys;
  }

  public void AddKeyForRemove(String key) {
    this.shouldRemoveKeys.add(key);
  }

  public Boolean IsKeyExistForRemove(String key) {
    return this.shouldRemoveKeys.contains(key);
  }
}
