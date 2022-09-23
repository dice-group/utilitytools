package org.dice.utilitytools.service.transform;

import org.apache.jena.rdf.model.*;
import org.dice.utilitytools.Model.RDFProcessEntity;
import org.dice.utilitytools.Model.SimpleRDF;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TTLtoSimpleRDFTransform {
    public List<SimpleRDF> transform(Model input, String splitter) {
        StmtIterator iter = input.listStatements();
        List<SimpleRDF> returnVal = new ArrayList<>();
        try {
            HashMap<String, RDFProcessEntity> map = new HashMap<String, RDFProcessEntity> ();
            while ( iter.hasNext() ) {
                Statement stmt = iter.next();

                String key = stmt.getSubject().toString();

                if (!map.containsKey(key)) {
                    map.put(key, new RDFProcessEntity());
                }

                RDFProcessEntity currentStatement = map.get(key);

                Resource predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();

                if (predicate.getURI().contains("subject")) {
                    currentStatement.setSubject(yago2DBpediaObjectSubject(object.toString()));
                    currentStatement.AddStep();
                }

                if (predicate.getURI().contains("object")) {
                    currentStatement.setObject(yago2DBpediaObjectSubject(object.toString()));
                    currentStatement.AddStep();
                }

                if (predicate.getURI().contains("predicat")) {
                    String convertedPredicate = yago2DBpediaPredicate(object.toString());
                    if(convertedPredicate.equals("")){
                        System.out.println("Error : can not convert this predicate to dbpedia " + object.toString());
                        currentStatement.setAfterProcessResultIsAcceptable(false);
                    }else{
                        currentStatement.setAfterProcessResultIsAcceptable(true);
                    }
                    currentStatement.setPredicate(convertedPredicate);
                    currentStatement.AddStep();
                }
            }

            for (Map.Entry<String, RDFProcessEntity> value : map.entrySet()) {
                if(value.getValue().getAfterProcessResultIsAcceptable()) {
                    returnVal.add(new SimpleRDF(value.getKey(), value.getValue().getSubject(), value.getValue().getPredicate(), value.getValue().getObject()));
                }
            }
        } finally {
            if ( iter != null ) iter.close();
        }
        return returnVal;
    }

    private String yago2DBpediaPredicate(String toString) {
        String[] parts = toString.split("/");
        String predicate  =  parts[parts.length-1];
        switch (predicate.toLowerCase()){
            case "actedin":
                return "http://dbpedia.org/ontology/starring";
            case "created":
                return "http://dbpedia.org/ontology/writer";
            case "diedin":
                return "http://dbpedia.org/ontology/deathPlace";
            case "directed":
                return "http://dbpedia.org/ontology/director";
            case "hasacademicadvisor":
                return "http://dbpedia.org/ontology/doctoralAdvisor";
            case "haschild":
                return "http://dbpedia.org/ontology/child";
            case "hasofficiallanguage":
                return "http://dbpedia.org/ontology/officialLanguage";
            case "iscitizenof":
                return "http://dbpedia.org/ontology/nationality";
            case "isknownfor":
                return "http://dbpedia.org/ontology/knownFor";
            case "isleaderof":
                return "http://dbpedia.org/ontology/governor";
            case "islocatedin":
                return "http://dbpedia.org/ontology/location";
            case "ismarriedto":
                return "http://dbpedia.org/ontology/spouse";
            case "livesin":
                return "http://dbpedia.org/ontology/residence";
            case "produced":
                return "http://dbpedia.org/ontology/producer";
            case "wasbornin":
                return "http://dbpedia.org/ontology/birthPlace";
            case "worksat":
                return "http://dbpedia.org/property/workInstitutions";
        }
        return "";
    }

    private String yago2DBpediaObjectSubject(String toString) {
        String dbpediaResource = "http://dbpedia.org/resource/";
        String[] parts = toString.split("/");
        return  dbpediaResource+parts[parts.length-1];

    }
}

