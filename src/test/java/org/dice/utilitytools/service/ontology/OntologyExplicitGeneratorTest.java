package org.dice.utilitytools.service.ontology;

import javassist.bytecode.stackmap.BasicBlock;
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
public class OntologyExplicitGeneratorTest {

    @Autowired
    OntologyExplicitGenerator service;

    @Test
    public void doesItWork() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("complatedAllDomainAndRanges.txt").getFile());

        FileReader fr=new FileReader(file);   //reads the file
        BufferedReader br=new BufferedReader(fr);
        String line;
        Set<String> ontologies = new HashSet<String>();
        while((line=br.readLine())!=null)
        {
            ontologies.add(line);
            System.out.println(line);
        }

/*        ontologies = service.addAllChildren(ontologies);

try {
    saveFromString(ontologies, "complatedAllDomainAndRanges.txt");
}catch(Exception ex){
    System.out.println(ex);
}*/


        Map<String, ArrayList<String>> rr =  service.generate(ontologies);

        for (Map.Entry<String, ArrayList<String>> entry : rr.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append(",[");
            for(String s:value){
                sb.append(s);
                sb.append(" ");
            }
            sb.append("]");
            System.out.println(sb.toString());
        }

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("map.ser"));
        out.writeObject(rr);
        out.close();

    }

    public static void saveFromString(Collection<String> obj, String path) throws Exception {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            for (String s : obj) {
                pw.println(s);
            }
            pw.flush();
        } finally {
            pw.close();
        }
    }
}
