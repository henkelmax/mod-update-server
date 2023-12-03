package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.services.ForgeUpdateService;
import de.maxhenkel.modupdateserver.services.ModService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
public class ForgeController {

    @Autowired
    private ForgeUpdateService forgeUpdateService;
    @Autowired
    private ModService modService;
    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/forge/{modID}")
    public ResponseEntity<Map<String, Object>> forgeModUpdates(@PathVariable("modID") String modID) {
        return modUpdatesForLoader("forge", modID);
    }

    @GetMapping("/neoforge/{modID}")
    public ResponseEntity<Map<String, Object>> neoForgeModUpdates(@PathVariable("modID") String modID) {
        return modUpdatesForLoader("neoforge", modID);
    }

    public ResponseEntity<Map<String, Object>> modUpdatesForLoader(String loader, String modID) {
        if (!modService.doesModExist(modID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Counter.builder("requests.update_check").tag("loader", loader).tag("mod_id", modID).register(meterRegistry).increment();
        return forgeUpdateService.modUpdatesForLoader(loader, modID);
    }

}
