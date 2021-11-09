package org.dice.utilitytools.service.ontology;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.*;

@Component
public class OntologyExplicitGenerator {
    Map<String, ArrayList<String>> mapOfParents;
    private HttpClient client;
    String service = "https://dbpedia.org/sparql?query=";

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
             ArrayList<String> listOpParents = calcParentsFor(item, allVisited);
             mapOfParents.put(item , listOpParents);
        }
        return mapOfParents;
    }

    private ArrayList<String> calcParentsFor(String item, Set<String> visitedParents) {
        // get parent for this item
        Set<String> parents = (Set<String>)getParentClass(item);

        ArrayList<String> newArrayToAddInResult = new ArrayList<>();

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
                for(String superClass:parents){
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
        Set<String> results = new HashSet<>();
        
        
        String result = runQuery(selectBuilder.toString());
        
        
        return results;
    }

    private String runQuery(String query) {
        HttpResponse response = null;
        try {
            HttpGet get = new HttpGet(service  + URLEncoder.encode(query, "UTF-8"));
            //get.addHeader(HttpHeaders.ACCEPT, "application/sparql-results+xml");
            get.addHeader(HttpHeaders.ACCEPT, "application/turtle");
            response = client.execute(get);
            String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return result;
        }
        catch(SocketTimeoutException e) {
            return "";
        }
        catch(ConnectionPoolTimeoutException e) {
            return "";
        }
        catch(Exception e){
            throw new RuntimeException("There is an error while running the query",e);
        }finally {
            // If we received a response, we need to ensure that its entity is consumed correctly to free
            // all resources
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }
}
