package de.maxhenkel.modupdateserver.controllers;

import de.maxhenkel.modupdateserver.annotations.ValidateApiKey;
import de.maxhenkel.modupdateserver.annotations.ValidateMasterKey;
import de.maxhenkel.modupdateserver.dtos.Backup;
import de.maxhenkel.modupdateserver.services.BackupService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class BackupController {

    @Autowired
    private BackupService backupService;

    @ValidateApiKey
    @GetMapping("/backup")
    public ResponseEntity<Backup> backup() {
        return new ResponseEntity<>(backupService.getBackup(), HttpStatus.OK);
    }

    @ValidateMasterKey
    @PostMapping("/restore")
    public ResponseEntity<?> restore(@RequestBody @NotNull Backup backup) {
        if (!backupService.restore(backup)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Database not empty");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
