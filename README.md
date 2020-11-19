# A place for a tiny useful tools

the first one is "NtFileUpdater" this small get an nt file (N-Tripel) like below
```
<http://swc2017.aksw.org/task2/dataset/3252390><http://www.w3.org/1999/02/22-rdf-syntax-ns#type><http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement>.
<http://swc2017.aksw.org/task2/dataset/3252390><http://www.w3.org/1999/02/22-rdf-syntax-ns#subject><http://dbpedia.org/resource/Trevor_Ariza>.
<http://swc2017.aksw.org/task2/dataset/3252390><http://www.w3.org/1999/02/22-rdf-syntax-ns#object><http://dbpedia.org/resource/Sacramento_Kings>.
<http://swc2017.aksw.org/task2/dataset/3252390><http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate><http://dbpedia.org/ontology/team>.
<http://swc2017.aksw.org/task2/dataset/3252390><http://swc2017.aksw.org/hasTruthValue>"1.0"^^<http://www.w3.org/2001/XMLSchema#double>.
```

use DBpedia endpoint to validate the triples and, if need update the object with a new value
then generate the report of change like 
```
3252390 http://dbpedia.org/resource/Trevor_Ariza http://dbpedia.org/ontology/team http://dbpedia.org/resource/Houston_Rockets it was http://dbpedia.org/resource/Sacramento_Kings
```
and also generate verbalized sentence like 
```
3252390 Trevor Ariza's team is Houston Rockets.
```

## for run 
for run this application first build the package with this comment
```
 mvn clean package
```

then run with this comment

```
java -jar target/utilitytools-0.0.1-SNAPSHOT.jar FileName
```
