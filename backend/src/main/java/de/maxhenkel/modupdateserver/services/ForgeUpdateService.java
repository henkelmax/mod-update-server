package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.entities.UpdateEntity;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.*;

@Service
public class ForgeUpdateService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Cacheable(value = "forge_updates", cacheManager = "cacheManager")
    public Map<String, Object> modUpdatesForLoader(String loader, String modID) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            return Collections.emptyMap();
        }

        Counter.builder("requests.update_check.cache_miss").tag("loader", loader).tag("modID", modID).register(meterRegistry).increment();

        ModEntity mod = optionalMod.get();

        Map<String, Object> forgeFormat = new HashMap<>();
        Map<String, String> promos = new HashMap<>();

        List<UpdateEntity> latest = updateRepository.getLatestUpdateEntries(mod.getModID(), loader);
        List<UpdateEntity> recommended = updateRepository.getRecommendedUpdateEntries(mod.getModID(), loader);

        for (UpdateEntity entity : latest) {
            Map<String, String> o = (Map<String, String>) forgeFormat.computeIfAbsent(entity.getGameVersion(), e -> new HashMap<String, String>());
            o.put(entity.getVersion(), StringUtils.join(entity.getUpdateMessages(), "\n"));
            promos.put("%s-latest".formatted(entity.getGameVersion()), entity.getVersion());
        }

        for (UpdateEntity entity : recommended) {
            Map<String, String> o = (Map<String, String>) forgeFormat.computeIfAbsent(entity.getGameVersion(), e -> new HashMap<String, String>());
            o.put(entity.getVersion(), StringUtils.join(entity.getUpdateMessages(), "\n"));
            promos.put("%s-recommended".formatted(entity.getGameVersion()), entity.getVersion());
        }

        forgeFormat.put("promos", promos);
        forgeFormat.put("homepage", mod.getWebsiteURL());
        return forgeFormat;
    }

}
