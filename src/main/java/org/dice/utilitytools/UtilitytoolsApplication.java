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
      System.out.println("no file mentioned");
      return;
    }
    File f = new File(args[0]);
    if (!f.exists()) {
      System.out.println("no file exist");
      return;
    }

    if (!f.isFile()) {
      System.out.println(args[0] + " is not a file");
      return;
    }

    if (!f.canRead()) {
      System.out.println(args[0] + " is not readable");
      return;
    }

    service.Update(args[0]);

    System.out.println(" Job Done");
  }
}
