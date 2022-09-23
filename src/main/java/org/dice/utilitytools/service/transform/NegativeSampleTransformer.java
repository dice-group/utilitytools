package org.dice.utilitytools.service.transform;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.ResultSet;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

// this class can generate negative samples from positive file
@Component
public class NegativeSampleTransformer {

    QueryExecutionFactory qef = new QueryExecutionFactoryHttp("https://synthg-fact.dice-research.org/sparql");

    long heapSizeThreshold = 40000000;
    HashSet<String> setOfAllObjects;
    HashMap<String, HashSet<String>> mapOfTrueFacts;
    HashMap<String, HashSet<String>> mapOfFalseFacts;
    HashMap<String, List<String>> mapOfFalseFactsListVersion;
    // use this map for keep the trace of last selected false object from mapOfFalseFacts
    // the key is the name of the subject and the value is the index of next object to select
    // it should check for out of index error if it is out of index then should set 0
    HashMap<String, Integer> keepCountMap;

    public NegativeSampleTransformer(){
        reset();
    }

    public void reset(){
        setOfAllObjects = new HashSet<>();
        mapOfFalseFacts = new HashMap<>();
        mapOfTrueFacts = new HashMap<>();
        keepCountMap = new HashMap<>();
        mapOfFalseFactsListVersion = new HashMap<>();
    }


    // input is a triple (subject , predicate , object ) which with "separator character" separated
    public List<String> generate(List<String> input, String separator){
        System.out.println("size of the input list for generating the false facts are " + input.size());
        reset();
        for(String line:input){
            String[] parts = line.split(separator);

            String subject = parts[0];
            String object = parts[2];
            // save all possible objects
            setOfAllObjects.add(object);

            // save all exist subject object relations
            HashSet<String> temp;
            if (mapOfTrueFacts.containsKey(subject)){
                temp = mapOfTrueFacts.get(subject);
            }else{
                temp = new HashSet<>();
            }
            temp.add(object);
            mapOfTrueFacts.put(subject,temp);
            showAndFreeHeap("1");
        }

        // calculate all possible false object for each subject
        for(Map.Entry<String, HashSet<String>> entry: mapOfTrueFacts.entrySet()){
            HashSet<String> tempFalseObjects = new HashSet<>();
            String tempSubject = entry.getKey();
            HashSet<String> currentTrueObjects = entry.getValue();

            for(String possibleObject:setOfAllObjects){
                if(!currentTrueObjects.contains(possibleObject)){
                    tempFalseObjects.add(possibleObject);
                }
            }
            if(tempFalseObjects.size() == 0){
                System.out.println("for this subject :"+ tempSubject+ " could not found any false object");
                System.out.println("total objects are "+ setOfAllObjects.size()+" this subject also has "+currentTrueObjects.size()+ " objects");
            }
            showAndFreeHeap("2");
            mapOfFalseFacts.put(tempSubject, tempFalseObjects);
        }

        for (Map.Entry<String, HashSet<String>> entry: mapOfFalseFacts.entrySet()){
            ArrayList temp = (ArrayList<String>)entry.getValue().stream()
                    .collect(Collectors.toList());

            Collections.sort(temp);

            mapOfFalseFactsListVersion.put(entry.getKey(), temp);
            showAndFreeHeap("3");
        }

        List<String> transformedResult = new ArrayList<>();
        // for each line select one false object from map in order
        for(String line:input){
            String[] parts = line.split(separator);

            String subject = parts[0];
            String predicate = parts[1];
            String object = parts[2];

            Integer indexOfFalseObject = 0;
            if(keepCountMap.containsKey(subject)){
                indexOfFalseObject = keepCountMap.get(subject);
            }

            List<String> listOfFalseObjects = mapOfFalseFactsListVersion.get(subject);

            if(indexOfFalseObject >= listOfFalseObjects.size()){
                indexOfFalseObject = 0;
            }

            String newFalseObject = listOfFalseObjects.get(indexOfFalseObject);

            transformedResult.add(subject+separator+predicate+separator+newFalseObject+" .");

            keepCountMap.put(subject, indexOfFalseObject + 1);
            showAndFreeHeap("4");
        }
        return transformedResult;
    }

    public List<String> generateShiftAlgorithm(List<String> input, String separator){
        List<String> returnValue = new ArrayList<>();
        System.out.println("size of the input list for generating the false facts are " + input.size());
        reset();
        for(String line:input){
            String[] parts = line.split(separator);

            String subject = parts[0];
            String predicate = parts[1];
            String object = parts[2];
            // save all possible objects
            setOfAllObjects.add(object);

            // save all exist subject predicate and object relations
            HashSet<String> temp;
            if (mapOfTrueFacts.containsKey(subject+" "+predicate)){
                temp = mapOfTrueFacts.get(subject+" "+predicate);
            }else{
                temp = new HashSet<>();
            }
            temp.add(object);
            mapOfTrueFacts.put(subject+" "+predicate,temp);
            showAndFreeHeap("1");
        }
        for(String line:input) {
            String[] parts = line.split(separator);

            String subject = parts[0];
            String predicate = parts[1];
            String object = parts[2];

            String randomObject = selectRandomObject();
            HashSet<String> trueObjects = mapOfTrueFacts.get(subject+" "+predicate);
            while (trueObjects.contains(randomObject)){
                randomObject = selectRandomObject();
            }
            returnValue.add(subject+separator+predicate+separator+randomObject);
        }
        return returnValue;
    }

    public List<String> runQueryOverDBpedia(List<String> input, String separator,int fromLine, int toLine){
        // run this query if it has result consider as negative triple if not remove the facts
        HashSet<String> resultSet = new HashSet<>();
        System.out.println("size of the input list for generating the false facts are " + input.size());
        reset();
        int progressCount = 0;
        for(String line:input){
            if(progressCount<fromLine || toLine<progressCount){
                progressCount = progressCount + 1;
                continue;
            }
            progressCount = progressCount + 1;
            System.out.println("progress :"+progressCount +"from" +input.size() + "%");


            String[] parts = line.split(separator);

            String subject = parts[0];
            String predicate = parts[1];
            String object = parts[2];
            String range = whatIsTheRange(predicate);

            if(range.equals("")){
                System.out.println("for this predicate the range in null!  :"+predicate);
                continue;
            }

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select ?o where \n");
            queryBuilder.append("{");
            queryBuilder.append("<"+subject+ "> ?p ?o .");
            queryBuilder.append("?o a <"+range+"> .");
            queryBuilder.append("FILTER (?p != <"+predicate+">)");
            queryBuilder.append("FILTER NOT EXISTS { <"+subject+"> <"+predicate+"> ?o }");
            queryBuilder.append("}");

            ResultSet rs = qef.createQueryExecution(queryBuilder.toString()).execSelect();
            // without a domain, there can be no violation, so we just need to check the case in which a domain exists
            if(rs.hasNext()) {
                String newObject = rs.next().get("?o").toString();
                if(theTripleIsWrong(subject,predicate,newObject)){
                    String newTriple = subject+separator+predicate+separator+newObject;
                    System.out.println(newTriple);
                    resultSet.add(newTriple);
                }
            }
        }
        List<String> returnList = new ArrayList<>(resultSet);
        return returnList;
    }

    private boolean theTripleIsWrong(String subject, String predicate, String newObject) {
        String queryDom = "SELECT * WHERE { <"+subject+"> <"+predicate+"> <"+newObject+"> }";
        ResultSet rs = qef.createQueryExecution(queryDom).execSelect();
        // without a domain, there can be no violation, so we just need to check the case in which a domain exists
        boolean isWrong = true;
        if(rs.hasNext()) {
            isWrong = false;
        }
        return isWrong;
    }

    private String whatIsTheRange(String predicate) {
        String queryDom = "SELECT * WHERE { <"+predicate+"> rdfs:domain ?dom }";
        ResultSet rs = qef.createQueryExecution(queryDom).execSelect();
        // without a domain, there can be no violation, so we just need to check the case in which a domain exists
        String domain = "";
        if(rs.hasNext()) {
             domain = rs.next().get("dom").toString();
        }
        return domain;
    }

    private String selectRandomObject() {


        int size = setOfAllObjects.size();
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        for(String obj : setOfAllObjects)
        {
            if (i == item)
                return obj;
            i++;
        }
        return "error";
    }

    private void showAndFreeHeap(String labelOfWhereIsfunctionCalled){

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        if(heapFreeSize<heapSizeThreshold) {

            // Get current size of heap in bytes
            long heapSize = Runtime.getRuntime().totalMemory();

            System.out.println(labelOfWhereIsfunctionCalled + " heapSize: " + heapSize);


            // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
            long heapMaxSize = Runtime.getRuntime().maxMemory();
            System.out.println(labelOfWhereIsfunctionCalled + " heapMaxSize: " + heapMaxSize);


            System.out.println(labelOfWhereIsfunctionCalled + " heapFreeSize: " + heapFreeSize);


            System.out.println("------------------------------");

            System.gc();
            System.out.println("free");
        }
    }
}
