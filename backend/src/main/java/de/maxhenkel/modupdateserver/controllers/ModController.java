package de.maxhenkel.modupdateserver.controllers;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.ModWithUpdateCount;
import de.maxhenkel.modupdateserver.entities.ModWithoutModId;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Controller
public class ModController {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("/mods")
    public ResponseEntity<List<ModWithUpdateCount>> getMods() {
        Aggregation agg = newAggregation(
                l ->
                        new Document("$lookup",
                                new Document("from", "updates")
                                        .append("let", new Document("id", "$_id"))
                                        .append("pipeline",
                                                List.of(
                                                        new Document("$match", new Document("$expr", new Document("$eq", Arrays.asList("$mod", "$$id")))),
                                                        new Document("$count", "count")
                                                )
                                        )
                                        .append("as", "updates")
                        ),
                unwind("$updates", Boolean.TRUE),
                l ->
                        new Document("$addFields",
                                new Document("updateCount",
                                        new Document("$ifNull", List.of(
                                                new Document("$arrayElemAt", List.of(
                                                        new Document("$map",
                                                                new Document("input", new Document("$objectToArray", "$updates"))
                                                                        .append("as", "c")
                                                                        .append("in", "$$c.v")
                                                        ),
                                                        0
                                                )),
                                                0
                                        ))
                                )
                        ),
                l ->
                        new Document("$unset", "updates")
        );

        AggregationResults<ModWithUpdateCount> aggregate = mongoTemplate.aggregate(agg, Mod.class, ModWithUpdateCount.class);
        return new ResponseEntity<>(aggregate.getMappedResults(), HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/mods/add")
    public ResponseEntity<?> addMod(@Valid @RequestBody Mod mod) {
        Optional<Mod> optionalMod = modRepository.findByModID(mod.getModID());
        if (optionalMod.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mod already exists");
        }
        modRepository.save(mod);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ValidateApiKey
    @PostMapping("/mods/edit/{modID}")
    public ResponseEntity<?> editUpdate(@PathVariable("modID") String modID, @Valid @RequestBody ModWithoutModId mod) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod oldMod = optionalMod.get();

        Query query = Query.query(where("_id").is(oldMod.getId()));
        org.springframework.data.mongodb.core.query.Update u = new org.springframework.data.mongodb.core.query.Update()
                .set("name", mod.getName())
                .set("description", mod.getDescription())
                .set("websiteURL", mod.getWebsiteURL())
                .set("downloadURL", mod.getDownloadURL())
                .set("issueURL", mod.getIssueURL());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, u, Mod.class);

        if (updateResult.getMatchedCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mods/{modID}")
    public ResponseEntity<ModWithUpdateCount> getMod(@PathVariable("modID") String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();
        int updates = updateRepository.countUpdatesByMod(mod.getId());
        return new ResponseEntity<>(new ModWithUpdateCount(mod, updates), HttpStatus.OK);
    }

    @ValidateApiKey
    @DeleteMapping("/mods/{modID}")
    public ResponseEntity<?> deleteUpdate(@PathVariable("modID") String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();

        Query query = Query.query(where("modID").is(modID));
        DeleteResult deleteResult = mongoTemplate.remove(query, Mod.class);

        if (deleteResult.getDeletedCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }

        Query updateQuery = Query.query(where("mod").is(mod.getId()));
        DeleteResult updateDeleteResult = mongoTemplate.remove(updateQuery, Update.class);

        if (!updateDeleteResult.wasAcknowledged()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete updates for mod");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
