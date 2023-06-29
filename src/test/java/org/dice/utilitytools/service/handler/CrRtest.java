package org.dice.utilitytools.service.handler;

import org.dice.utilitytools.UtilitytoolsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UtilitytoolsApplication.class)
public class CrRtest {
    @Test
    public void doesItWork()
    {
        CorefrenceResulotionGenerator CrR = new CorefrenceResulotionGenerator(null);
        String accutal = CrR.generateCrR("Barack Obama was born in Hawaii.  He is the president. Obama was elected in 2008.");
        String expected = "Barack Barack Obama was born in Hawaii.  Barack Barack Obama is Barack Barack Obama. Barack Obama was elected in 2008.";
        assertEquals(expected, accutal);
    }
}
