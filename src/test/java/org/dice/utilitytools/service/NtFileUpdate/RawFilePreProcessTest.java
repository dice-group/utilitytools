package org.dice.utilitytools.service.NtFileUpdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RawFilePreProcessTest {

  @Autowired RawFilePreProcess processor;

  @Test
  public void EnhanceShouldReturnCorrectResult() {
    String input = " ÅÄÄ‡%C3%A1%C3%A4%C3%A7%C3%A9%C3%AD%C3%B6%C3%B8%C5%8D%C5%8C%C3%89 ";
    String expected = "ōčćáäçéíöøōŌÉ\n";
    String actual = processor.Enhance(input);
    assertEquals(expected, actual);
  }
}
