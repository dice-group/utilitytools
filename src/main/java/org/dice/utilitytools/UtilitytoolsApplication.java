package org.dice.utilitytools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice.utilitytools.service.NtFileUpdate.NtFileUpdater;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.dice.utilitytools.service.filter.CommonRDFFilter;
import org.dice.utilitytools.service.load.IOService;
import org.dice.utilitytools.service.ontology.OntologyNtFileUpdater;
import org.dice.utilitytools.service.spliter.BasedDateSpliter;
import org.dice.utilitytools.service.transform.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UtilitytoolsApplication implements CommandLineRunner {

  @Autowired
  NtFileUpdater service;

  @Autowired
  OntologyNtFileUpdater ontologyNtFileUpdaterService;

  @Autowired
  BasedDateSpliter spliterService;

  @Autowired
  IOService ioService;

  @Autowired
  Transformer transformer;

  @Autowired
  CommonRDFFilter commonRDFFilter;

  @Autowired
  NegativeSampleTransformer negativeSampleTransformer;

  @Autowired
  RDFModelTransform rdfModelTransformer;

  @Autowired
  TTLtoSimpleRDFTransform tTLtoSimpleRDFTransform;

  public static void main(String[] args) {
    SpringApplication.run(UtilitytoolsApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {

    if (args == null || args.length == 0) {
      System.out.println("no arguments ! use h to get help");
      return;
    }

    if (args.length ==  1 && args[0].equals("h")){
      System.out.println("this is help");
      System.out.println("1 . use 'up' for update the file ");
      System.out.println("\t \t up [file for update] [verbalizedFileForReplace ] [t it is training file, nt it is ]");

      System.out.println("2 . use 'euf' for explicit update file");
      System.out.println("\t \t euf [ntFile] [mapFile] [path for save result ]");

      System.out.println("3 . use 'split' for split the file based on the date (main usage ontotext dataset csv or tsv files with last column as date column)");
      System.out.println("\t \t split [the file] [c for comma t for tab separated file] [path for save result(just directory path)]");

      System.out.println("4 . use 'gntwsp' for Generate NT file With Specific Predicate (out put of the 3. functionality)");
      System.out.println("\t \t gntwsp [the file] [c for comma t for tab separated file] [the predicate to add] [path for save result (full path with name)]");

      System.out.println("5 . use 'cdtf' accept two .nt file , before and after , save the triples on the after file which both their subject and objects are exist in the before file in the result file");
      System.out.println("\t \t cdtf [beforeFile] [afterFile] [c for comma t for tab separated file s for space] [path for save result with file name]");

      System.out.println("6 . use 'gff' generate the false fact from true facts file ");
      System.out.println("\t \t gff [trueFile] [c for comma t for tab separated file s for space] [path for save result with file name]");

      System.out.println("7 . use 'gffRDF' generate the false fact from true facts file , input file is a model like turtle or ...");
      System.out.println("\t \t gffRDF [trueFile] [c for comma t for tab separated file s for space] [path for save result with file name]");

      System.out.println("8 . use 'cso' to change the subject and an object for a predicate");
      System.out.println("\t \t cso [File] [predicate]");

      System.out.println("9 . use 'qip' to query and get instance of each given path");
      System.out.println("\t \t qip [pathFiles] [endpoint] [number of instances] [location for save the results]");

      System.out.println("10. use 'gffGF' generate the false fact from ground truth file ");
      System.out.println("\t \t gffGF [trueFile] [groundTruthFile] [c for comma t for tab separated file s for space] [path for save result with file name]");



      return ;
    }

    System.out.println("args len is  " + args.length);

    for(String a:args){
      System.out.println(a);
    }


    // update file
    // functionality 1
    if(args[0].equals("up") ){
      //TODO : rearrange the args
      File f = new File(args[1]);
      if (!f.exists()) {
        System.out.println("no file exist");
        return;
      }

      if (!f.isFile()) {
        System.out.println(args[1] + " is not a file");
        return;
      }

      if (!f.canRead()) {
        System.out.println(args[1] + " is not readable");
        return;
      }

        boolean isTraining = false;
        if (args[3].toLowerCase().equals("t")) {
            System.out.println("it is a training file");
          isTraining = true;
        }
        service.update(args[1], args[2], isTraining);
    }

    // functionality 2
    if(args.length == 4 && args[0].equals("euf")){
        String ntFile = args[1];
        String mapFile = args[2];
        String saveFileHere = args[3];
        try {
          ontologyNtFileUpdaterService.update(ntFile, mapFile, saveFileHere);
        }catch (Exception ex){
          System.out.println(ex.getMessage());
        }
    }

    // functionality 3
    if (args.length == 4 && args[0].equals("split")){
        System.out.println("split file ");
        if(args[2].equals("t")){
          spliterService.split(args[1],"2010-01-01","\t");
        }else{
          spliterService.split(args[1],"2010-01-01",",");
        }

        List<String> bef = spliterService.getBeforeList();
        List<String> aft = spliterService.getAfterList();

        if(args[3].charAt(args[3].length()-1) != '/'){
          args[3] = args[3] + "/";
        }

        ioService.writeListAsFile(bef ,args[3]+"before.out",false);
        ioService.writeListAsFile(aft ,args[3]+"after.out",false);
    }

    // functionality 4
    if (args.length == 5 && args[0].equals("gntwsp")){
        System.out.println("generating the nt file with specific predicate: "+args[2]);

        File file = ioService.readFile(args[1]);

        String separetor;
        if(args[2].equals("t")){
          separetor = "\t";
        }else{
          separetor = ",";
        }

        List<String> converted = transformer.transformAndAddPredicate(file, separetor, args[3]);

        ioService.writeListAsFile(converted ,args[4], false);
    }


    // functionality 5
      if (args.length == 5 && args[0].equals("cdtf")){
        String beforeFile = args[1];
        String afterFile = args[2];

        String separetor;
          if(args[3].equals("t")){
              separetor = "\t";
          }else{
              if(args[3].equals("c")){
                  separetor = ",";
              }else
              {
                  separetor = " ";
              }
          }

        String savePath = args[4];
        List<String> theFilteredResult =commonRDFFilter.filterNotCommonSubjectAndaObject(
                transformer.transformToList(ioService.readFile(beforeFile)),
                transformer.transformToList(ioService.readFile(afterFile)),
                separetor);

        ioService.writeListAsFile(theFilteredResult, savePath,false);
      }

      // functionality 6
      if (args.length == 4 && args[0].equals("gff")){
          String trueFile = args[1];

          System.out.println("start generating false facts");
          String separetor;
          if(args[2].equals("t")){
              separetor = "\t";
          }else{
              if(args[3].equals("c")){
                  separetor = ",";
              }else
              {
                  separetor = " ";
              }
          }

          String savePath = args[3];

          List<String> theGeneratedFalseFacts =negativeSampleTransformer.generate(
                  transformer.transformToList(ioService.readFile(trueFile)),
                  separetor);
          System.out.println(theGeneratedFalseFacts.size() + "false facts are generated will be save " + savePath);


          ioService.writeListAsFile(theGeneratedFalseFacts, savePath, false);
      }

      // functionality 7
      if (args.length == 4 && args[0].equals("gffRDF")){
          String trueFile = args[1];

          System.out.println("start generating false facts");
          String separetor;
          if(args[2].equals("t")){
              separetor = "\t";
          }else{
              if(args[2].equals("c")){
                  separetor = ",";
              }else
              {
                  separetor = " ";
              }
          }

          String savePath = args[3];

          List<String> theGeneratedFalseFacts =negativeSampleTransformer.runQueryOverDBpedia(
                  tTLtoSimpleRDFTransform.transform(rdfModelTransformer.transform(ioService.readFile(trueFile),""),"").stream().map(t -> t.toString()).collect(Collectors.toList()), " ",0,10000);

          System.out.println(theGeneratedFalseFacts.size() + "false facts are generated will be save " + savePath);


          ioService.writeListAsFile(theGeneratedFalseFacts, savePath, true);
      }

      //F functionality 8 cso
      // change subject and object for predicate

      if (args.length == 3 && args[0].equals("cso")){
          String filePath = args[1];

          File f = new File(filePath);
          if (!f.exists()) {
              System.out.println("no file exist");
              return;
          }

          if (!f.isFile()) {
              System.out.println(filePath + " is not a file");
              return;
          }

          if (!f.canRead()) {
              System.out.println(filePath + " is not readable");
              return;
          }

          String predicate = args[2];

          service.switchSubjectAndObjectForAPredicate(filePath, predicate);
      }

      //Functionality  'qip' to query and get instance of each given path

      if(args.length == 5 && args[0].equals("qip")){
          String pathsFile = args[1];
          File f = new File(pathsFile);
          if (!f.exists()) {
              System.out.println("no file exist");
              return;
          }

          if (!f.isFile()) {
              System.out.println(pathsFile + " is not a file");
              return;
          }

          if (!f.canRead()) {
              System.out.println(pathsFile + " is not readable");
              return;
          }

          SplitSimpleTransformer simpleTransformer = new SplitSimpleTransformer();
          // the output has extra [
          List<String> nonTrimmedPaths = simpleTransformer.Transform(f,", ");

          String endpoint = args[2];
          String numberOfInstances = args[3];
          String locationForSaveTheResult = args[4];

          QueryTransformer qT = new QueryTransformer(Integer.parseInt(numberOfInstances));

          List<String> listOfQueries = nonTrimmedPaths.stream().map(p -> {
              try {
                  return qT.Transform(p,",");
              } catch (Exception e) {
                  throw new RuntimeException(e);
              }
          }).collect(Collectors.toList());

          StringBuilder sbForSaveResult = new StringBuilder();
          for (String query:listOfQueries) {
              QueryExecutioner queryExecutioner = new QueryExecutioner(endpoint);
              try (QueryExecution queryExecution = queryExecutioner.getQueryExecution(query)) {
                  ResultSet resultSet = queryExecution.execSelect();
                  while (resultSet.hasNext()) {
                      QuerySolution qs = resultSet.next();
                      String str = qs.toString();
                      sbForSaveResult.append(query);
                      sbForSaveResult.append("\n");
                      sbForSaveResult.append(str);
                      sbForSaveResult.append("\n----------\n");
                  }
              }catch (Exception ex){
                  System.out.println(ex.getMessage());
              }
          }

          File propDir = new File(locationForSaveTheResult);
          try (BufferedWriter bw = new BufferedWriter(new FileWriter(propDir));) {
              bw.write(sbForSaveResult.toString());
          } catch (IOException ex) {
              System.out.println(ex.getMessage());
          }

      }

      //

      //gffGF [trueFile] [groundTruthFile] [c for comma t for tab separated file s for space] [path for save result with file name]
      // functionality 10
      if (args.length == 5 && args[0].equals("gffGF")){
          String trueFile = args[1];
          String groundTruthFile = args[2];

          System.out.println("start generating false facts");
          String separetor;
          if(args[3].equals("t")){
              separetor = "\t";
          }else{
              if(args[3].equals("c")){
                  separetor = ",";
              }else
              {
                  separetor = " ";
              }
          }

          String savePath = args[4];

          List<String> groundTruthFileTriples = new ArrayList<>();
          File groundTruthTripleFile = ioService.readFile(groundTruthFile);
          Scanner scanner = new Scanner(groundTruthTripleFile);
          String line = "";
          while (scanner.hasNextLine()) {
              line = scanner.nextLine();
              groundTruthFileTriples.add(line);
          }

          negativeSampleTransformer.setGroundTruth(groundTruthFileTriples,"http://rdf.frockg.eu/frockg/ontology/hasAdverseReaction",separetor);

          List<String> trueTriples = new ArrayList<>();
          File trueTriplesFile = ioService.readFile(trueFile);
          scanner = new Scanner(trueTriplesFile);

          while (scanner.hasNextLine()) {
              line = scanner.nextLine();
              trueTriples.add(line);
          }
          List<String> theGeneratedFalseFacts =negativeSampleTransformer.runQueryGroundTruthFile(trueTriples,separetor).stream().map(t -> t.toString()).collect(Collectors.toList());



          System.out.println(theGeneratedFalseFacts.size() + "false facts are generated will be save " + savePath);


          ioService.writeListAsFile(theGeneratedFalseFacts, savePath, true);
      }

      System.out.println("Finish");
  }
}
