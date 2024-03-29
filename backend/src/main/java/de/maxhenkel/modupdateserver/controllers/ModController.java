package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
import de.maxhenkel.modupdateserver.dtos.Mod;
import de.maxhenkel.modupdateserver.dtos.ModWithUpdateCount;
import de.maxhenkel.modupdateserver.dtos.ModWithoutModId;
import de.maxhenkel.modupdateserver.services.ModService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Controller
public class ModController {

    @Autowired
    private ModService modService;

    @GetMapping("/mods")
    public ResponseEntity<List<Mod>> getMods() {
        return new ResponseEntity<>(modService.getMods(), HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/mods/add")
    public ResponseEntity<?> addMod(@Valid @RequestBody Mod mod) {
        if (!modService.addMod(mod)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mod already exists");
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ValidateApiKey
    @PostMapping("/mods/edit/{modID}")
    public ResponseEntity<?> editMod(@PathVariable("modID") String modID, @Valid @RequestBody ModWithoutModId mod) {
        if (!modService.editMod(modID, mod)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mods/{modID}")
    public ResponseEntity<ModWithUpdateCount> getMod(@PathVariable("modID") String modID) {
        Optional<ModWithUpdateCount> optionalMod = modService.getMod(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(optionalMod.get(), HttpStatus.OK);
    }

    @ValidateApiKey
    @DeleteMapping("/mods/{modID}")
    public ResponseEntity<?> deleteMod(@PathVariable("modID") String modID) {
        modService.deleteMod(modID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
