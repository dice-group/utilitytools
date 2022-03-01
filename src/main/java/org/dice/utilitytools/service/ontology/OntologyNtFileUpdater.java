package org.dice.utilitytools.service.ontology;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OntologyNtFileUpdater {
    public void update(String ntFile, String mapFile, String saveFileHere) throws IOException, ClassNotFoundException {


        Map<String, ArrayList<String>> map;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(mapFile));
        map = (Map<String, ArrayList<String>>) in.readObject();
        in.close();

        FileWriter write = new FileWriter("map.report");
        for(Map.Entry<String, ArrayList<String>> entry : map.entrySet()){
            StringBuilder items = new StringBuilder();
            for(int i = 0 ; i < entry.getValue().size() ; i++){
                items.append(entry.getValue().get(i));
                if(i+1<entry.getValue().size())
                    items.append(",");
            }

            write.write(entry.getKey()+":["+items.toString()+"]\n");
        }
        write.close();


        long lineCounter = 0;
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(new FileInputStream(ntFile)));
             FileWriter fw = new FileWriter(saveFileHere)) {
            String st;
            // https://www.utf8-chartable.de/
            while ((st = br.readLine()) != null) {
                lineCounter = lineCounter+1;
                System.out.println(lineCounter);
                if(isType(st)){
                    String ontology = getOntology(st);
                    String firstPart =extractSbjectAndPredicate(st);

                    System.out.println("start look in map with size "+ map.size() + " for " + ontology);

                    if(map.containsKey(ontology)){
                        ArrayList<String> allParents = map.get(ontology);

                        System.out.println("it has "+allParents.size()+" parents.");

                        for(String s : allParents){
                            fw.write(firstPart+s+"> .");
                        }
                    }else{
                        System.out.println("there is nothing");
                    }
                }
                fw.write(st);
                fw.write("\n");
            }
            fw.close();
            System.out.println("Raw file Preprocessed...");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String extractSbjectAndPredicate(String st) {
        String[] t = st.split(" ");
        String firstPart = t[0]+" "+t[1]+" <";
        System.out.println("first Part is " + firstPart);
        return firstPart;
    }

    //<http://dbpedia.org/resource/(16960)_1998_QS52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Planet> .
    private static String getOntology(String st) {
        String[] t = st.split(" ");
            String ontology = t[2].replace("<","").replace(">","");
            System.out.println("ontology is " + ontology);
            return ontology;
    }

    private static boolean isType(String st) {
        String[] t = st.split(" ");
        if(t.length==4){
            if(t[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")){
                System.out.println("yessss");
                return true;
            }
        }
        return false;
    }
}
