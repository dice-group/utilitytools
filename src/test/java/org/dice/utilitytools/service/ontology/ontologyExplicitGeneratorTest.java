package org.dice.utilitytools.service.ontology;

import org.dice.utilitytools.UtilitytoolsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UtilitytoolsApplication.class)
public class ontologyExplicitGeneratorTest {

    @Autowired
    OntologyExplicitGenerator service;

    @Test
    public void doesItWork() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("allPredicates.txt").getFile());

        FileReader fr=new FileReader(file);   //reads the file
        BufferedReader br=new BufferedReader(fr);
        String line;
        Set<String> ontologies = new HashSet<String>();
        while((line=br.readLine())!=null)
        {
            ontologies.add(line);
            System.out.println(line);
        }

        Map<String, ArrayList<String>> rr =  service.generate(ontologies);

    }
}
