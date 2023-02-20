package org.dice.utilitytools;


import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice.utilitytools.service.filter.stream.NodeFilterBasedTripleFilter;
import org.dice.utilitytools.service.filter.stream.RDFStreamTripleFilter;
import org.dice.utilitytools.service.filter.stream.node.StringBasedNamespaceNodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

public class DBOPreprocessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBOPreprocessor.class);

    private static final Set<String> PROPERTY_BLACKLIST = new HashSet<String>(Arrays.asList(
            "http://dbpedia.org/ontology/wikiPageExternalLink", "http://dbpedia.org/ontology/dbo:wikiPageWikiLink"));

    public void process(String inputFile, String outputFilePath, int counter){
        try (Writer out1 = new FileWriter(outputFilePath+counter+".out")) {
            StreamRDF outStream = StreamRDFLib.writer(out1);
            ProgressMonitor monitor1 = ProgressMonitor.create(LOGGER, "Added triples", 100000, 10);
            outStream = new ProgressStreamRDF(outStream, monitor1);

            // From the remaining triples, take thos that have a dbo property which is not
            // on the blacklist
            StreamRDF stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(null, p -> p.getURI().startsWith("http://dbpedia.org/ontology/")
                            && !PROPERTY_BLACKLIST.contains(p.getURI()), null),
                    outStream);

            // Write RDF and RDFS triples to the output; forward all other triples to the
            // next filter
            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(null,
                    new StringBasedNamespaceNodeFilter(RDF.getURI(), RDFS.getURI()), null), outStream, stream);

            // Only use triples which have a subject of the dbo or dbr namespace. Make sure that the object is not a literal.
            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(
                    new StringBasedNamespaceNodeFilter("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"),
                    null, o -> !o.isLiteral()), stream);

            monitor1.start();

                LOGGER.info("Streaming file {}.", inputFile);
                ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 100000, 10);
                StreamRDF fileStream = new ProgressStreamRDF(stream, monitorS);
                monitorS.start();
                // If we have a bz2 file
                if(inputFile.endsWith("bz2")) {
                    try(BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(inputFile))) {
                        if(inputFile.contains("ttl")){
                            RDFDataMgr.parse(fileStream, bzIn, Lang.TTL);
                        }else {
                            RDFDataMgr.parse(fileStream, bzIn, Lang.NT);
                        }
                    }
                } else {
                    if(inputFile.contains("ttl")){
                        RDFDataMgr.parse(stream, inputFile, Lang.TTL);
                    }else {
                        RDFDataMgr.parse(stream, inputFile, Lang.NT);
                    }
                }

            monitor1.finish();
            stream.finish();
            LOGGER.info("Finished");
        }catch (Exception ex){
            LOGGER.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
