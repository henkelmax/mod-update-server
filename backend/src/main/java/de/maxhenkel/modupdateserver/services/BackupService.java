package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.entities.Backup;
import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.ModWithUpdates;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class BackupService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Backup getBackup() {
        Aggregation agg = newAggregation(
                lookup("updates", "_id", "mod", "updates")
        );

        AggregationResults<ModWithUpdates> aggregate = mongoTemplate.aggregate(agg, Mod.class, ModWithUpdates.class);
        return new Backup(aggregate.getMappedResults());
    }

    /**
     * @param backup the backup
     * @return <code>false</code> if the database is not empty
     */
    @Transactional
    public boolean restore(Backup backup) {
        if (modRepository.count() > 0L || updateRepository.count() > 0L) {
            return false;
        }
        mongoTemplate.insertAll(backup.getMods().stream().map(Mod::new).toList());
        mongoTemplate.insertAll(backup.getMods().stream().flatMap(modWithUpdates -> modWithUpdates.getUpdates().stream()).toList());
        return true;
    }

}
