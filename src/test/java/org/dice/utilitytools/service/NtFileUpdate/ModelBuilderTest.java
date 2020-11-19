package org.dice.utilitytools.service.NtFileUpdate;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelBuilderTest {

  @Autowired ModelBuilder builder;

  @Test
  public void ModelBuilderShouldBuildModelFromValidFile() throws FileNotFoundException {
    builder.build("/src/test/java/org/dice/utilitytools/service/NtFileUpdate/test.nt");
    Model model = builder.getModel();
    StmtIterator iterator = model.listStatements();
    assertTrue(iterator.hasNext());

    Statement statement = iterator.next();

    assertEquals(
        "http://swc2017.aksw.org/task2/dataset/3823854", statement.getSubject().toString());
  }
}
