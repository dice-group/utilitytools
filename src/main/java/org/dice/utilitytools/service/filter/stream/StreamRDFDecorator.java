package org.dice.utilitytools.service.filter.stream;

import org.apache.jena.riot.system.StreamRDF;

/**
 * A decorator of a {@link StreamRDF} object. It acts as a {@link StreamRDF}
 * object and may or may not forward calls to the {@link StreamRDF} object that
 * it wraps.
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *  https://github.com/dice-group/rdf-tools/blob/main/rdf-tools.stream/src/main/java/org/dice_research/rdf/stream/StreamRDFDecorator.java
 */
public interface StreamRDFDecorator extends StreamRDF {

    /**
     * Returns the decorated {@link StreamRDF} object.
     *
     * @return the decorated object
     */
    public StreamRDF getDecorated();
}


