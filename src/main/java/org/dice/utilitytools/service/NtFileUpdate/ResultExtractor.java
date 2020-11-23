package org.dice.utilitytools.service.NtFileUpdate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.bengal.paraphrasing.Paraphrasing;
import org.aksw.simba.bengal.verbalizer.SemWeb2NLVerbalizer;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.dice.utilitytools.Model.ProcessedModelResponse;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResultExtractor {

  private static final Logger logger = LoggerFactory.getLogger(SemWeb2NLVerbalizer.class);

  protected SemWeb2NLVerbalizer verbalizer =
      new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), true, true);

  public HashMap<String, String> ExtractResult(
      ProcessedModelResponse proccessedModel, String fileName, boolean isTrainingData)
      throws IOException {

    System.out.println("Start Extract result");

    HashMap<String, String> resultMap = new HashMap<String, String>();

    String[] fileNameParts = fileName.split("\\.");

    FileWriter fw =
        new FileWriter(fileNameParts[0].replace("RawPreProcess", "ResultText") + ".tsv");
    FileWriter fwReport =
        new FileWriter(fileNameParts[0].replace("RawPreProcess", "ResultTextReport") + ".txt");

    FileWriter fwNt = new FileWriter(fileNameParts[0].replace("RawPreProcess", "New") + ".nt");

    Iterator<Entry<String, RDFProcessEntity>> iterator =
        proccessedModel.getIntendedStatements().entrySet().iterator();

    while (iterator.hasNext()) {
      try {
        Entry<String, RDFProcessEntity> entry = iterator.next();
        // System.out.println(entry.getKey());
        if (proccessedModel.IsKeyExistForRemove(entry.getKey())) {
          continue;
        }
        GenerateNtFile(fwNt, entry);
        if (entry.getValue().getDoesItChange()) {
          GenerateReport(fwReport, entry);
          String verbalizedStatement = GenerateVerbalizedSentence(entry);
          if (isTrainingData) {
            resultMap.put(
                entry.getKey(),
                verbalizedStatement + "\t" + entry.getValue().getStringFormHasTruthValue());
          } else {
            resultMap.put(entry.getKey(), verbalizedStatement);
          }
          fw.append(entry.getKey() + " " + verbalizedStatement + "\n");
        }
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }

    fw.close();
    fwReport.close();

    System.out.println(
        "getUnIntendedStatements are :" + proccessedModel.getUnIntendedStatements().size());

    for (Statement s : proccessedModel.getUnIntendedStatements()) {
      fwNt.append(s.getSubject() + " " + s.getPredicate() + " " + s.getObject() + "\n");
    }

    fwNt.close();
    System.out.println("Changes are ready");
    return resultMap;
  }

  private String GenerateVerbalizedSentence(Entry<String, RDFProcessEntity> entry) {
    Resource resourceSubject = ResourceFactory.createResource(entry.getValue().getSubject());
    Property property = ResourceFactory.createProperty(entry.getValue().getPredicate());
    Resource resourceObject = ResourceFactory.createResource(entry.getValue().getObject());
    List<Statement> statements = new ArrayList<Statement>();
    statements.add(new StatementImpl(resourceSubject, property, resourceObject));
    Document doc =
        verbalizer.generateDocument(statements, Paraphrasing.prop.getProperty("surfaceForms"));
    return doc.getText();
  }

  private void GenerateReport(FileWriter fwReport, Entry<String, RDFProcessEntity> entry)
      throws IOException {
    fwReport.append(
        entry.getKey().toString()
            + " "
            + entry.getValue().getSubject()
            + " "
            + entry.getValue().getPredicate()
            + " "
            + entry.getValue().getObject()
            + " it was "
            + entry.getValue().getPreviusObject()
            + "\n");
  }

  private void GenerateNtFile(FileWriter fwNt, Entry<String, RDFProcessEntity> entry)
      throws IOException {
    // <http://swc2017.aksw.org/task2/dataset/3494664>
    // <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
    // <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .
    fwNt.append(
        "<http://swc2017.aksw.org/task2/dataset/"
            + entry.getKey()
            + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
            + entry.getValue().getType()
            + "> .\n");

    if (entry.getValue().getHasTruthValue()) {
      // <http://swc2017.aksw.org/task2/dataset/3494664> <http://swc2017.aksw.org/hasTruthValue>
      // "1.0"^^<http://www.w3.org/2001/XMLSchema#float> .
      fwNt.append(
          "<http://swc2017.aksw.org/task2/dataset/"
              + entry.getKey()
              + "> <http://swc2017.aksw.org/hasTruthValue> \"1.0\"^^<http://www.w3.org/2001/XMLSchema#float> .\n");
    } else {
      fwNt.append(
          "<http://swc2017.aksw.org/task2/dataset/"
              + entry.getKey()
              + "> <http://swc2017.aksw.org/hasTruthValue> \"0.0\"^^<http://www.w3.org/2001/XMLSchema#float> .\n");
    }

    // <http://swc2017.aksw.org/task2/dataset/3494664>
    // <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject>
    // <http://dbpedia.org/resource/Chris_Kaman> .
    fwNt.append(
        "<http://swc2017.aksw.org/task2/dataset/"
            + entry.getKey()
            + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <"
            + entry.getValue().getSubject()
            + "> .\n");

    // <http://swc2017.aksw.org/task2/dataset/3494664>
    // <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://dbpedia.org/ontology/author> .
    fwNt.append(
        "<http://swc2017.aksw.org/task2/dataset/"
            + entry.getKey()
            + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <"
            + entry.getValue().getPredicate()
            + "> .\n");

    // <http://swc2017.aksw.org/task2/dataset/3494664>
    // <http://www.w3.org/1999/02/22-rdf-syntax-ns#object>
    // <http://dbpedia.org/resource/New_Orleans_Hornets> .
    fwNt.append(
        "<http://swc2017.aksw.org/task2/dataset/"
            + entry.getKey()
            + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> <"
            + entry.getValue().getObject()
            + "> .\n");
  }
}
