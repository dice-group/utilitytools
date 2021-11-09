package org.dice.utilitytools.service.ontology;

import org.junit.Test;

import java.io.IOException;

public class OntologyNtFileUpdaterTest {


    @Test
    public void canLoad() throws IOException, ClassNotFoundException {
        OntologyNtFileUpdater service = new OntologyNtFileUpdater();
        service.update("","map.ser","");
    }
}
