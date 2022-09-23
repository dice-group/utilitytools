package org.dice.utilitytools.service.NtFileUpdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ModelProcessorTest {

  @Autowired ModelProcessor processor;

  @Test
  public void IsIntendedShouldResponceCorrectWithFalse() {
    Resource resourceSubject =
        ResourceFactory.createResource("http://dbpedia.org/resource/Stanley_Kubrick");
    Property property =
        ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
    Resource resourceObject = ResourceFactory.createResource("StanleyKubrick");

    Statement statement = new StatementImpl(resourceSubject, property, resourceObject);

    boolean actual = processor.IsIntended(statement);

    boolean expected = false;

    assertEquals(expected, actual);
  }

  @Test
  public void IsIntendedShouldResponceCorrectWithTrue() {
    Resource resourceSubject =
        ResourceFactory.createResource("http://swc2017.aksw.org/task2/dataset/3823854");
    Property property =
        ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    Resource resourceObject =
        ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

    Statement statement = new StatementImpl(resourceSubject, property, resourceObject);

    boolean actual = processor.IsIntended(statement);

    boolean expected = true;

    assertEquals(expected, actual);
  }

  @Test
  public void ExtractKeyShouldWorkFine() {
    String input = "http://swc2017.aksw.org/task2/dataset/3823854";
    String actual = processor.ExtractKey(input);
    assertEquals(actual, "3823854");
  }


  @Test
  public void shouldResponceCorrectForfalsePeredicateWhichConsideredAsFalseFact(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/ontology/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Marrakesh");
    pe.setHasTruthValue(false);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
  }

  @Test
  public void shouldResponceCorrectForTruePeredicateWhichConsideredAsTrueFact(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/property/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Marrakesh");
    pe.setHasTruthValue(true);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
  }

  @Test
  public void shouldResponceCorrectForTruePeredicateWhichConsideredAsTrueFactWithFalceObject(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/property/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Tehran");
    pe.setHasTruthValue(true);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
    assertEquals(actual.getDoesItChange(),true);
    assertNotEquals(actual.getObject(),"http://dbpedia.org/resource/Tehran");
  }

  @Test
  public void shouldResponceCorrectForTruePeredicateWhichConsideredAsFalseFactWithFalceObject(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/property/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Tehran");
    pe.setHasTruthValue(false);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
    assertEquals(actual.getDoesItChange(),false);
    assertEquals(actual.getObject(),"http://dbpedia.org/resource/Tehran");
  }

  @Test
  public void shouldResponceCorrectForTruePeredicateWhichConsideredAsFalseFactWithTrueObject(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/property/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Marrakesh");
    pe.setHasTruthValue(false);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
    assertEquals(actual.getDoesItChange(),true);
    assertNotEquals(actual.getObject(),"http://dbpedia.org/resource/Marrakesh");
  }

  @Test
  public void shouldTryGetCorrectPredicate(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Claude_Auchinleck");
    pe.setPredicate("http://dbpedia.org/ontology/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Marrakesh");
    pe.setHasTruthValue(true);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),true);
    assertEquals(actual.getDoesThePredicateChange(),true);
  }

  @Test
  public void shouldTryGetCorrectPredicateandActCorrectIfTheTripleIsFalseButConsideredAsTrue(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Barack_Obama");
    pe.setPredicate("http://dbpedia.org/ontology/deathPlace");
    pe.setObject("http://dbpedia.org/resource/Marrakesh");
    pe.setHasTruthValue(true);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),false);
    assertEquals(actual.getDoesThePredicateChange(),true);
  }

  @Test
  public void shouldTryGetCorrectPredicateandActCorrectIfTheTripleIsFalseButConsideredAsTrueadadwda(){
    RDFProcessEntity pe = new RDFProcessEntity();
    pe.setSubject("http://dbpedia.org/resource/Willie_Cauley-Stein");
    pe.setPredicate("http://dbpedia.org/ontology/team");
    pe.setObject("http://dbpedia.org/resource/Sacramento_Kings");
    pe.setHasTruthValue(true);

    RDFProcessEntity actual = processor.ProcessAndUpdate(pe);

    assertEquals(actual.getAfterProcessResultIsAcceptable(),false);
    assertEquals(actual.getDoesThePredicateChange(),true);
  }

}
