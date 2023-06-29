package org.dice.utilitytools.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;

public class CorefrenceResulotionGenerator implements ITaskHandler<Void, HashMap<String,Boolean>>{
    private static final Logger LOGGGER = LoggerFactory.getLogger(CorefrenceResulotionGenerator.class);
    String destinationPath ;

    public CorefrenceResulotionGenerator(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    @Override
    public Void handleTask(HashMap<String, Boolean> input) {
        try {
            for (Map.Entry<String, Boolean> entry : input.entrySet()) {
                String path = entry.getKey();
                Boolean value = entry.getValue();
                String textForCrR = Files.readString(Path.of(path));

            }
        }catch (Exception ex){
            LOGGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    String generateCrR(String input){
/*        StringBuilder sb = new StringBuilder();
        Annotation document = new Annotation(input);
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        pipeline.annotate(document);
        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
        }

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
                sb.append(m);
            }
        }
        return sb.toString();*/

        // Create StanfordCoreNLP object with coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(input);

        // Run all annotators on the text
        pipeline.annotate(document);

        // Get the coreference information
        Map<Integer, CorefChain> coreferenceChains = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        // Replace pronouns and noun phrases with their most typical mentions
        String resolvedText = replaceCoreferences(input, coreferenceChains);
        return resolvedText;
    }

    public static String replaceCoreferences(String text, Map<Integer, CorefChain> coreferenceChains) {
        Map<Integer, String> mentions = new HashMap<>();

        for (Map.Entry<Integer, CorefChain> entry : coreferenceChains.entrySet()) {
            CorefChain.CorefMention representativeMention = entry.getValue().getRepresentativeMention();
            String representativeText = representativeMention.mentionSpan;
            for (CorefChain.CorefMention mention : entry.getValue().getMentionsInTextualOrder()) {
                if (!mention.equals(representativeMention)) {
                    String mentionText = mention.mentionSpan;
                    text = text.replace(mentionText, representativeText);
                }
            }
            mentions.put(representativeMention.sentNum, representativeText);
        }

        for (Map.Entry<Integer, String> entry : mentions.entrySet()) {
            String corefId = "[" + entry.getKey() + "]";
            text = text.replace(corefId, entry.getValue());
        }

        return text;
    }
}
