package org.dice.utilitytools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dice.utilitytools.service.NtFileUpdate.NtFileUpdater;
import org.dice.utilitytools.service.filter.CommonRDFFilter;
import org.dice.utilitytools.service.load.IOService;
import org.dice.utilitytools.service.ontology.OntologyNtFileUpdater;
import org.dice.utilitytools.service.spliter.BasedDateSpliter;
import org.dice.utilitytools.service.transform.NegativeSampleTransformer;
import org.dice.utilitytools.service.transform.Transformer;
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

  public static void main(String[] args) {
    SpringApplication.run(UtilitytoolsApplication.class, args);
  }

  @Override
  public void run(String... args) throws IOException {

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

        ioService.writeListAsFile(bef ,args[3]+"before.out");
        ioService.writeListAsFile(aft ,args[3]+"after.out");
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

        ioService.writeListAsFile(converted ,args[4]);
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

        ioService.writeListAsFile(theFilteredResult, savePath);
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


          ioService.writeListAsFile(theGeneratedFalseFacts, savePath);
      }

      System.out.println("Finish");
  }
}
