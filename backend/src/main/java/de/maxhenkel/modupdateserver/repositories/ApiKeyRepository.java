package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.ApiKey;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends MongoRepository<ApiKey, ObjectId> {

    Optional<ApiKey> findByApiKey(UUID apikey);

}
