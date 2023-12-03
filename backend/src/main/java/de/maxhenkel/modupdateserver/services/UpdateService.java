package de.maxhenkel.modupdateserver.services;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.entities.UpdateWithMod;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UpdateService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<UpdateWithMod> getUpdates(int amount, int page) {
        Aggregation agg = newAggregation(
                sort(Sort.Direction.DESC, "publishDate", "gameVersion"),
                skip((long) amount * (long) page),
                limit(amount),
                lookup("mods", "mod", "_id", "mod"),
                unwind("$mod")
        );
        AggregationResults<UpdateWithMod> aggregate = mongoTemplate.aggregate(agg, Update.class, UpdateWithMod.class);
        return aggregate.getMappedResults();
    }

    /**
     * @param modID  the mod id
     * @param amount the amount per page
     * @param page   the page
     * @return <code>null</code> if the mod does not exist
     */
    @Nullable
    public List<Update> getModUpdates(String modID, int amount, int page) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            return null;
        }
        Mod mod = optionalMod.get();
        Query query = new Query();
        query.with(PageRequest.of(page, amount));
        query.addCriteria(Criteria.where("mod").is(mod.getId()));
        query.with(Sort.by(Sort.Direction.DESC, "publishDate", "gameVersion"));
        return mongoTemplate.find(query, Update.class);
    }

    /**
     * @param modID  the mod ID
     * @param update the update
     * @return <code>false</code> if the mod does not exist
     */
    public boolean addUpdate(String modID, Update update) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            return false;
        }
        Mod mod = optionalMod.get();
        update.setMod(mod.getId());
        updateRepository.save(update);
        return true;
    }


    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @return the update
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public Update getUpdate(String modID, ObjectId updateID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        Query query = new Query();
        query.addCriteria(Criteria.where("mod").is(mod.getId()));
        query.addCriteria(Criteria.where("_id").is(updateID));
        Update update = mongoTemplate.findOne(query, Update.class);
        if (update == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        return update;
    }

    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @param update   the update
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public void editUpdate(String modID, ObjectId updateID, Update update) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        Optional<Update> optionalUpdate = updateRepository.findById(updateID);

        if (optionalUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        Update oldUpdate = optionalUpdate.get();

        Query query = Query.query(where("_id").is(oldUpdate.getId())).addCriteria(where("mod").is(mod.getId()));
        org.springframework.data.mongodb.core.query.Update u = new org.springframework.data.mongodb.core.query.Update()
                .set("publishDate", update.getPublishDate())
                .set("gameVersion", update.getGameVersion())
                .set("version", update.getVersion())
                .set("updateMessages", update.getUpdateMessages())
                .set("releaseType", update.getReleaseType())
                .set("tags", update.getTags())
                .set("modLoader", update.getModLoader());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, u, Update.class);

        if (updateResult.getMatchedCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
    }

    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public void deleteUpdate(String modID, ObjectId updateID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        Optional<Update> optionalUpdate = updateRepository.findById(updateID);

        if (optionalUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        Update update = optionalUpdate.get();

        Query query = Query.query(where("_id").is(update.getId())).addCriteria(where("mod").is(mod.getId()));
        DeleteResult deleteResult = mongoTemplate.remove(query, Update.class);

        if (deleteResult.getDeletedCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
    }

}
