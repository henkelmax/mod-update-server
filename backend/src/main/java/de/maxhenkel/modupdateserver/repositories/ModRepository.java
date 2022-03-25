package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.Mod;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ModRepository extends MongoRepository<Mod, ObjectId> {

    Optional<Mod> findByModID(String modID);

}
