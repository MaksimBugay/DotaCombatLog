package gg.bayes.challenge.persistence.repository;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CombatLogEntryRepository extends JpaRepository<CombatLogEntryEntity, Long> {

  @Query("SELECT c.actor as actor, COUNT(c) as count FROM CombatLogEntryEntity c WHERE c.match.id = :matchId AND c.type = :type GROUP BY c.actor")
  List<Map<String, Object>> findByMatchIdAndTypeGroupedByActor(@Param("matchId") Long matchId, @Param("type") CombatLogEntryEntity.Type type);

  @Query("SELECT c FROM CombatLogEntryEntity c WHERE c.match.id = :matchId AND c.actor = :actor AND c.type = :type")
  List<CombatLogEntryEntity> findByMatchIdAndActor(@Param("matchId") Long matchId, @Param("actor") String actor, @Param("type") CombatLogEntryEntity.Type type);

  @Query("SELECT c.ability as ability, COUNT(c) as count FROM CombatLogEntryEntity c WHERE c.match.id = :matchId AND c.actor = :actor AND c.type = :type GROUP BY c.ability")
  List<Map<String, Object>> findByMatchIdAndActorAndTypeGroupedByAbility(@Param("matchId") Long matchId, @Param("actor") String actor, @Param("type") CombatLogEntryEntity.Type type);

  @Query("SELECT c.target as target, COUNT(c) as count, SUM(c.damage) as sum FROM CombatLogEntryEntity c WHERE c.match.id = :matchId AND c.actor = :actor AND c.type = :type AND c.target LIKE 'hero$_%' ESCAPE '$' GROUP BY c.target")
  List<Map<String, Object>> findByMatchIdAndActorAndTypeGroupedByTarget(@Param("matchId") Long matchId, @Param("actor") String actor, @Param("type") CombatLogEntryEntity.Type type);

}
