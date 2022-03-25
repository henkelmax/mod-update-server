package de.maxhenkel.modupdateserver.controllers;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.*;

@Controller
public class UpdateController {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("/updates")
    public ResponseEntity<List<UpdateWithMod>> updates(@RequestParam(defaultValue = "16") @Min(1) @Max(128) Integer amount, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
        Aggregation agg = newAggregation(
                sort(Sort.Direction.DESC, "publishDate", "gameVersion"),
                skip((long) amount * (long) page),
                limit(amount),
                lookup("mods", "mod", "_id", "mod"),
                unwind("$mod")
        );
        AggregationResults<UpdateWithMod> aggregate = mongoTemplate.aggregate(agg, Update.class, UpdateWithMod.class);
        return new ResponseEntity<>(aggregate.getMappedResults(), HttpStatus.OK);
    }

    @GetMapping("/updates/{modID}")
    public ResponseEntity<List<Update>> modUpdates(@PathVariable("modID") String modID, @RequestParam(defaultValue = "16") @Min(1) @Max(128) Integer amount, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        Query query = new Query();
        query.with(PageRequest.of(page, amount));
        query.addCriteria(Criteria.where("mod").is(mod.getId()));
        query.with(Sort.by(Sort.Direction.DESC, "publishDate", "gameVersion"));
        List<Update> updates = mongoTemplate.find(query, Update.class);
        return new ResponseEntity<>(updates, HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/updates/{modID}")
    public ResponseEntity<?> addUpdate(@PathVariable("modID") String modID, @Valid @RequestBody Update update) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        update.setMod(mod.getId());
        updateRepository.save(update);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<Update> update(@PathVariable("modID") String modID, @PathVariable("updateID") ObjectId updateID) {
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
        return new ResponseEntity<>(update, HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<?> editUpdate(@PathVariable("modID") String modID, @PathVariable("updateID") ObjectId updateID, @Valid @RequestBody Update update) {
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
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ValidateApiKey
    @DeleteMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<?> deleteUpdate(@PathVariable("modID") String modID, @PathVariable("updateID") ObjectId updateID) {
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
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
