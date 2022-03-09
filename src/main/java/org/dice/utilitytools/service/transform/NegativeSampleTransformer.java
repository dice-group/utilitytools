package org.dice.utilitytools.service.transform;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

// this class can generate negative samples from positive file
@Component
public class NegativeSampleTransformer {

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

            mapOfFalseFacts.put(tempSubject, tempFalseObjects);
        }

        for (Map.Entry<String, HashSet<String>> entry: mapOfFalseFacts.entrySet()){
            ArrayList temp = (ArrayList<String>)entry.getValue().stream()
                    .collect(Collectors.toList());

            Collections.sort(temp);

            mapOfFalseFactsListVersion.put(entry.getKey(), temp);
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
        }
        return transformedResult;
    }
}
