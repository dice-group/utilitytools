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
      fileName = preProcessor.Process(fileName);
      model.build(fileName);
      processedModel = processor.ProcessModel(model.getModel());
      resultMap = extractor.ExtractResult(processedModel, fileName, isTraining);
      if (verbalizedFileForReplace.length() > 5) {
        System.out.println("do with verbalization");
        patcher.patch(verbalizedFileForReplace, resultMap, processedModel.getShouldRemoveKeys());
      }else{
        System.out.println("No verbalization");
      }

    } catch (Exception exp) {
      String kkk = exp.getMessage();
      System.out.println(kkk);
    }
  }
}
