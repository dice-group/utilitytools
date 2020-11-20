package org.dice.utilitytools.service.NtFileUpdate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResultPatcherTest {

  @Autowired ResultPatcher patcher;

  @Test
  public void ReadFile_forTrainFile_ShouldWorkFine() {
    String fileName = "./src/test/java/org/dice/utilitytools/service/NtFileUpdate/training.tsv";
    HashMap<String, String> actual = patcher.ReadFile(fileName);

    assertEquals(2, actual.size());
    assertTrue(actual.containsKey("3603525"));
    assertTrue(actual.containsKey("3818307"));
    assertFalse(actual.containsKey("3818308"));
    assertEquals("Camp Rock stars Nick Jonas.\t1.0", actual.get("3603525"));
    assertEquals(
        "Santa Barbara, California is Robert Mitchum's nascence place.\t0.0",
        actual.get("3818307"));
  }

  @Test
  public void ReadFile_fortestFile_ShouldWorkFine() {
    String fileName = "./src/test/java/org/dice/utilitytools/service/NtFileUpdate/test.tsv";
    HashMap<String, String> actual = patcher.ReadFile(fileName);

    assertEquals(2, actual.size());
    assertTrue(actual.containsKey("3407450"));
    assertTrue(actual.containsKey("3857435"));
    assertFalse(actual.containsKey("3818308"));
    assertEquals("Rolandas Paksas' office is Lithuania.", actual.get("3407450"));
    assertEquals("London is Hawley Harvey Crippen's nascence place.", actual.get("3857435"));
  }

  @Test
  public void Replace_ForNoShareContent_ShouldReturnBoth() {
    HashMap<String, String> input1 = new HashMap<String, String>();
    input1.put("1", "1");
    HashMap<String, String> input2 = new HashMap<String, String>();
    input2.put("2", "2");

    HashMap<String, String> actual = patcher.Replace(input1, input2);

    assertEquals(2, actual.size());
    assertTrue(actual.containsKey("1"));
    assertTrue(actual.containsKey("2"));
    assertEquals("1", actual.get("1"));
    assertEquals("2", actual.get("2"));
  }

  @Test
  public void Replace_ForShareContent_ShouldReturnreplaced() {
    HashMap<String, String> input1 = new HashMap<String, String>();
    input1.put("1", "1");
    HashMap<String, String> input2 = new HashMap<String, String>();
    input2.put("1", "2");
    input2.put("2", "2");

    HashMap<String, String> actual = patcher.Replace(input1, input2);

    assertEquals(2, actual.size());
    assertTrue(actual.containsKey("1"));
    assertTrue(actual.containsKey("2"));
    assertEquals("2", actual.get("1"));
    assertEquals("2", actual.get("2"));
  }
}
