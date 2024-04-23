package gg.bayes.challenge.service;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.DAMAGE_DONE;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.HERO_KILLED;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.ITEM_PURCHASED;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.SPELL_CAST;
import static gg.bayes.challenge.service.CombatLogParser.getHeroNameFromTarget;

import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.service.model.HeroDamage;
import gg.bayes.challenge.service.model.HeroItem;
import gg.bayes.challenge.service.model.HeroKills;
import gg.bayes.challenge.service.model.HeroSpells;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

  private final CombatLogEntryRepository repository;

  public MatchService(CombatLogEntryRepository repository) {
    this.repository = repository;
  }

  public List<HeroKills> getAllHeroKills(Long matchId) {
    return repository.findByMatchIdAndTypeGroupedByActor(
            matchId,
            HERO_KILLED
        ).stream()
        .map(map -> new HeroKills((String) map.get("actor"), ((Long) map.get("count")).intValue()))
        .collect(Collectors.toList());
  }

  public List<HeroItem> getHeroItems(Long matchId, String heroName) {
    return repository.findByMatchIdAndActor(matchId, heroName, ITEM_PURCHASED).stream()
        .map(event -> new HeroItem(event.getItem(), event.getTimestamp()))
        .collect(Collectors.toList());
  }

  public List<HeroSpells> getHeroSpells(Long matchId, String heroName) {
    return repository.findByMatchIdAndActorAndTypeGroupedByAbility(
            matchId,
            heroName,
            SPELL_CAST
        ).stream()
        .map(map -> new HeroSpells((String) map.get("ability"),
            ((Long) map.get("count")).intValue()))
        .collect(Collectors.toList());
  }

  public List<HeroDamage> getHeroDamages(Long matchId, String heroName) {
    return repository.findByMatchIdAndActorAndTypeGroupedByTarget(
            matchId,
            heroName,
            DAMAGE_DONE
        ).stream()
        .map(map -> new HeroDamage(
            getHeroNameFromTarget((String) map.get("target")),
            ((Long) map.get("count")).intValue(),
            ((Long) map.get("sum")).intValue())
        )
        .collect(Collectors.toList());
  }
}
