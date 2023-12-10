package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.ApiKeyEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface ApiKeyRepository extends CrudRepository<ApiKeyEntity, UUID> {

    List<ApiKeyEntity> findAll();

}
