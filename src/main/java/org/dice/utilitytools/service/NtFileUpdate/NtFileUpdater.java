package org.dice.utilitytools.service.NtFileUpdate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NtFileUpdater {

  @Autowired private RawFilePreProcess preProcessor;

  @Autowired private ModelBuilder model;

  @Autowired private ModelProcessor processor;

  HashMap<String, RDFProcessEntity> proccessMap = new HashMap<String, RDFProcessEntity>();

  /*protected SemWeb2NLVerbalizer verbalizer =
        new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), true, true);
  */

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

    FileWriter fw = new FileWriter("resulttext-" + fileNameParts[0] + ".txt");

    int counter = 0;
    while (iterator.hasNext()) {
      Entry<String, RDFProcessEntity> entry = iterator.next();
      // ss.add(StatementImpl(entry.component2().getSubject(), entry.component2().getPredicate(),
      // entry.component2().getObject()));
      if (entry.getValue().getDoesItChange()) {
        counter = counter + 1;
        fw.append(
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
    fw.append("total changes are : " + counter);
    fw.close();

    /*Document doc =
    verbalizer.generateDocument(statements, Paraphrasing.prop.getProperty("surfaceForms"));*/
  }
}
