package org.dice.utilitytools.service.ontology;

import org.apache.http.impl.client.HttpClientBuilder;


import org.apache.http.client.HttpClient;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OntologyExplicitGenerator {
    Map<String, ArrayList<String>> mapOfParents;
    private HttpClient client;
    String service = "https://dbpedia.org/sparql";

    public OntologyExplicitGenerator() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        client = builder.build();
    }

    // get list of ontology , for each return all parents and super classes
    public Map<String, ArrayList<String>> generate(Collection<String> ontology){
        mapOfParents = new HashMap<>();
        // for each input item
        for(String item : ontology) {
            Set<String> allVisited = new HashSet<>();
            allVisited.add(item);
             ArrayList<String> listOfParents = calcParentsFor(item, allVisited);
             mapOfParents.put(item , listOfParents);

             for(int i = 0 ; i < listOfParents.size()-1 ; i++){
                 ArrayList<String> tempListOfParents = new ArrayList<String>(listOfParents.subList(i+1, listOfParents.size()));
                 mapOfParents.put(listOfParents.get(i) , tempListOfParents);
             }
             if(listOfParents.size()>0) {
                 mapOfParents.put(listOfParents.get(listOfParents.size() - 1), new ArrayList<>());
             }

        }
        return mapOfParents;
    }

    private ArrayList<String> calcParentsFor(String item, Set<String> visitedParents) {
        ArrayList<String> newArrayToAddInResult = new ArrayList<>();

        // get parent for this item
        Set<String> parents = (Set<String>)getParentClass(item);

        if(parents.size() == 0) return newArrayToAddInResult;

        // stop getting stuck in the loop
        //if(visitedParents.contains(parentClass)) return newArrayToAddInResult;

        for(String parent : parents){
            newArrayToAddInResult.add(parent);
            //if the parent class exist in the return map just add parent to list of ancestors ans add to map
            if(mapOfParents.containsKey(parent)){
                // copy other parent class from map
                for(String superClass:mapOfParents.get(parent)){
                    newArrayToAddInResult.add(superClass);
                }
            }else {
                // while reach end or find a parent class which we has
                visitedParents.add(parent);
                ArrayList<String> tempParents = calcParentsFor(parent, visitedParents);
                for(String superClass:tempParents){
                    newArrayToAddInResult.add(superClass);
                }
                visitedParents.remove(parent);
            }
        }

        return newArrayToAddInResult;
    }

    private Collection<String> getParentClass(String item) {
        // run query like this
        // select distinct ?o where {<item> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o } LIMIT 100

        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select distinct ?o where { <");
        selectBuilder.append(item);
        selectBuilder.append("> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o } LIMIT 100");
        Collection<String> results = runQuery(selectBuilder.toString());
        return results;
    }

    private Collection<String> runQuery(String query) {
        System.out.println(query);
        Set<String> results = new HashSet<>();
        //QueryExecutioner queryExecutioner = new QueryExecutioner(service);
        QueryExecutioner queryExecutioner = new QueryExecutioner();
        try (QueryExecution queryExecution = queryExecutioner.getQueryExecution(query)) {
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                String str = qs.get("?o").asNode().getURI();
                results.add(str);
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return results;
    }

    // get set of ontologies , for each item add all children to the set
    public Set<String> addAllChildren(Set<String> ontologies) {

        Set<String> tempOntologies = new HashSet<>();

        for (Iterator<String> iterator = ontologies.iterator(); iterator.hasNext();) {
            String ontology = iterator.next();
            tempOntologies = addAllChildren(ontology, tempOntologies);
        }

        for (Iterator<String> iterator = ontologies.iterator(); iterator.hasNext();) {
            tempOntologies.add(iterator.next());
        }

        return tempOntologies;
    }

    private Set<String> addAllChildren(String item, Set<String> ontologies) {
        Set<String> children = whatAreTheChildrenFor(item);
        if(children.size()==0) return ontologies;
        for(String child : children){
            ontologies.add(child);
            ontologies = addAllChildren(child , ontologies);
        }
        return ontologies;
    }

    private Set<String> whatAreTheChildrenFor(String item) {
        // run query like this
        // select distinct ?s where {?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> <item> } LIMIT 100

        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select distinct ?o where { ?o ");
        selectBuilder.append(" <http://www.w3.org/2000/01/rdf-schema#subClassOf> <");
        selectBuilder.append(item);
        selectBuilder.append("> } LIMIT 100");
        Set<String> results = (Set<String>) runQuery(selectBuilder.toString());
        return results;
    }
}
