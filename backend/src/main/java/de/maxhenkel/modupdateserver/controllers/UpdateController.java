package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
import de.maxhenkel.modupdateserver.dtos.Update;
import de.maxhenkel.modupdateserver.dtos.UpdateWithoutIdAndMod;
import de.maxhenkel.modupdateserver.dtos.UpdateWithMod;
import de.maxhenkel.modupdateserver.services.UpdateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class UpdateController {

    @Autowired
    private UpdateService updateService;

    @GetMapping("/updates")
    public ResponseEntity<List<UpdateWithMod>> updates(@RequestParam(defaultValue = "16") @Min(1) @Max(128) Integer amount, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
        return new ResponseEntity<>(updateService.getUpdates(amount, page), HttpStatus.OK);
    }

    @GetMapping("/updates/{modID}")
    public ResponseEntity<List<Update>> modUpdates(@PathVariable("modID") String modID, @RequestParam(defaultValue = "16") @Min(1) @Max(128) Integer amount, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
        List<Update> modUpdates = updateService.getModUpdates(modID, amount, page);
        if (modUpdates == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(modUpdates, HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/updates/{modID}")
    public ResponseEntity<?> addUpdate(@PathVariable("modID") String modID, @Valid @RequestBody UpdateWithoutIdAndMod update) {
        if (!updateService.addUpdate(modID, update)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<Update> update(@PathVariable("modID") String modID, @PathVariable("updateID") long updateID) {
        return new ResponseEntity<>(updateService.getUpdate(modID, updateID), HttpStatus.OK);
    }

    @ValidateApiKey
    @PostMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<?> editUpdate(@PathVariable("modID") String modID, @PathVariable("updateID") long updateID, @Valid @RequestBody UpdateWithoutIdAndMod update) {
        updateService.editUpdate(modID, updateID, update);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ValidateApiKey
    @DeleteMapping("/updates/{modID}/{updateID}")
    public ResponseEntity<?> deleteUpdate(@PathVariable("modID") String modID, @PathVariable("updateID") long updateID) {
        updateService.deleteUpdate(modID, updateID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
