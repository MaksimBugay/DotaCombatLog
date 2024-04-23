package gg.bayes.challenge.rest.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 * Integration test template to get you started. Add tests and make modifications as you see fit.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class MatchControllerIntegrationTest {

  private static final String COMBATLOG_FILE_1 = "/data/combatlog_1.log.txt";
  private static final String COMBATLOG_FILE_2 = "/data/combatlog_2.log.txt";

  @Autowired
  private MockMvc mvc;

  private Map<String, Future<Long>> matchIds;

  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

  @BeforeAll
  void setup() throws Exception {
    // Populate the database with all events from both sample data files and store the returned
    // match IDS.
    matchIds = Map.of(
        COMBATLOG_FILE_1, ingestMatch(COMBATLOG_FILE_1),
        COMBATLOG_FILE_2, ingestMatch(COMBATLOG_FILE_2));
  }

  // Replace this test method with the tests that you consider appropriate to test your implementation.
  @Test
  void someTest() throws Exception {
    assertThat(mvc).isNotNull();
    Thread.sleep(2000);
    Long matchId1 = matchIds.get(COMBATLOG_FILE_1).get();
    Long matchId2 = matchIds.get(COMBATLOG_FILE_2).get();
    String json = mvc.perform(get("/api/match/" + matchId1)
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    System.out.println(json);

    json = mvc.perform(get("/api/match/" + matchId2 + "/keeper_of_the_light/items")
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    System.out.println(json);

    json = mvc.perform(get("/api/match/" + matchId2 + "/earthshaker/spells")
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    System.out.println(json);

    json = mvc.perform(get("/api/match/" + matchId1 + "/dragon_knight/damage")
            .contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse()
        .getContentAsString();
    System.out.println(json);
  }

  /**
   * Helper method that ingests a combat log file and returns the match id associated with all parsed events.
   *
   * @param file file path as a classpath resource, e.g.: /data/combatlog_1.log.txt.
   * @return the id of the match associated with the events parsed from the given file
   * @throws Exception if an error happens when reading or ingesting the file
   */
  private Future<Long> ingestMatch(String file) throws Exception {
    return executorService.submit(() -> {
      String fileContent = IOUtils.resourceToString(file, StandardCharsets.UTF_8);

      return Long.parseLong(mvc.perform(post("/api/match")
              .contentType(MediaType.TEXT_PLAIN)
              .content(fileContent))
          .andReturn()
          .getResponse()
          .getContentAsString());
    });
  }
}
