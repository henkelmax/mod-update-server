package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.UpdateEntity;
import de.maxhenkel.modupdateserver.entities.ModAndUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UpdateRepository extends CrudRepository<UpdateEntity, Long> {

    Page<UpdateEntity> findAllByModOrderByPublishDateDesc(String modId, Pageable pageable);

    Optional<UpdateEntity> findFirstByModAndId(String modId, Long id);

    @Query("SELECT new de.maxhenkel.modupdateserver.entities.ModAndUpdate(m, u) FROM update u, mod m WHERE u.mod = m.modID ORDER BY u.publishDate DESC")
    Page<ModAndUpdate> getAllUpdatesWithMod(Pageable pageable);

    List<UpdateEntity> getAllByMod(String mod);

    void removeAllByMod(String modId);

    @Query(value = "WITH latest_updates AS (SELECT u.*, ROW_NUMBER() OVER (PARTITION BY u.game_version ORDER BY u.publish_date DESC) AS row_num FROM update u WHERE u.mod = :modId AND u.mod_loader = :loader) SELECT * FROM latest_updates WHERE row_num = 1 ORDER BY publish_date DESC;", nativeQuery = true)
    List<UpdateEntity> getLatestUpdateEntries(String modId, String loader);

    @Query(value = "WITH latest_updates AS (SELECT u.*, ROW_NUMBER() OVER (PARTITION BY u.game_version ORDER BY u.publish_date DESC) AS row_num FROM update u WHERE u.mod = :modId AND u.mod_loader = :loader AND 'recommended' = ANY(u.tags)) SELECT * FROM latest_updates WHERE row_num = 1 ORDER BY publish_date DESC;", nativeQuery = true)
    List<UpdateEntity> getRecommendedUpdateEntries(String modId, String loader);
}
