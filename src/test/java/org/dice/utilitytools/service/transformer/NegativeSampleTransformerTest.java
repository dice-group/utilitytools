package org.dice.utilitytools.service.transformer;

import org.dice.utilitytools.UtilitytoolsApplication;
import org.dice.utilitytools.service.transform.NegativeSampleTransformer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UtilitytoolsApplication.class)
public class NegativeSampleTransformerTest {
    @Autowired
    NegativeSampleTransformer service ;

    @Test
    public void generateCorrectNegativeForSimpleTwoSample(){

         List<String> input = new ArrayList<>();

         input.add("s1 p o1 .");
         input.add("s2 p o3 .");

         List<String> actual = service.generate(input, " ");

         List<String> expected = new ArrayList<>();

         expected.add("s1 p o3 .");
         expected.add("s2 p o1 .");

         Assert.assertEquals(expected, actual);
     }

    @Test
    public void generateCorrectNegativeForTwoSample(){

        List<String> input = new ArrayList<>();

        input.add("s1 p o1 .");
        input.add("s1 p o2 .");
        input.add("s2 p o3 .");

        List<String> actual = service.generate(input, " ");

        List<String> expected = new ArrayList<>();

        expected.add("s1 p o3 .");
        expected.add("s1 p o3 .");
        expected.add("s2 p o1 .");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void generateCorrectNegativeForThreeSample(){

        List<String> input = new ArrayList<>();

        input.add("s1 p o1 .");
        input.add("s1 p o2 .");
        input.add("s2 p o3 .");
        input.add("s2 p o4 .");
        input.add("s3 p o5 .");
        input.add("s3 p o6 .");

        List<String> actual = service.generate(input, " ");

        List<String> expected = new ArrayList<>();

        expected.add("s1 p o3 .");
        expected.add("s1 p o4 .");
        expected.add("s2 p o1 .");
        expected.add("s2 p o2 .");
        expected.add("s3 p o1 .");
        expected.add("s3 p o2 .");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void generateCorrectNegativeForSharedObjects(){

        List<String> input = new ArrayList<>();

        input.add("s1 p o1 .");
        input.add("s1 p o2 .");
        input.add("s2 p o2 .");
        input.add("s2 p o3 .");
        input.add("s3 p o3 .");
        input.add("s3 p o4 .");

        List<String> actual = service.generate(input, " ");

        List<String> expected = new ArrayList<>();

        expected.add("s1 p o3 .");
        expected.add("s1 p o4 .");
        expected.add("s2 p o1 .");
        expected.add("s2 p o4 .");
        expected.add("s3 p o1 .");
        expected.add("s3 p o2 .");

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void generateCorrectNegativeFortwoToMany(){

        List<String> input = new ArrayList<>();

        input.add("s1 p o8 .");
        input.add("s1 p o7 .");
        input.add("s1 p o6 .");
        input.add("s1 p o5 .");
        input.add("s1 p o4 .");
        input.add("s1 p o3 .");
        input.add("s1 p o2 .");
        input.add("s1 p o1 .");
        input.add("s2 p o9 .");
        input.add("s2 p o10 .");


        List<String> actual = service.generate(input, " ");

        List<String> expected = new ArrayList<>();

        expected.add("s1 p o10 .");
        expected.add("s1 p o9 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o9 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o9 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o9 .");
        expected.add("s2 p o1 .");
        expected.add("s2 p o2 .");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void generateCorrectNegativeFordublicateAndShare(){

        List<String> input = new ArrayList<>();

        input.add("s1 p o8 .");
        input.add("s1 p o7 .");
        input.add("s1 p o6 .");
        input.add("s1 p o5 .");
        input.add("s1 p o4 .");
        input.add("s1 p o4 .");
        input.add("s1 p o8 .");
        input.add("s1 p o1 .");
        input.add("s2 p o1 .");
        input.add("s2 p o10 .");
        input.add("s2 p o11 .");


        List<String> actual = service.generate(input, " ");

        List<String> expected = new ArrayList<>();

        expected.add("s1 p o10 .");
        expected.add("s1 p o11 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o11 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o11 .");
        expected.add("s1 p o10 .");
        expected.add("s1 p o11 .");
        expected.add("s2 p o4 .");
        expected.add("s2 p o5 .");
        expected.add("s2 p o6 .");

        Assert.assertEquals(expected, actual);
    }
}
