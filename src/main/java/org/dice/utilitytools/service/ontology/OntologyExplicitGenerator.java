package org.dice.utilitytools.service.ontology;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

import java.util.*;

//
public class OntologyExplicitGenerator {
    Map<String, ArrayList<String>> mapOfParents;
    private QueryExecutionFactory executioner;

    public OntologyExplicitGenerator(QueryExecutionFactory executioner) {
        this.executioner = executioner;
    }

    // get list of ontology , for each return all parents and super classes
    public Map<String, ArrayList<String>> generate(Collection<String> ontology){
        mapOfParents = new HashMap<>();
        // for each input item
        for(String item : ontology) {
            Set<String> allVisited = new HashSet<>();
            allVisited.add(item);
             ArrayList<String> listOpParents = calcParentsFor(item, allVisited);
             mapOfParents.put(item , listOpParents);
        }
        return mapOfParents;
    }

    private ArrayList<String> calcParentsFor(String item, Set<String> visitedParents) {
        // get parent for this item
        String parentClass = getParentClass(item);

        ArrayList<String> newArrayToAddInResult = new ArrayList<>();

        if(parentClass.equals("")) return newArrayToAddInResult;

        // stop getting stuck in the loop
        if(visitedParents.contains(parentClass)) return newArrayToAddInResult;

        newArrayToAddInResult.add(parentClass);
        //if the parent class exist in the return map just add parent to list of ancestors ans add to map
        if(mapOfParents.containsKey(parentClass)){
            // copy other parent class from map
            for(String superClass:mapOfParents.get(parentClass)){
                newArrayToAddInResult.add(superClass);
            }
        }else {
            // while reach end or find a parent class which we has
            visitedParents.add(parentClass);
            ArrayList<String> parents = calcParentsFor(parentClass, visitedParents);
            for(String superClass:parents){
                newArrayToAddInResult.add(superClass);
            }
        }
        return newArrayToAddInResult;
    }

    private String getParentClass(String item) {
        // run query like this
        // select distinct ?o where {<item> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o } LIMIT 100

        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select distinct ?o where { <");
        selectBuilder.append(item);
        selectBuilder.append("> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o } LIMIT 100");

        try (QueryExecution queryExecution = executioner.createQueryExecution(selectBuilder.toString())) {
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                String parent  = resultSet.next().get("o").asResource().getURI();
                return parent;
            }
        }
        return "";
    }
}
