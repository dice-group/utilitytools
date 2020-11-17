package org.dice.utilitytools.service.NtFileUpdate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NtFileUpdaterTest {

  @Autowired NtFileUpdater service;

  final String fileName = "SNLP2019_test.nt";

  @Test
  public void NtFileUpdaterShouldReadFile() {
    // String fn = service.PreProccessFile(fileName);
    service.Update(fileName);
  }

  // @Test
  // public void PreProccessWorks() {
  // service.PreProccessFile(fileName);
  // }

  // @Test
  // public void givenFileNameAsAbsolutePath_whenUsingClasspath_thenFileData() throws IOException {

  // Object t = new FileInputStream(getClass().getResourceAsStream(fileName));

  // try (InputStream inputStream = getClass().getResourceAsStream(fileName);
  // BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
  // String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
  // System.out.println(contents);
  // }
  // }
}
