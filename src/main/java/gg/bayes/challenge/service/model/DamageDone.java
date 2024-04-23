package gg.bayes.challenge.service.model;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DamageDone implements CombatLogEvent {

  private String actor;

  private String target;

  private String ability;

  private Integer abilityLevel;

  private String item;

  private Integer damage;

  private Long timestamp;

  public DamageDone(String actor, String target, Integer damage, Long timestamp) {
    this.actor = actor;
    this.target = target;
    this.damage = damage;
    this.timestamp = timestamp;
  }

  public DamageDone(String actor, String target, String item, Integer damage, Long timestamp) {
    this.actor = actor;
    this.target = target;
    this.item = item;
    this.damage = damage;
    this.timestamp = timestamp;
  }

  public DamageDone(String actor, String target, String ability, Integer abilityLevel,
      Integer damage, Long timestamp) {
    this.actor = actor;
    this.target = target;
    this.ability = ability;
    this.abilityLevel = abilityLevel;
    this.damage = damage;
    this.timestamp = timestamp;
  }

  @Override
  public Type getType() {
    return Type.DAMAGE_DONE;
  }
}
