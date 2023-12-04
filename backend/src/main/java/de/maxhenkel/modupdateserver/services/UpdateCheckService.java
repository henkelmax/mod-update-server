package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.UpdateCheckResponse;
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
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

@Service
public class UpdateCheckService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Cacheable(value = "updates", cacheManager = "cacheManager")
    public UpdateCheckResponse modUpdatesForLoader(String loader, String modID) {
        Optional<Mod> optionalMod = modRepository.findByModID(modID);
        if (optionalMod.isEmpty()) {
            throw new IllegalArgumentException("Mod does not exist");
        }

        Counter.builder("requests.update_check.cache_miss").tag("loader", loader).tag("modID", modID).register(meterRegistry).increment();

        Mod mod = optionalMod.get();

        UpdateCheckResponse response = new UpdateCheckResponse();
        response.setHomepage(mod.getWebsiteURL());
        Map<String, UpdateCheckResponse.VersionUpdateInfo> versions = new LinkedHashMap<>();
        response.setVersions(versions);

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


        AggregationResults<UpdateEntry> resultLatest = mongoTemplate.aggregate(aggregationLatest, Update.class, UpdateEntry.class);
        AggregationResults<UpdateEntry> resultRecommended = mongoTemplate.aggregate(aggregationRecommended, Update.class, UpdateEntry.class);

        for (UpdateEntry entry : resultLatest.getMappedResults()) {
            UpdateCheckResponse.VersionUpdateInfo versionUpdateInfo = new UpdateCheckResponse.VersionUpdateInfo();
            versionUpdateInfo.setLatest(new UpdateCheckResponse.Version(entry.getVersion(), entry.getUpdateMessages()));
            versions.put(entry.getGameVersion(), versionUpdateInfo);
        }

        for (UpdateEntry entry : resultRecommended.getMappedResults()) {
            UpdateCheckResponse.VersionUpdateInfo info = versions.get(entry.getGameVersion());
            if (info == null) {
                continue;
            }
            info.setRecommended(new UpdateCheckResponse.Version(entry.getVersion(), entry.getUpdateMessages()));
        }

        return response;
    }

    @Data
    private static class UpdateEntry {
        private String gameVersion;
        private String version;
        private String[] updateMessages;
    }

}
