package org.dice.utilitytools.service.NtFileUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModelProcessor {

  @Value("${sparql.url.default}")
  String sparqlServelEndpoint;

  @Autowired private QueryExecutioner queryExecutioner;

  private HashMap<String, RDFProcessEntity> proccessMap = new HashMap<String, RDFProcessEntity>();

  public ModelProcessor() {}

  HashMap<String, RDFProcessEntity> ProcessModel(Model model) {
    queryExecutioner.setServiceRequestURL(sparqlServelEndpoint);
    StmtIterator iterator = model.listStatements();
    while (iterator.hasNext()) {
      Statement statement = iterator.next();
      if (IsIntended(statement)) {
        String key = ExtractKey(statement.getSubject().toString());
        if (!proccessMap.containsKey(key)) {
          // System.out.println("Size of the hash map: " + proccessMap.size());
          proccessMap.put(key, new RDFProcessEntity());
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
        proccessMap.put(key, currentStatement);
      }
    }

    return proccessMap;
  }

  private boolean IsIntended(Statement statement) {
    if (statement.getSubject().asNode().toString().contains("dbpedia")) {
      return false;
    }
    return true;
  }

  private String ExtractKey(String input) {
    String[] splited = input.split("/");
    return splited[splited.length - 1];
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
}
