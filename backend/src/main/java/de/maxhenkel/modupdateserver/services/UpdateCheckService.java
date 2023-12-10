package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.UpdateCheckResponse;
import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.entities.UpdateEntity;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UpdateCheckService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Cacheable(value = "updates", cacheManager = "cacheManager")
    public UpdateCheckResponse modUpdatesForLoader(String loader, String modID) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            throw new IllegalArgumentException("Mod does not exist");
        }

        Counter.builder("requests.update_check.cache_miss").tag("loader", loader).tag("modID", modID).register(meterRegistry).increment();

        ModEntity mod = optionalMod.get();

        UpdateCheckResponse response = new UpdateCheckResponse();
        response.setHomepage(mod.getWebsiteURL());
        Map<String, UpdateCheckResponse.VersionUpdateInfo> versions = new LinkedHashMap<>();
        response.setVersions(versions);

        List<UpdateEntity> latest = updateRepository.getLatestUpdateEntries(mod.getModID(), loader);
        List<UpdateEntity> recommended = updateRepository.getRecommendedUpdateEntries(mod.getModID(), loader);

        for (UpdateEntity entry : latest) {
            UpdateCheckResponse.VersionUpdateInfo versionUpdateInfo = new UpdateCheckResponse.VersionUpdateInfo();
            versionUpdateInfo.setLatest(new UpdateCheckResponse.Version(entry.getVersion(), entry.getUpdateMessages()));
            versions.put(entry.getGameVersion(), versionUpdateInfo);
        }

        for (UpdateEntity entry : recommended) {
            UpdateCheckResponse.VersionUpdateInfo info = versions.get(entry.getGameVersion());
            if (info == null) {
                continue;
            }
            info.setRecommended(new UpdateCheckResponse.Version(entry.getVersion(), entry.getUpdateMessages()));
        }

        return response;
    }

}
