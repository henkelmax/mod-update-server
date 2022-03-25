package de.maxhenkel.modupdateserver.repositories;

import de.maxhenkel.modupdateserver.entities.Update;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UpdateRepository extends MongoRepository<Update, ObjectId> {

    int countUpdatesByMod(ObjectId mod);

}
