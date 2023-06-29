package org.dice.utilitytools;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.dice.utilitytools.mapper.TranslatedResult2ElasticMapper;
import org.dice.utilitytools.service.FolderCrawler;
import org.dice.utilitytools.service.NtFileUpdate.NtFileUpdater;
import org.dice.utilitytools.service.Query.QueryExecutioner;
import org.dice.utilitytools.service.handler.APICaller;
import org.dice.utilitytools.service.handler.CorefrenceResulotionGenerator;
import org.dice.utilitytools.service.handler.ITaskHandler;
import org.dice.utilitytools.service.handler.Translator;
import org.dice.utilitytools.service.filter.CommonRDFFilter;
import org.dice.utilitytools.service.load.IOService;
import org.dice.utilitytools.service.ontology.OntologyNtFileUpdater;
import org.dice.utilitytools.service.spliter.BasedDateSpliter;
import org.dice.utilitytools.service.transform.*;
import org.json.JSONArray;
import org.json.JSONObject;
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

  DBOPreprocessor dboPreprocessor = new DBOPreprocessor();

    CSVProcessor csvProcessor = new CSVProcessor();

  public static void main(String[] args) {
    SpringApplication.run(UtilitytoolsApplication.class, args);
  }

    boolean isDirectoryExist(String path){
        File directory = new File(path);
        if (!directory.exists()) {
            System.out.println(path+" no folder exist");
            return false;
        }

        if (!directory.isDirectory()) {
            System.out.println(path + " is not a directory");
            return false;
        }
        return true;
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

      System.out.println("11. use 'dbp' Preprocessor the db to prune the triples");
      System.out.println("\t \t dbp [input folder which contains bz2 files] [folder for save the results]");

      System.out.println("12. use 'reLi' remove literal");
      System.out.println("\t \t reLI [input file] [output file]");

      System.out.println("13. use 'reLiFOLDER' remove literal folder");
      System.out.println("\t \t reLiFOLDER [input file] [outputPathWithSlash]");

      System.out.println("14. use 'fillTemplate' for insert data in a csv file with template");
      System.out.println("\t \t fillTemplate [input file] [template file] [output file] ");

      System.out.println("15. use 'trFolder' translate all files in folder and subfolders ");
      System.out.println("\t \t trFolder [start folder] [destination folder] [threadsNumber]");

      System.out.println("16. use 'jsonlCall' for each line of jsonl file send an X value to server and save the result and wait to status of te request change to done then proceed");
      System.out.println(" also could mentioned to continue from the progress file");
      System.out.println("\t \t jsonlCall [json file] [destination file] [api to call] [api for check status] [is continue] [file to save progress] ");

      System.out.println("17. use 'jsonlCallProcessResult' for each line of jsonl file send an X value to server and process the response ");
      System.out.println(" use for gathering nebula training samples");
      System.out.println("\t \t jsonlCallProcessResult [json file] [destination file] [api to call]");

      System.out.println("18. use 'CrR' to calculate the Coreference Resolution of all texts in a directory");
      System.out.println("\t \t CrR [start directory] [destination directory]");

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

      //
      if(args.length == 3 && args[0].equals("dbp")){
          String inputPath = args[1];
          String outputPath = args[2];
          File inputFolder = new File(inputPath);
          File[] existFiles = inputFolder.listFiles();
          for(File f:existFiles){
              if(!f.isDirectory() && f.isFile()){
                  //if(FilenameUtils.getExtension(f.getName()).equals("bz2")){
                      dboPreprocessor.process(f.getAbsolutePath(), outputPath,true);
                  //}
              }
          }
      }

      //System.out.println("12. use 'reLi' remove literatur");
      //      System.out.println("\t \t reLI [input file] [output file]");
      // 12

      if(args.length == 3 && args[0].equals("reLiFOLDER")){
          String inputPath = args[1];
          String outputPathWithSlash = args[2];
          File inputFolder = new File(inputPath);
          File[] existFiles = inputFolder.listFiles();
          int counter = 0;
          for(File f:existFiles){
              if(!f.isDirectory() && f.isFile()){
                  if(FilenameUtils.getExtension(f.getName()).equals("bz2")){
                      counter = counter +1;
                      dboPreprocessor.removeLiteral(f.getAbsolutePath(), outputPathWithSlash);
                  }
              }
          }
      }

      if(args.length == 3 && args[0].equals("reLi")){
          String inputPath = args[1];
          File inputFile = new File(inputPath);
          if (!inputFile.exists()) {
              System.out.println("no file exist");
              return;
          }

          if (!inputFile.isFile()) {
              System.out.println(inputPath + " is not a file");
              return;
          }

          if (!inputFile.canRead()) {
              System.out.println(inputPath + " is not readable");
              return;
          }

          String outputPath = args[2];
          dboPreprocessor.removeLiteral(inputFile.getAbsolutePath(),outputPath);


      }

      if(args.length == 4 && args[0].equals("fillTemplate")){
          String inputPath = args[1];
          File inputFile = new File(inputPath);
          if (!inputFile.exists()) {
              System.out.println("no file exist");
              return;
          }

          if (!inputFile.isFile()) {
              System.out.println(inputPath + " is not a file");
              return;
          }

          if (!inputFile.canRead()) {
              System.out.println(inputPath + " is not readable");
              return;
          }

          String templateFile = args[2];
          String outputPath = args[3];

          csvProcessor.fillTheTemplate(inputPath,templateFile,outputPath);
      }

      // 15 trFolder [start folder] [destination folder] [threadsNumber]
      if(args.length == 4 && args[0].equals("trFolder")){
          String startFolderPath = args[1];
          String destinationPath = args[2];
          int threadsNumber = Integer.parseInt(args[3]);

          if(!isDirectoryExist(startFolderPath) || !isDirectoryExist(destinationPath)){
              return;
          }

          List<ITaskHandler> taskHandlers = new ArrayList<>();
          taskHandlers.add(new Translator("http://neamt.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          taskHandlers.add(new Translator("http://neamt1.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          taskHandlers.add(new Translator("http://neamt2.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          taskHandlers.add(new Translator("http://neamt3.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          taskHandlers.add(new Translator("http://neamt4.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          taskHandlers.add(new Translator("http://neamt5.cs.upb.de:6100/custom-pipeline", new TranslatedResult2ElasticMapper(), destinationPath, threadsNumber));
          FolderCrawler folderCrawler = new FolderCrawler(taskHandlers);
          folderCrawler.start(startFolderPath);
      }

      // 16 jsonlCall [json file] [destination file] [api to call] [api for check status] [is continuing] [file to save progress]
      if(args.length == 7 && args[0].equals("jsonlCall")){
          String filePath = args[1];
          String destinationFilePath = args[2];
          String api = args[3];
          String apistatusCheck = args[4];
          String isContinuing = args[5];
          String progressfilePath = args[6];

//http://localhost:5000/check?text=
          APICaller apic = new APICaller(api,destinationFilePath, apistatusCheck,progressfilePath);
          if(isContinuing.equals("no")) {
              HashMap<String, Boolean> jsons = new HashMap<>();
              try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                  String line;
                  while ((line = reader.readLine()) != null) {
                      jsons.put(line, false);
                  }
              } catch (IOException e) {
                  System.out.println("An error occurred while reading the file: " + e.getMessage());
              }
              apic.handleTask(jsons);
          }
          else {
              File progressFile = new File(progressfilePath);
              if (!progressFile.exists()) {
                  HashMap<String, Boolean> jsons = new HashMap<>();
                  try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                      String line;
                      while ((line = reader.readLine()) != null) {
                          jsons.put(line, false);
                      }
                  } catch (IOException e) {
                      System.out.println("An error occurred while reading the file: " + e.getMessage());
                  }
                  FileOutputStream fileOut = null;
                  try {
                      fileOut =
                              new FileOutputStream(progressfilePath);
                      ObjectOutputStream out = new ObjectOutputStream(fileOut);
                      System.out.println("updatePr");
                      out.writeObject(jsons);
                      out.close();
                      fileOut.close();
                  } catch (IOException i) {
                      i.printStackTrace();
                  }finally {
                      try {
                          if (fileOut != null) {
                              fileOut.close();
                          }
                      }catch (Exception ex){
                          ex.printStackTrace();
                      }
                  }
              }
              apic.handleTaskFromFile();
          }
      }

      // 17 jsonlCallProcessResult [json file] [destination file] [api to call]
      if(args.length == 4 && args[0].equals("jsonlCallProcessResult")){
          String filePath = args[1];
          String destinationFilePath = args[2];
          String api = args[3];

//http://localhost:5000/textsearch?text=
          APICaller apic = new APICaller(api,destinationFilePath, "","temp");
          try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
              String line;
              while ((line = reader.readLine()) != null) {
                  JSONObject obj = new JSONObject(line);
                  String claim = obj.getString("claim");
                  String label = obj.getString("label");
                  BigInteger id = obj.getBigInteger("id");
                  if(id.toString().equals("138117")){
                      String we;
                      we = "new test";
                  }
                  try {
                      HttpResponse<String> res = Unirest.get(api+apic.urlEncode(claim,"UTF-8"))
                              .asString();
                      String body = res.getBody();
                      JSONObject responseAsJson = new JSONObject(body);
                      JSONArray tempresults = responseAsJson.getJSONArray("results");
                      if(tempresults.length()==0){
                          System.out.println("not calculated :"+claim);
                          addForCheck(id, claim, label,filePath, destinationFilePath);
                      }else{
                          JSONObject lastchild = (JSONObject)tempresults.get(tempresults.length()-1);
                          int STAGE_NUMBER = lastchild.getInt("STAGE_NUMBER");
                          if(STAGE_NUMBER <3){
                              System.out.println("steeps not complete :"+claim);
                          }else{
                              //JSONObject claimCheckWorthinessResult = new JSONObject(lastchild.getString("CLAIM_CHECK_WORTHINESS_RESULT"));
                              //JSONObject evidenceRetrivalResult = new JSONObject(lastchild.getString("EVIDENCE_RETRIVAL_RESULT"));
                              String tmpstances = lastchild.getString("STANCE_DETECTION_RESULT");

                              tmpstances = tmpstances.replace("\n","");
                              //System.out.println(tmpstances);
                              //System.out.println("------"+id);
                              tmpstances = tmpstances.replace(",\"[",",'[").replace(",\"(",",'(").replace(",\" ",",' ").replace(",\t ",",'\t").replace("\": ","': ").replaceAll("[a-zA-Z],\\\"",",'");
                              tmpstances = tmpstances.replace("{\"","__XX__").replace("\":\"","__YY__").replace("\",\"","__ZZ__");
                              tmpstances = tmpstances.replace("\":","__OO__").replace(",\"","__HH__");
                              tmpstances = tmpstances.replace("\"","'");
                              tmpstances = tmpstances.replace("__XX__","{\"").replace("__YY__","\":\"").replace("__ZZ__","\",\"");
                              tmpstances = tmpstances.replace("__OO__","\":").replace("__HH__",",\"");
                              //System.out.println(tmpstances);
                              JSONObject stanceDetectionResult = new JSONObject(tmpstances);
                              StringBuilder jsonl = new StringBuilder();
                              jsonl.append("{\"label\":\"");
                              jsonl.append(label);
                              jsonl.append("\",\"id\":\"");
                              jsonl.append(id);
                              jsonl.append("\",\"scores\":[");

                              JSONArray stances = stanceDetectionResult.getJSONArray("stances");
                              for(int stance_c = 0 ; stance_c < stances.length() ; stance_c++){
                                  JSONObject tmpc  = (JSONObject)stances.get(stance_c);
                                  Double elastic_score = tmpc.getDouble("elastic_score");
                                  Double stance_score = tmpc.getDouble("stance_score");
                                  jsonl.append("{\"elastic_score\":"+elastic_score+",");
                                  jsonl.append("\"stance_score\":"+stance_score+"}");
                                  if(stance_c+1<stances.length()){
                                      jsonl.append(",");
                                  }
                              }
                              jsonl.append("]}");

                              // write to file

                              try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFilePath, true))) {
                                  writer.write(jsonl.toString());
                                  writer.newLine();
                              } catch (IOException e) {
                                  e.printStackTrace();
                              }
                          }
                      }

                  }catch (Exception ex){
                      //ex.printStackTrace();
                      addForCheck(id, claim, label,filePath,destinationFilePath);
                  }
              }
          } catch (IOException e) {
              System.out.println("An error occurred while reading the file: " + e.getMessage());
          }
      }


      // 18. use 'CrR' to calculate the Coreference Resolution of all texts in a directory
      if(args.length == 3 && args[0].equals("CrR")){
          String startFolderPath = args[1];
          String destinationPath = args[2];

          if(!isDirectoryExist(startFolderPath) || !isDirectoryExist(destinationPath)){
              return;
          }

          List<ITaskHandler> taskHandlers = new ArrayList<>();
          taskHandlers.add(new CorefrenceResulotionGenerator(destinationPath));
          FolderCrawler folderCrawler = new FolderCrawler(taskHandlers);
          folderCrawler.start(startFolderPath);
      }

      System.out.println("Finish");
  }

    private void addForCheck(BigInteger id, String claim, String label,String filePath,String destinationFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFilePath+".tsv.error", true))) {
            writer.write(id+"\t"+label+"\t"+claim+"\t"+filePath);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
