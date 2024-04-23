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
public class ItemPurchased implements CombatLogEvent {

  private String actor;

  private String item;

  private Long timestamp;

  @Override
  public Type getType() {
    return Type.ITEM_PURCHASED;
  }
}
