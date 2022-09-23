package org.dice.utilitytools.service.NtFileUpdate;

import java.util.HashMap;
import org.aksw.simba.bengal.verbalizer.SemWeb2NLVerbalizer;
import org.dice.utilitytools.Model.ProcessedModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NtFileUpdater {

  private static final Logger logger = LoggerFactory.getLogger(SemWeb2NLVerbalizer.class);

  @Autowired private RawFilePreProcess preProcessor;

  @Autowired private ModelBuilder model;

  @Autowired private ModelProcessor processor;

  @Autowired private ResultExtractor extractor;

  @Autowired private ResultPatcher patcher;

  ProcessedModelResponse processedModel;

  HashMap<String, String> resultMap = new HashMap<String, String>();

  public NtFileUpdater() {
    super();
  }

  public void update(String fileName, String verbalizedFileForReplace, boolean isTraining) {
    try {
      System.out.println("fileName is :" + fileName);
      System.out.println("verbalizedFileForReplace is :" + verbalizedFileForReplace);
      // update some characters in a file
      fileName = preProcessor.Process(fileName,"updateFile");
      // made a model
      model.build(fileName);

      processedModel = processor.ProcessModelAndUpdate(model.getModel());

      resultMap = extractor.ExtractResult(processedModel, fileName, isTraining);
      if (verbalizedFileForReplace.length() > 5) {
        System.out.println("do with verbalization");
        patcher.patch(verbalizedFileForReplace, resultMap, processedModel.getShouldRemoveKeys());
      }else{
        System.out.println("No verbalization");
      }

      System.out.println(processedModel.sizeReport());
    } catch (Exception exp) {
      String kkk = exp.getMessage();
      System.out.println(kkk);
    }
  }

  public void switchSubjectAndObjectForAPredicate(String fileName,String predicate){
    try {
      System.out.println("fileName is :" + fileName);
      System.out.println("predicate is :" + predicate);
      fileName = preProcessor.Process(fileName,"switchSubjectObjectFor("+predicate+")");
      model.build(fileName);
      processedModel = processor.ProcessModelAndSwitchSubjectAndObjectForPredicate(model.getModel(), predicate);
      extractor.ExtractResult(processedModel, fileName, false);
    } catch (Exception exp) {
      String kkk = exp.getMessage();
      System.out.println(kkk);
    }
  }
}
