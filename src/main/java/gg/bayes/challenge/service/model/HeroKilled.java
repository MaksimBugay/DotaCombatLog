package gg.bayes.challenge.service.model;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HeroKilled implements CombatLogEvent{

  private String actor;

  private String target;

  private Long timestamp;

  @Override
  public Type getType() {
    return Type.HERO_KILLED;
  }
}
