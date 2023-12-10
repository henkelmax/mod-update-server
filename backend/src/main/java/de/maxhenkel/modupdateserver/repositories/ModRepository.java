package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.ModAndUpdateCount;
import de.maxhenkel.modupdateserver.entities.ModEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ModRepository extends CrudRepository<ModEntity, String> {

    List<ModEntity> findAll();

    @Query("SELECT new de.maxhenkel.modupdateserver.entities.ModAndUpdateCount(m, COUNT(u.id)) FROM update u, mod m WHERE m.modID = :modId AND m.modID = u.mod GROUP BY u.mod")
    Optional<ModAndUpdateCount> getModWithUpdateCount(String modId);

}
