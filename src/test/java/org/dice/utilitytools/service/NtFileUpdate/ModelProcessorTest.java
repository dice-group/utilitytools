package org.dice.utilitytools.service.NtFileUpdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
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
}
