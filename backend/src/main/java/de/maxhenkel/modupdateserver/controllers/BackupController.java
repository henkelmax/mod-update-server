package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
import de.maxhenkel.modupdateserver.annotations.ValidateMasterKey;
import de.maxhenkel.modupdateserver.entities.Backup;
import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.ModWithUpdates;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Controller
public class BackupController {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @ValidateApiKey
    @GetMapping("/backup")
    public ResponseEntity<Backup> backup() {
        Aggregation agg = newAggregation(
                lookup("updates", "_id", "mod", "updates")
        );

        AggregationResults<ModWithUpdates> aggregate = mongoTemplate.aggregate(agg, Mod.class, ModWithUpdates.class);
        return new ResponseEntity<>(new Backup(aggregate.getMappedResults()), HttpStatus.OK);
    }

    @ValidateMasterKey
    @PostMapping("/restore")
    @Transactional
    public ResponseEntity<?> restore(@RequestBody @NotNull Backup backup) {
        if (modRepository.count() > 0L || updateRepository.count() > 0L) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Database not empty");
        }

        mongoTemplate.insertAll(backup.getMods().stream().map(Mod::new).toList());
        mongoTemplate.insertAll(backup.getMods().stream().flatMap(modWithUpdates -> modWithUpdates.getUpdates().stream()).toList());

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
