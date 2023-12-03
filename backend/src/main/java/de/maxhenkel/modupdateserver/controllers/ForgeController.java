package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.services.ForgeUpdateService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ForgeController {

    @Autowired
    private ForgeUpdateService forgeUpdateService;

    private final Counter totalUpdateCheckCounter;
    private final Counter forgeUpdateCheckCounter;
    private final Counter neoforgeUpdateCheckCounter;

    public ForgeController(MeterRegistry meterRegistry) {
        totalUpdateCheckCounter = Counter.builder("update_check.total")
                .register(meterRegistry);
        forgeUpdateCheckCounter = Counter.builder("update_check.loader.forge")
                .register(meterRegistry);
        neoforgeUpdateCheckCounter = Counter.builder("update_check.loader.neoforge")
                .register(meterRegistry);
    }

    @GetMapping("/forge/{modID}")
    public ResponseEntity<?> forgeModUpdates(@PathVariable("modID") String modID) {
        totalUpdateCheckCounter.increment();
        forgeUpdateCheckCounter.increment();
        return forgeUpdateService.modUpdatesForLoader("forge", modID);
    }

    @GetMapping("/neoforge/{modID}")
    public ResponseEntity<?> neoForgeModUpdates(@PathVariable("modID") String modID) {
        totalUpdateCheckCounter.increment();
        neoforgeUpdateCheckCounter.increment();
        return forgeUpdateService.modUpdatesForLoader("neoforge", modID);
    }

}
