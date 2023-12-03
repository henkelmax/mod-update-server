package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LatestUpdatesService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean doesModExist(String modID) {
        return modRepository.findByModID(modID).isPresent();
    }

    public Collection<Update> getModUpdates(String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            return Collections.emptyList();
        }
        Mod mod = optionalMod.get();

        Query query = new Query();
        query.addCriteria(Criteria.where("mod").is(mod.getId()));
        query.with(Sort.by(Sort.Direction.ASC, "publishDate"));
        List<Update> updates = mongoTemplate.find(query, Update.class);

        Map<String, Update> versions = new HashMap<>();

        for (Update update : updates) {
            versions.put(update.getGameVersion(), update);
        }

        return versions.values();
    }

}
