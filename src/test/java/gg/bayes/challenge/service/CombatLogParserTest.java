package gg.bayes.challenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gg.bayes.challenge.service.model.DamageDone;
import gg.bayes.challenge.service.model.HeroKilled;
import gg.bayes.challenge.service.model.ItemPurchased;
import gg.bayes.challenge.service.model.SpellCast;
import org.junit.jupiter.api.Test;

public class CombatLogParserTest {

  private final CombatLogParser combatLogParser = new CombatLogParser();

  @Test
  void parseItemPurchaseTest() {
    String logRecord = "[00:08:46.759] npc_dota_hero_dragon_knight buys item item_quelling_blade";
    ItemPurchased itemPurchased = combatLogParser.parseItemPurchase(logRecord);
    assertEquals("dragon_knight", itemPurchased.getActor());
    assertEquals("quelling_blade", itemPurchased.getItem());
    assertEquals(526759, itemPurchased.getTimestamp());
  }

  @Test
  void parseHeroKilledTest() {
    String logRecord = "[00:11:20.322] npc_dota_hero_rubick is killed by npc_dota_hero_pangolier";
    HeroKilled heroKilled = combatLogParser.parseHeroKilled(logRecord);
    assertEquals("pangolier", heroKilled.getActor());
    assertEquals("rubick", heroKilled.getTarget());
    assertEquals(680322, heroKilled.getTimestamp());
  }

  @Test
  void parseSpellCastTest() {
    String logRecord =
        "[00:12:23.740] npc_dota_hero_bane casts ability bane_brain_sap (lvl 1) on npc_dota_hero_puck";
    SpellCast spellCast = combatLogParser.parseSpellCast(1L, logRecord);
    assertEquals("bane", spellCast.getActor());
    assertEquals("hero_puck", spellCast.getTarget());
    assertEquals("bane_brain_sap", spellCast.getAbility());
    assertEquals(1, spellCast.getAbilityLevel());
    assertEquals(743740, spellCast.getTimestamp());

    logRecord =
        "[00:12:39.502] npc_dota_hero_abyssal_underlord casts ability abyssal_underlord_firestorm (lvl 1) on dota_unknown";
    spellCast = combatLogParser.parseSpellCast(1L, logRecord);
    assertEquals("abyssal_underlord", spellCast.getActor());
    assertEquals("unknown", spellCast.getTarget());
    assertEquals("abyssal_underlord_firestorm", spellCast.getAbility());
    assertEquals(1, spellCast.getAbilityLevel());
    assertEquals(759502, spellCast.getTimestamp());
  }

  @Test
  void parseDamageDoneTest() {
    String logRecord =
        "[00:12:25.706] npc_dota_hero_pangolier hits npc_dota_hero_rubick with dota_unknown for 32 damage (621->589)";
    DamageDone damageDone = combatLogParser.parseDamageDone(1L, logRecord);
    assertEquals("pangolier", damageDone.getActor());
    assertEquals("hero_rubick", damageDone.getTarget());
    assertEquals(32, damageDone.getDamage());
    assertNull(damageDone.getItem());
    assertNull(damageDone.getAbility());
    assertNull(damageDone.getAbilityLevel());
    assertEquals(745706, damageDone.getTimestamp());

    logRecord = "[00:12:23.740] npc_dota_hero_bane hits npc_dota_hero_puck with bane_brain_sap for 75 damage (139->64)";
    damageDone = combatLogParser.parseDamageDone(1L, logRecord);
    assertEquals("bane", damageDone.getActor());
    assertEquals("hero_puck", damageDone.getTarget());
    assertEquals(75, damageDone.getDamage());
    assertEquals("bane_brain_sap", damageDone.getItem());
    assertNull(damageDone.getAbility());
    assertNull(damageDone.getAbilityLevel());
    assertEquals(743740, damageDone.getTimestamp());

    combatLogParser.parseSpellCast(1L, "[00:12:39.502] npc_dota_hero_abyssal_underlord casts ability abyssal_underlord_firestorm (lvl 1) on dota_unknown");
    logRecord = "[00:12:39.536] npc_dota_hero_abyssal_underlord hits npc_dota_hero_bloodseeker with abyssal_underlord_firestorm for 18 damage (693->675)";
    damageDone = combatLogParser.parseDamageDone(1L, logRecord);
    assertEquals("abyssal_underlord", damageDone.getActor());
    assertEquals("hero_bloodseeker", damageDone.getTarget());
    assertEquals(18, damageDone.getDamage());
    assertNull(damageDone.getItem());
    assertEquals("abyssal_underlord_firestorm", damageDone.getAbility());
    assertEquals(1, damageDone.getAbilityLevel());
    assertEquals(759536, damageDone.getTimestamp());
  }
}
