package org.dice.utilitytools.service.NtFileUpdate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.github.andrewoma.dexx.collection.HashMap;
import com.github.andrewoma.dexx.collection.Pair;

@Component
public class NtFileUpdater {

  @Value("${sparql.url.default}")
  String sparqlServelEndpoint;

  @Autowired private QueryExecutioner queryExecutioner;

  Model model;
  HashMap<String, RDFProcessEntity> proccessMap = new HashMap<String, RDFProcessEntity>();

  /*protected SemWeb2NLVerbalizer verbalizer =
        new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), true, true);
  */
  public void FillModel(String fileName) throws FileNotFoundException {
    model = ModelFactory.createDefaultModel();
    model.read(
        // getClass().getResourceAsStream("/org/dice/utilitytools/NtfileUpdate/" + fileName),
        new FileInputStream("./" + fileName), "", "N-TRIPLE");
  }

  public void Update(String fileName) {
    try {
      queryExecutioner.setServiceRequestURL(sparqlServelEndpoint);
      // PreProccessFile(fileName);
      FillModel(fileName);
      ProcessModel();
      ExtractResult(fileName);
    } catch (Exception exp) {
      String kkk = exp.getMessage();
    }
  }

  private void ExtractResult(String fileName) throws IOException {
    String[] fileNameParts = fileName.split("\\.");
    List<Statement> ss = new ArrayList<Statement>();

    Iterator<Pair<String, RDFProcessEntity>> iterator = proccessMap.iterator();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();
    FileWriter fw =
        new FileWriter("resulttext-" + fileNameParts[0] + "_" + dtf.format(now) + ".txt");
    int counter = 0;
    while (iterator.hasNext()) {
      Pair<String, RDFProcessEntity> entry = iterator.next();
      // ss.add(StatementImpl(entry.component2().getSubject(), entry.component2().getPredicate(),
      // entry.component2().getObject()));
      if (entry.component2().getDoesItChange()) {
        counter = counter + 1;
        fw.append(
            entry.component1().toString()
                + " "
                + entry.component2().getSubject()
                + " "
                + entry.component2().getPredicate()
                + " "
                + entry.component2().getObject()
                + " it was "
                + entry.component2().getPreviusObject()
                + "\n");
      }
    }
    fw.append("total changes are : " + counter);
    fw.close();

    /*Document doc =
    verbalizer.generateDocument(statements, Paraphrasing.prop.getProperty("surfaceForms"));*/
  }

  private void ProcessModel() {
    StmtIterator iterator = model.listStatements();
    while (iterator.hasNext()) {
      Statement statement = iterator.next();
      if (IsIntended(statement)) {
        String key = ExtractKey(statement.getSubject().toString());
        if (!proccessMap.containsKey(key)) {
          // System.out.println("Size of the hash map: " + proccessMap.size());
          proccessMap = proccessMap.put(key, new RDFProcessEntity());
          // System.out.println("Size of the hash map: " + proccessMap.size());
        }
        // System.out.println("Size of the hash map: " + proccessMap.size());
        RDFProcessEntity currentStatement = proccessMap.get(key);
        RDFNode object = statement.getObject();
        String predicate = statement.getPredicate().toString().toLowerCase();

        if (predicate.contains("hastruthvalue")) {
          if (object.toString().contains("0.0")) {
            currentStatement.setHasTruthValue(false);
          } else {
            currentStatement.setHasTruthValue(true);
          }
          currentStatement.AddStep();
        }

        if (predicate.contains("object")) {
          currentStatement.setObject(object.toString());
          currentStatement.AddStep();
        }

        if (predicate.contains("predicate")) {
          currentStatement.setPredicate(object.toString());
          currentStatement.AddStep();
        }

        if (predicate.contains("subject")) {
          currentStatement.setSubject(object.toString());
          currentStatement.AddStep();
        }

        if (!currentStatement.getIsProcessed() && currentStatement.getRedyForProcess() == 4) {
          // System.out.println("befor: " + current.toString());
          currentStatement = Process(currentStatement);
          currentStatement.setIsProcessed(true);
          // System.out.println("after: " + current.toString());
        }
        proccessMap = proccessMap.put(key, currentStatement);
      }
    }
  }

  private RDFProcessEntity Process(RDFProcessEntity current) {
    List<String> actualObjects = DoQuery(current.getSubject(), current.getPredicate());
    if (current.getHasTruthValue()) {
      // this statement should be valid
      if (actualObjects.size() == 0) {
        // if there is no result for Query then this triple is not valid and it is not we need
        current.setAfterProcessResultIsAcceptable(false);
      } else {
        // check if the current object is valid
        if (!actualObjects.contains(current.getObject())) {
          // replace with valid object
          current.setObject(actualObjects.get(0).toString());
          current.setDoesItChange(true);
        }
        current.setAfterProcessResultIsAcceptable(true);
      }
    } else {
      // this statement should not be valid
      if (actualObjects.size() == 0) {
        // if there is no result for Query then this triple is not valid and then there is no need
        // for change
        current.setAfterProcessResultIsAcceptable(true);
      } else {
        // check if the current object is valid then we should change it with something else
        if (actualObjects.contains(current.getObject())) {
          // replace with valid object
          // TODO
          current.setObject("something else");
          current.setDoesItChange(true);
        }
        current.setAfterProcessResultIsAcceptable(true);
      }
    }
    return current;
  }

  private List<String> DoQuery(String subject, String predicate) {
    String textOfQuery =
        "select distinct  ?o where " + "{<" + subject + "> <" + predicate + "> ?o" + "}";
    Query query = QueryFactory.create(textOfQuery);
    QueryExecution queryExecution = queryExecutioner.getQueryExecution(query);
    List<String> objects = new ArrayList<String>();
    ResultSet resultSet = queryExecution.execSelect();
    boolean everyThingIsFine = true;
    while (resultSet.hasNext()) {
      RDFNode thisNode = resultSet.next().get("?o");
      if (thisNode.isResource()) {
        objects.add(thisNode.asResource().toString());
      } else {
        everyThingIsFine = false;
      }
    }
    queryExecution.close();
    if (!everyThingIsFine) {
      // System.out.println(predicate);
    }
    return objects;
  }

  private String ExtractKey(String input) {
    String[] splited = input.split("/");
    return splited[splited.length - 1];
  }

  private boolean IsIntended(Statement statement) {
    if (statement.getSubject().asNode().toString().contains("dbpedia")) {
      return false;
    }
    return true;
  }

  public String PreProccessFile(String fileName) {
    String[] fileNameParts = fileName.split("\\.");
    Iterator<Pair<String, RDFProcessEntity>> iterator = proccessMap.iterator();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();

    String PreProccessedFileName = fileNameParts[0] + dtf.format(now) + "." + fileNameParts[1];
    // PreProccessedFileName = PreProccessedFileName.replace("/", "");
    try (BufferedReader br =
            new BufferedReader(
                new InputStreamReader(
                    // this.getClass().getResourceAsStream(fileName)
                    new FileInputStream("./" + fileName)));
        FileWriter fw = new FileWriter("./" + PreProccessedFileName)) {
      String st;
      while ((st = br.readLine()) != null) {
        fw.write(
            st.replaceAll("\\s+", "")
                .replaceAll("Å", "ō")
                .replaceAll("Ä", "č")
                .replaceAll("Ä‡", "ć")
                .trim()
                .concat("\n"));
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return PreProccessedFileName;
  }
}
