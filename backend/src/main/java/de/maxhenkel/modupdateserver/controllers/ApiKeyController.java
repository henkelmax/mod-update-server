package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.annotations.ValidateMasterKey;
import de.maxhenkel.modupdateserver.entities.ApiKey;
import de.maxhenkel.modupdateserver.services.ApiKeyService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Controller
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @ValidateMasterKey
    @GetMapping("/apikeys")
    public ResponseEntity<List<ApiKey>> apikeys() {
        return new ResponseEntity<>(apiKeyService.getApikeys(), HttpStatus.OK);
    }

    @ValidateMasterKey
    @PostMapping("/apikeys/add")
    public ResponseEntity<ApiKey> addApikey(@RequestBody @NotNull String[] mods) {
        return new ResponseEntity<>(apiKeyService.addApikey(mods), HttpStatus.CREATED);
    }

    @ValidateMasterKey
    @DeleteMapping("/apikeys/{apikey}")
    public ResponseEntity<?> removeApikey(@PathVariable("apikey") UUID apikey) {
        if (!apiKeyService.removeApikey(apikey)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ApiKey not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
