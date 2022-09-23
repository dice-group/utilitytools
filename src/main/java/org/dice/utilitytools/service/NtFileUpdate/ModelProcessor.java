package org.dice.utilitytools.service.NtFileUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.dice.utilitytools.Model.ProcessedModelResponse;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// this class make ProcessedModelResponse for a Model and after that process it

@Component
public class ModelProcessor {

  @Value("${sparql.url.default}")
  String sparqlServerEndpoint;

  @Autowired
  private QueryExecutioner queryExecutioner;

  private HashMap<String,String> equivalentPredicates;

  private HashMap<String, Integer> lastSelectedObject = new HashMap<String, Integer>();
  private HashSet<String> shouldRemoveKeys = new HashSet<String>();

  public ModelProcessor() {
    equivalentPredicates = new HashMap<>();
    equivalentPredicates.put("http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/property/birthPlace");
    equivalentPredicates.put("http://dbpedia.org/ontology/deathPlace","http://dbpedia.org/property/deathPlace");
    equivalentPredicates.put("http://dbpedia.org/ontology/team","http://dbpedia.org/property/team");
    equivalentPredicates.put("http://dbpedia.org/ontology/author","http://dbpedia.org/property/author");
    equivalentPredicates.put("http://dbpedia.org/ontology/starring","http://dbpedia.org/property/starring");
    equivalentPredicates.put("http://dbpedia.org/ontology/foundationPlace","http://dbpedia.org/property/foundationPlace");
    equivalentPredicates.put("http://dbpedia.org/ontology/award","http://dbpedia.org/property/award");
  }

  ProcessedModelResponse ProcessModelAndUpdate(Model model) {
    System.out.println("Start Processing for updating...");
    System.out.println("it may take a few minutes");

    ProcessedModelResponse responce = new ProcessedModelResponse();

    //queryExecutioner.setServiceRequestURL(sparqlServerEndpoint);
    StmtIterator iterator = model.listStatements();
    while (iterator.hasNext()) {
      Statement statement = iterator.next();
      if (IsIntended(statement)) {
        String key = ExtractKey(statement.getSubject().toString());
        if (!responce.getIntendedStatements().containsKey(key)) {
          // System.out.println("Size of the hash map: " + proccessMap.size());
          responce.Put(key, new RDFProcessEntity());
          // System.out.println("Size of the hash map: " + proccessMap.size());
        }
        // System.out.println("Size of the hash map: " + proccessMap.size());
        RDFProcessEntity currentStatement = responce.Get(key);
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

          if (object.toString().toLowerCase().contains("office")) {
            responce.AddKeyForRemove(key);
          }
        }

        if (predicate.contains("subject")) {
          currentStatement.setSubject(object.toString());
          currentStatement.AddStep();
        }

        if (predicate.contains("type")) {
          currentStatement.setType(object.toString());
          currentStatement.AddStep();
        }

        if (!currentStatement.getIsProcessed() && currentStatement.getReadyForProcess() == 5) {
          currentStatement = ProcessAndUpdate(currentStatement);
          currentStatement.setIsProcessed(true);
        }
        responce.Put(key, currentStatement);
      } else {
        responce.Add(statement);
      }
    }
    System.out.println("Process done");
    return responce;
  }

  public boolean IsIntended(Statement statement) {
    if (statement.getSubject().asNode().toString().contains("dbpedia")) {
      return false;
    }
    return true;
  }

  public String ExtractKey(String input) {
    String[] splited = input.split("/");
    return splited[splited.length - 1];
  }

  public RDFProcessEntity ProcessAndUpdate(RDFProcessEntity current) {
    List<String> actualObjects = DoQuery(current.getSubject(), current.getPredicate());

    if (current.getHasTruthValue()) {
      // this statement should be valid
      if (actualObjects.size() == 0) {
        // check if the predicate could change
        if(isCandidateForChange(current.getPredicate())) {
          // get a new predicate
          String newPredicate = getCandidateForChange(current.getPredicate());
          // check if with new predicate we can prove the fact
          current.setPredicate(newPredicate);
          current.setDoesThePredicateChange(true);
          actualObjects = DoQuery(current.getSubject(), current.getPredicate());
          if(actualObjects.size()>0){
            // the predicate changed now let see what could we do with the object
            current = updateTheCurrentObjectBaseOnTheResultsOfTheQueryWhichSearchedSubjectAndPredicate(current,actualObjects);
          }else{
            // no result then the triple is not exist anymore
            current.setAfterProcessResultIsAcceptable(false);
          }
        }else{
          // if there is no result for Query then this triple is not valid and it is not we need
          current.setAfterProcessResultIsAcceptable(false);
        }
      } else
        {
          current = updateTheCurrentObjectBaseOnTheResultsOfTheQueryWhichSearchedSubjectAndPredicate(current,actualObjects);
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
          current.setObject(ProvideNewFalseObject(current.getPredicate(), current.getObject()));
          current.setDoesItChange(true);
        }
        current.setAfterProcessResultIsAcceptable(true);
      }
    }
    return current;
  }

  private String getCandidateForChange(String predicate) {
    return equivalentPredicates.get(predicate);
  }

  private boolean isCandidateForChange(String predicate) {
    return equivalentPredicates.containsKey(predicate);
  }

  private RDFProcessEntity updateTheCurrentObjectBaseOnTheResultsOfTheQueryWhichSearchedSubjectAndPredicate(RDFProcessEntity current, List<String> actualObjects) {
    // check if the current object is valid
    // here we know that the subject and predicate are correct then lets see if
    // the object is correct
    if (!actualObjects.contains(current.getObject())) {
      // if not then replace it with a valid object
      String checkKey = current.getSubject() + current.getPredicate();
      // if this happend multi time we dont want to replace all of them with one equal object
      if (lastSelectedObject.containsKey(checkKey)) {
        int lastIndex = lastSelectedObject.get(checkKey);
        lastIndex = lastIndex + 1;
        if (lastIndex < actualObjects.size()) {
          current.setObject(actualObjects.get(lastIndex).toString());
          lastSelectedObject.put(checkKey, lastIndex);
        } else {
          current.setObject(actualObjects.get(0).toString());
        }
      } else {
        lastSelectedObject.put(checkKey, 0);
        current.setObject(actualObjects.get(0).toString());
      }
      current.setDoesItChange(true);
    }
    current.setAfterProcessResultIsAcceptable(true);
    return current;
  }

  private String ProvideNewFalseObject(String predicate, String object) {
    if (predicate.toLowerCase().equals("http://dbpedia.org/ontology/birthplace")
        || predicate.toLowerCase().equals("http://dbpedia.org/ontology/foundationplace")
        || predicate.toLowerCase().equals("http://dbpedia.org/ontology/deathplace")) {
      if (object.toLowerCase().equals("http://dbpedia.org/resource/Sevier_County,_Tennessee")) {
        return "http://dbpedia.org/resource/Netherlands";
      } else {
        return "http://dbpedia.org/resource/Sevier_County,_Tennessee";
      }
    }

    if (predicate.toLowerCase().equals("http://dbpedia.org/ontology/team")) {
      if (object.toLowerCase().equals("http://dbpedia.org/resource/Atlanta_Hawks")) {
        return "http://dbpedia.org/resource/Washington_Wizards";
      } else {
        return "http://dbpedia.org/resource/Phoenix_Suns";
      }
    }

    return "Something Else";
  }

  public List<String> DoQuery(String subject, String predicate) {
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

  public ProcessedModelResponse ProcessModelAndSwitchSubjectAndObjectForPredicate(Model model, String predicateForSwitch) {
    System.out.println("Start Processing for updating...");
    System.out.println("it may take a few minutes");

    ProcessedModelResponse responce = new ProcessedModelResponse();

    //queryExecutioner.setServiceRequestURL(sparqlServerEndpoint);
    StmtIterator iterator = model.listStatements();
    while (iterator.hasNext()) {
      Statement statement = iterator.next();
      if (IsIntended(statement)) {
        String key = ExtractKey(statement.getSubject().toString());
        if (!responce.getIntendedStatements().containsKey(key)) {
          // System.out.println("Size of the hash map: " + proccessMap.size());
          responce.Put(key, new RDFProcessEntity());
          // System.out.println("Size of the hash map: " + proccessMap.size());
        }
        // System.out.println("Size of the hash map: " + proccessMap.size());
        RDFProcessEntity currentStatement = responce.Get(key);
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

          if (object.toString().toLowerCase().contains("office")) {
            responce.AddKeyForRemove(key);
          }
        }

        if (predicate.contains("subject")) {
          currentStatement.setSubject(object.toString());
          currentStatement.AddStep();
        }

        if (predicate.contains("type")) {
          currentStatement.setType(object.toString());
          currentStatement.AddStep();
        }

        if (!currentStatement.getIsProcessed() && currentStatement.getReadyForProcess() == 5) {
          currentStatement = ProcessAndSwitch(currentStatement, predicateForSwitch);
          currentStatement.setIsProcessed(true);
        }
        responce.Put(key, currentStatement);
      } else {
        responce.Add(statement);
      }
    }
    System.out.println("Process done");
    return responce;
  }

  private RDFProcessEntity ProcessAndSwitch(RDFProcessEntity currentStatement, String predicate) {
    System.out.println(currentStatement.getPredicate());
    if(currentStatement.getPredicate().contains(predicate)){
      System.out.println("lets switch ");
      System.out.println(currentStatement.getSubject());
      System.out.println(currentStatement.getObject());
      currentStatement.switchObjectAndSubject();
      System.out.println("after switch ");
      System.out.println(currentStatement.getSubject());
      System.out.println(currentStatement.getObject());
      System.out.println("---------- ");
    }
    return currentStatement;
  }
}
