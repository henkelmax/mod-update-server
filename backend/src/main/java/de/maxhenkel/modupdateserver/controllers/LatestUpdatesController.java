package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.services.LatestUpdatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class LatestUpdatesController {

    @Autowired
    private LatestUpdatesService latestUpdatesService;

    @Cacheable(value = "latest", cacheManager = "cacheManager")
    @GetMapping("/latest/{modID}")
    public ResponseEntity<?> modUpdates(@PathVariable("modID") String modID) {
        if (!latestUpdatesService.doesModExist(modID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(latestUpdatesService.getModUpdates(modID), HttpStatus.OK);
    }

}
