package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class LatestController {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Cacheable(value = "latest", cacheManager = "cacheManager")
    @GetMapping("/latest/{modID}")
    public ResponseEntity<?> modUpdates(@PathVariable("modID") String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();

        Query query = new Query();
        query.addCriteria(Criteria.where("mod").is(mod.getId()));
        query.with(Sort.by(Sort.Direction.ASC, "publishDate"));
        List<Update> updates = mongoTemplate.find(query, Update.class);

        Map<String, Object> versions = new HashMap<>();

        for (Update update : updates) {
            versions.put(update.getGameVersion(), update);
        }

        return new ResponseEntity<>(versions.values(), HttpStatus.OK);
    }

}
