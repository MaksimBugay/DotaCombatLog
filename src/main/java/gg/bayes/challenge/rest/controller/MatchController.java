package gg.bayes.challenge.rest.controller;

import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.rest.model.HeroDamage;
import gg.bayes.challenge.rest.model.HeroItem;
import gg.bayes.challenge.rest.model.HeroKills;
import gg.bayes.challenge.rest.model.HeroSpells;
import gg.bayes.challenge.service.CombatLogProcessor;
import gg.bayes.challenge.service.MatchService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/match")
@Validated
public class MatchController {

  private final CombatLogProcessor combatLogProcessor;

  private final MatchService matchService;

  private final ModelMapper modelMapper = new ModelMapper();

  @Autowired
  public MatchController(CombatLogProcessor combatLogProcessor, MatchService matchService) {
    this.combatLogProcessor = combatLogProcessor;
    this.matchService = matchService;
  }

  /**
   * Ingests a DOTA combat log file, parses and persists relevant events data. All events are associated with the same
   * match id.
   *
   * @return the match id associated with the parsed events
   */
  @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<Long> ingestCombatLog(
      HttpServletRequest request
      //@RequestBody @NotBlank String combatLog
  ) {
    long lineCount = 0;
    long startTime = Instant.now().toEpochMilli();
    MatchEntity match = combatLogProcessor.createNewMatch();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lineCount++;
        combatLogProcessor.processCombatLogRecord(match, line.trim());
      }
      combatLogProcessor.finalizeAndClean(match.getId(), startTime);
      return ResponseEntity.ok(match.getId());
    } catch (Exception e) {
      return ResponseEntity.status(500).body(lineCount);
    }
  }

  /**
   * Fetches the heroes and their kill counts for the given match.
   *
   * @param matchId the match identifier
   * @return a collection of heroes and their kill counts
   */
  @GetMapping(
      path = "{matchId}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<List<HeroKills>> getHeroKills(@PathVariable("matchId") Long matchId) {
    return new ResponseEntity<>(
        matchService.getAllHeroKills(matchId).stream()
            .map(item -> new HeroKills(item.getHero(), item.getKills()))
            .collect(Collectors.toList()), HttpStatus.OK
    );
  }

  /**
   * For the given match, fetches the items bought by the named hero.
   *
   * @param matchId  the match identifier
   * @param heroName the hero name
   * @return a collection of items bought by the hero during the match
   */
  @GetMapping(
      path = "{matchId}/{heroName}/items",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<List<HeroItem>> getHeroItems(
      @PathVariable("matchId") Long matchId,
      @PathVariable("heroName") String heroName) {
    return new ResponseEntity<>(
        matchService.getHeroItems(matchId, heroName).stream()
            .map(item -> new HeroItem(item.getItem(), item.getTimestamp()))
            .collect(Collectors.toList()), HttpStatus.OK
    );
  }

  /**
   * For the given match, fetches the spells cast by the named hero.
   *
   * @param matchId  the match identifier
   * @param heroName the hero name
   * @return a collection of spells cast by the hero and how many times they were cast
   */
  @GetMapping(
      path = "{matchId}/{heroName}/spells",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<List<HeroSpells>> getHeroSpells(
      @PathVariable("matchId") Long matchId,
      @PathVariable("heroName") String heroName) {
    return new ResponseEntity<>(
        matchService.getHeroSpells(matchId, heroName).stream()
            .map(item -> new HeroSpells(item.getSpell(), item.getCasts()))
            .collect(Collectors.toList()), HttpStatus.OK
    );
  }

  /**
   * For a given match, fetches damage done data for the named hero.
   *
   * @param matchId  the match identifier
   * @param heroName the hero name
   * @return a collection of "damage done" (target, number of times and total damage) elements
   */
  @GetMapping(
      path = "{matchId}/{heroName}/damage",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<List<HeroDamage>> getHeroDamages(
      @PathVariable("matchId") Long matchId,
      @PathVariable("heroName") String heroName) {
    return new ResponseEntity<>(
        matchService.getHeroDamages(matchId, heroName).stream()
            .map(item -> new HeroDamage(item.getTarget(), item.getDamageInstances(),
                item.getTotalDamage()))
            .collect(Collectors.toList()), HttpStatus.OK
    );
  }
}
