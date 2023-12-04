package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.dtos.UpdateCheckResponse;
import de.maxhenkel.modupdateserver.services.ModService;
import de.maxhenkel.modupdateserver.services.UpdateCheckService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class UpdateCheckController {

    @Autowired
    private UpdateCheckService updateCheckService;
    @Autowired
    private ModService modService;
    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/check/{loader:forge|neoforge|fabric|quilt}/{modID}")
    public ResponseEntity<UpdateCheckResponse> forgeModUpdates(@PathVariable("loader") String loader, @PathVariable("modID") String modID) {
        if (!modService.doesModExist(modID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        Counter.builder("requests.update_check").tag("loader", loader).tag("mod_id", modID).register(meterRegistry).increment();
        return new ResponseEntity<>(updateCheckService.modUpdatesForLoader(loader, modID), HttpStatus.OK);
    }

}
