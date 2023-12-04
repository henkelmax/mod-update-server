package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.entities.Mod;
import de.maxhenkel.modupdateserver.entities.Update;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@Service
public class ForgeUpdateService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Cacheable(value = "mod_updates", cacheManager = "cacheManager")
    public ResponseEntity<Map<String, Object>> modUpdatesForLoader(String loader, String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }

        Counter.builder("requests.update_check.cache_miss").tag("loader", loader).tag("modID", modID).register(meterRegistry).increment();

        Mod mod = optionalMod.get();

        Map<String, Object> forgeFormat = new HashMap<>();
        Map<String, String> promos = new HashMap<>();

        MatchOperation matchByMod = Aggregation.match(Criteria.where("mod").is(mod.getId()).and("modLoader").is(loader));

        SortOperation sortByPublishDateDesc = Aggregation.sort(Sort.by(Sort.Order.desc("publishDate")));

        GroupOperation groupByGameVersion = group("gameVersion")
                .first("gameVersion").as("gameVersion")
                .first("version").as("version")
                .first("updateMessages").as("updateMessages");

        Aggregation aggregationLatest = Aggregation.newAggregation(
                matchByMod,
                sortByPublishDateDesc,
                groupByGameVersion
        );

        Aggregation aggregationRecommended = Aggregation.newAggregation(
                matchByMod,
                sortByPublishDateDesc,
                Aggregation.match(Criteria.where("tags").in("recommended")),
                groupByGameVersion
        );

        AggregationResults<ForgeUpdateEntry> resultLatest = mongoTemplate.aggregate(aggregationLatest, Update.class, ForgeUpdateEntry.class);
        AggregationResults<ForgeUpdateEntry> resultRecommended = mongoTemplate.aggregate(aggregationRecommended, Update.class, ForgeUpdateEntry.class);

        for (ForgeUpdateEntry entry : resultLatest.getMappedResults()) {
            Map<String, String> o = (Map<String, String>) forgeFormat.computeIfAbsent(entry.getGameVersion(), e -> new HashMap<String, String>());
            o.put(entry.getVersion(), StringUtils.join(entry.getUpdateMessages(), "\n"));
            promos.put("%s-latest".formatted(entry.getGameVersion()), entry.getVersion());
        }

        for (ForgeUpdateEntry entry : resultRecommended.getMappedResults()) {
            Map<String, String> o = (Map<String, String>) forgeFormat.computeIfAbsent(entry.getGameVersion(), e -> new HashMap<String, String>());
            o.put(entry.getVersion(), StringUtils.join(entry.getUpdateMessages(), "\n"));
            promos.put("%s-recommended".formatted(entry.getGameVersion()), entry.getVersion());
        }

        forgeFormat.put("promos", promos);
        forgeFormat.put("homepage", mod.getWebsiteURL());
        return new ResponseEntity<>(forgeFormat, HttpStatus.OK);
    }

    @Data
    private static class ForgeUpdateEntry {
        private String gameVersion;
        private String version;
        private String[] updateMessages;
    }

}
