package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.services.ForgeUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ForgeController {

    @Autowired
    private ForgeUpdateService forgeUpdateService;

    @GetMapping("/forge/{modID}")
    public ResponseEntity<?> forgeModUpdates(@PathVariable("modID") String modID) {
        return forgeUpdateService.modUpdatesForLoader("forge", modID);
    }

    @GetMapping("/neoforge/{modID}")
    public ResponseEntity<?> neoForgeModUpdates(@PathVariable("modID") String modID) {
        return forgeUpdateService.modUpdatesForLoader("neoforge", modID);
    }

}
