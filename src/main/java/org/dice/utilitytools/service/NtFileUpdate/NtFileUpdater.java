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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NtFileUpdater {

  private static final Logger logger = LoggerFactory.getLogger(SemWeb2NLVerbalizer.class);

  @Autowired private RawFilePreProcess preProcessor;

  @Autowired private ModelBuilder model;

  @Autowired private ModelProcessor processor;

  HashMap<String, RDFProcessEntity> proccessMap = new HashMap<String, RDFProcessEntity>();

  public NtFileUpdater() {
    super();
    // TODO Auto-generated constructor stub
  }

  protected SemWeb2NLVerbalizer verbalizer =
      new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), true, true);

  public void Update(String fileName) {
    try {
      fileName = preProcessor.Process(fileName);
      model.build(fileName);
      proccessMap = processor.ProcessModel(model.getModel());
      ExtractResult(fileName);
    } catch (Exception exp) {
      String kkk = exp.getMessage();
      System.out.println(kkk);
    }
  }

  private void ExtractResult(String fileName) throws IOException {
    String[] fileNameParts = fileName.split("\\.");

    Iterator<Entry<String, RDFProcessEntity>> iterator = proccessMap.entrySet().iterator();

    FileWriter fw = new FileWriter("resulttext-" + fileNameParts[0] + ".tsv");
    FileWriter fwReport = new FileWriter("Report-resulttext-" + fileNameParts[0] + ".txt");

    List<Statement> statements = new ArrayList<Statement>();

    // int counter = 0;
    while (iterator.hasNext()) {
      try {
        Entry<String, RDFProcessEntity> entry = iterator.next();
        logger.debug(entry.getKey());
        System.out.println(entry.getKey());
        // ss.add(StatementImpl(entry.component2().getSubject(), entry.component2().getPredicate(),
        // entry.component2().getObject()));
        if (entry.getValue().getDoesItChange()) {
          // counter = counter + 1;
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

          Resource resourceSubject = ResourceFactory.createResource(entry.getValue().getSubject());
          Property property = ResourceFactory.createProperty(entry.getValue().getPredicate());
          Resource resourceObject = ResourceFactory.createResource(entry.getValue().getObject());
          statements = new ArrayList<Statement>();
          statements.add(new StatementImpl(resourceSubject, property, resourceObject));
          Document doc =
              verbalizer.generateDocument(
                  statements, Paraphrasing.prop.getProperty("surfaceForms"));
          fw.append(entry.getKey() + " " + doc.getText() + "\n");
        }
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }

    // fw.append("total changes are : " + counter);
    fw.close();
    fwReport.close();
  }
}
