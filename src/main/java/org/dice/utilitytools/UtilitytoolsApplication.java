package org.dice.utilitytools;

import java.io.File;
import org.dice.utilitytools.service.NtFileUpdate.NtFileUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UtilitytoolsApplication implements CommandLineRunner {

  @Autowired NtFileUpdater service;

  public static void main(String[] args) {
    SpringApplication.run(UtilitytoolsApplication.class, args);
  }

  @Override
  public void run(String... args) {



    if (args == null || args.length == 0) {
      System.out.println("no arguments ! use h to get help");
      return;
    }

    if (args.length ==  1 && args[0].equals("h")){
      System.out.println("this is help");
      System.out.println("1 . use 'up' for update the file ");
      System.out.println("\t \t up [file for update] [verbalizedFileForReplace] [optional t : it is training file]");
      System.out.println("2 . use 'eo' for explicit ontology");
      System.out.println("\t \t eo [ontology file] [verbalizedFileForReplace] [optional t : it is training file]");
    }

    // update file
    if(args.length > 1 && args[0].equals("up") ){
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

      if (args.length == 3) {
        if (args[2].toLowerCase().equals("t")) {
          service.update(args[1], "", true);
        } else {
          service.update(args[1], args[2], false);
        }
      }

      if (args.length == 4) {
        boolean isTraining = false;
        if (args[3].toLowerCase().equals("t")) {
          isTraining = true;
        }
        service.update(args[1], args[2], isTraining);
      }

      if (args.length == 2 || args.length == 3) {
        System.out.println(" Job Done");
      } else {
        System.out.println(" wrong parameters!");
      }
    }

    if(args.length > 1 && args[0].equals("eo")){

    }
  }
}
