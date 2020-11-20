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
      HashMap<String, RDFProcessEntity> proccessMap, String fileName, boolean isTrainingData)
      throws IOException {

    HashMap<String, String> resultMap = new HashMap<String, String>();

    String[] fileNameParts = fileName.split("\\.");

    FileWriter fw =
        new FileWriter(fileNameParts[0].replace("RawPreProcess", "ResultText") + ".tsv");
    FileWriter fwReport =
        new FileWriter(fileNameParts[0].replace("RawPreProcess", "ResultTextReport") + ".txt");

    Iterator<Entry<String, RDFProcessEntity>> iterator = proccessMap.entrySet().iterator();

    while (iterator.hasNext()) {
      try {
        Entry<String, RDFProcessEntity> entry = iterator.next();
        // System.out.println(entry.getKey());
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
}
