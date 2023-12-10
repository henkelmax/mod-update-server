package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.ModEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ModRepository extends CrudRepository<ModEntity, String> {

    List<ModEntity> findAll();

}
