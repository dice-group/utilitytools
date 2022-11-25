package org.dice.utilitytools.service.transform;

import org.apache.commons.math3.util.Pair;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.ArrayList;

// enter a pathas String then generate a query to select limit number of it
public class QueryTransformer implements ITransform<String,String>{
    int instanceNumber = 1;
    public QueryTransformer(int instanceNumber){
        this.instanceNumber = instanceNumber;
    }
    @Override
    public String Transform(String input, String splitter) throws Exception {
        input = input.replace("[","").replace("]","");
        String[] parts = input.split(splitter);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * where {");

        String first= "?S";
        int pCounter = 0;
        String variableName = "?in";
        String second = variableName + pCounter;

        for(int i = 0 ; i < parts.length ; i++){

            if(i+1 == parts.length){
                second = "?O";
            }

            if(parts[i].charAt(0)=='^'){
                // is inverted
                parts[i] = parts[i].replace("^","");

                queryBuilder.append(second);
                queryBuilder.append(" <");
                queryBuilder.append(parts[i]);
                queryBuilder.append("> ");
                queryBuilder.append(first);

            }else{
                queryBuilder.append(first);
                queryBuilder.append(" <");
                queryBuilder.append(parts[i]);
                queryBuilder.append("> ");
                queryBuilder.append(second);
            }
            queryBuilder.append(" . ");
            pCounter = pCounter+1;
            first = second;
            second = variableName + pCounter;
        }


        queryBuilder.append("} limit "+instanceNumber);
        return queryBuilder.toString();
    }


}
