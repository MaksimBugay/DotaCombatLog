package gg.bayes.challenge.service;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.DAMAGE_DONE;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.HERO_KILLED;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.ITEM_PURCHASED;
import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.SPELL_CAST;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type;
import gg.bayes.challenge.service.model.CombatLogEvent;
import gg.bayes.challenge.service.model.DamageDone;
import gg.bayes.challenge.service.model.HeroKilled;
import gg.bayes.challenge.service.model.ItemPurchased;
import gg.bayes.challenge.service.model.SpellCast;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CombatLogParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(CombatLogParser.class);
  private static final String DOTA_UNKNOWN_INSTRUCTION = "unknown";

  private static final String DOTA_TARGET_HERO_PREFIX = "hero_";

  private final Map<Long, SpellCast> recentSpells = new ConcurrentHashMap<>();

  public static String getHeroNameFromTarget(String target) {
    if (target.contains(DOTA_TARGET_HERO_PREFIX)) {
      return target.replace(DOTA_TARGET_HERO_PREFIX, "");
    }
    throw new IllegalArgumentException("Target is not a hero: " + target);
  }

  public void reset(Long matchId) {
    recentSpells.remove(matchId);
  }

  public CombatLogEvent parseCombatLogRecord(Long matchId, String record) {
    for (Type type : Type.values()) {
      CombatLogEvent event = parseCombatLogRecord(matchId, record, type);
      if (event != null) {
        return event;
      }
    }
    return null;
  }

  CombatLogEvent parseCombatLogRecord(Long matchId, String record, Type type) {
    switch (type) {
      case ITEM_PURCHASED:
        return parseItemPurchase(record);
      case HERO_KILLED:
        return parseHeroKilled(record);
      case SPELL_CAST:
        return parseSpellCast(matchId, record);
      case DAMAGE_DONE:
        return parseDamageDone(matchId, record);
    }
    throw new IllegalArgumentException("Unknown event type");
  }

  ItemPurchased parseItemPurchase(String record) {
    Matcher matcher = ITEM_PURCHASED.getPattern().matcher(record);
    if (matcher.find()) {
      String heroName = matcher.group(5);
      String itemName = matcher.group(6);
      long totalMilliseconds = parseTimestamp(matcher);

      return new ItemPurchased(heroName, itemName, totalMilliseconds);
    }
    return null;
  }

  HeroKilled parseHeroKilled(String record) {
    Matcher matcher = HERO_KILLED.getPattern().matcher(record);
    if (matcher.find()) {
      String actor = matcher.group(6);
      String target = matcher.group(5);
      long totalMilliseconds = parseTimestamp(matcher);

      return new HeroKilled(actor, target, totalMilliseconds);
    }
    return null;
  }

  SpellCast parseSpellCast(Long matchId, String record) {
    Matcher matcher = SPELL_CAST.getPattern().matcher(record);
    if (matcher.find()) {
      String actor = matcher.group(5);
      String target = matcher.group(8);
      String ability = matcher.group(6);
      String abilityLevel = matcher.group(7);
      long totalMilliseconds = parseTimestamp(matcher);

      SpellCast spellCast = new SpellCast(actor, target, ability, Integer.parseInt(abilityLevel),
          totalMilliseconds);
      recentSpells.put(matchId, spellCast);
      return spellCast;
    }
    return null;
  }

  DamageDone parseDamageDone(Long matchId, String record) {
    Matcher matcher = DAMAGE_DONE.getPattern().matcher(record);
    if (matcher.find()) {
      String actor = matcher.group(5);
      String target = matcher.group(6);
      String weapon = matcher.group(7);
      Integer damage = Integer.parseInt(matcher.group(8));
      long totalMilliseconds = parseTimestamp(matcher);

      if (weapon.equals(DOTA_UNKNOWN_INSTRUCTION)) {
        return new DamageDone(actor, target, damage, totalMilliseconds);
      }

      SpellCast lastSpell = recentSpells.get(matchId);
      if ((lastSpell != null) && lastSpell.getActor().equals(actor)
          && (lastSpell.getTarget().equals(DOTA_UNKNOWN_INSTRUCTION)
          || lastSpell.getTarget().equals(target))
          && (lastSpell.getAbility().equals(weapon))
      ) {
        return new DamageDone(actor, target, weapon, lastSpell.getAbilityLevel(), damage,
            totalMilliseconds);
      }

      return new DamageDone(actor, target, weapon, damage, totalMilliseconds);
    }
    return null;
  }

  private long parseTimestamp(Matcher matcher) {
    if (matcher.groupCount() < 4) {
      throw new IllegalArgumentException("Invalid timestamp");
    }
    long hours = Long.parseLong(matcher.group(1));
    long minutes = Long.parseLong(matcher.group(2));
    long seconds = Long.parseLong(matcher.group(3));
    long milliseconds = Long.parseLong(matcher.group(4));

    return hours * 3600000 + minutes * 60000 + seconds * 1000 + milliseconds;
  }
}
