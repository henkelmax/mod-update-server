package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Controller
public class ForgeController {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Cacheable(value = "forge", cacheManager = "cacheManager")
    @GetMapping("/forge/{modID}")
    public ResponseEntity<?> modUpdates(@PathVariable("modID") String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Mod mod = optionalMod.get();

        Map<String, Object> forgeFormat = new HashMap<>();
        Map<String, String> promos = new HashMap<>();

        Aggregation agg = newAggregation(
                match(Criteria.where("mod").is(mod.getId())),
                match(Criteria.where("modLoader").is("forge")),
                sort(Sort.Direction.ASC, "publishDate")
        );

        AggregationResults<Update> aggregate = mongoTemplate.aggregate(agg, Update.class, Update.class);

        for (Update update : aggregate.getMappedResults()) {
            Map<String, String> o = (Map<String, String>) forgeFormat.get(update.getGameVersion());
            if (o == null) {
                o = new HashMap<>();
                forgeFormat.put(update.getGameVersion(), o);
            }

            o.put(update.getVersion(), StringUtils.join(update.getUpdateMessages(), "\n"));

            if (Arrays.asList(update.getTags()).contains("recommended")) {
                promos.put("%s-recommended".formatted(update.getGameVersion()), update.getVersion());
            }
            promos.put("%s-latest".formatted(update.getGameVersion()), update.getVersion());
        }

        forgeFormat.put("promos", promos);
        forgeFormat.put("homepage", mod.getWebsiteURL());
        return new ResponseEntity<>(forgeFormat, HttpStatus.OK);
    }

}
