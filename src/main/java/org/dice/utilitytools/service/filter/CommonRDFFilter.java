package org.dice.utilitytools.service.filter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// this class filter the "after file" based on "before file" and filter the rdfs which their subject and object are not exist on "before file"
@Component
public class CommonRDFFilter {

    public List<String>  filterNotCommonSubjectAndaObject(List<String> before , List<String> after, String separatorCharacter)
    {
        List<String> filteredResult = new ArrayList<>();

        System.out.println("before size is"+before.size());
        System.out.println("after size is"+after.size());

        HashSet<String> subjectSet = new HashSet<>();
        HashSet<String> objectSet = new HashSet<>();
        HashSet<String> tripleSet = new HashSet<>();

        //make hashset of before objects and subjects and exist triple
        for(String s:before){
            String[] parts = s.split(separatorCharacter);
            subjectSet.add(parts[0]);
            objectSet.add(parts[2]);
            tripleSet.add(parts[0]+parts[2]);
        }


        for(String s:after){
            String[] parts = s.split(separatorCharacter);
            if(subjectSet.contains(parts[0])){
                if(objectSet.contains(parts[2])){
                    // while subject and object should exist at before file
                    // the triple should not exist
                    if(!tripleSet.contains(parts[0]+parts[2])){
                        filteredResult.add(s);
                    }
                }
            }
        }

        System.out.println("filtered size is"+filteredResult.size());
        return filteredResult;
    }
}
