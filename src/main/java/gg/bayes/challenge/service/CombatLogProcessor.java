package gg.bayes.challenge.service;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type;
import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.persistence.repository.MatchRepository;
import gg.bayes.challenge.service.model.CombatLogEvent;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CombatLogProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CombatLogProcessor.class);
  private final Map<Long, Map<Type, AtomicInteger>> statistic = new ConcurrentHashMap<>();

  private final MatchRepository matchRepository;

  private final CombatLogEntryRepository combatLogEntryRepository;
  private final CombatLogParser parser;

  private final ModelMapper modelMapper = new ModelMapper();
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private final Map<Long, Set<CompletableFuture<Void>>> matchTasks = new ConcurrentHashMap<>();

  public CombatLogProcessor(MatchRepository matchRepository,
      CombatLogEntryRepository combatLogEntryRepository, CombatLogParser parser) {
    this.matchRepository = matchRepository;
    this.combatLogEntryRepository = combatLogEntryRepository;
    this.parser = parser;
  }

  public MatchEntity createNewMatch() {
    MatchEntity matchEntity = new MatchEntity();
    matchRepository.save(matchEntity);
    return matchEntity;
  }

  public void processCombatLogRecord(MatchEntity match, String record) {
    CombatLogEvent event = parser.parseCombatLogRecord(match.getId(), record);
    if (event == null) {
      return;
    }
    Set<CompletableFuture<Void>> tasks =
        matchTasks.computeIfAbsent(match.getId(), key -> ConcurrentHashMap.newKeySet());

    tasks.add(
        CompletableFuture.runAsync(
            () -> {
              CombatLogEntryEntity entity = modelMapper.map(event, CombatLogEntryEntity.class);
              entity.setMatch(match);
              combatLogEntryRepository.save(entity);
              updateStatistic(match.getId(), event);
            },
            executorService
        )
    );
  }

  public void finalizeAndClean(Long matchId, long startTime) {
    CompletableFuture<Void> combinedFuture =
        CompletableFuture.allOf(matchTasks.get(matchId).toArray(new CompletableFuture[] {}));
    combinedFuture.whenComplete((result, error) -> {
      if (error != null) {
        LOGGER.error("Failed attempt to finalize combat log processing: match id = {}", matchId);
      } else {
        printStatistic(matchId, Instant.now().toEpochMilli() - startTime);
      }
      parser.reset(matchId);
      statistic.remove(matchId);
      matchTasks.remove(matchId);
    });
  }

  private void printStatistic(Long matchId, long processingTime) {
    LOGGER.info("Combat log statistic: match id = {}, processing time {} ms, {}",
        matchId, processingTime, statistic.get(matchId));
  }

  private void updateStatistic(Long matchId, CombatLogEvent event) {
    Map<Type, AtomicInteger> stat =
        statistic.computeIfAbsent(matchId, key -> new ConcurrentHashMap<>());

    AtomicInteger old = stat.putIfAbsent(event.getType(), new AtomicInteger(1));
    if (old != null) {
      stat.computeIfPresent(event.getType(), (k, v) -> {
        v.incrementAndGet();
        return v;
      });
    }
  }
}
