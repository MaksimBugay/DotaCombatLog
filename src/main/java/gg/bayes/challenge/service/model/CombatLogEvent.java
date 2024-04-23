package gg.bayes.challenge.service.model;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type;

public interface CombatLogEvent {

  Long getTimestamp();

  Type getType();
}
