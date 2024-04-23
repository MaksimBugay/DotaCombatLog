package gg.bayes.challenge.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class HeroDamage {
    String target;
    @JsonProperty("damage_instances")
    Integer damageInstances;
    @JsonProperty("total_damage")
    Integer totalDamage;
}
