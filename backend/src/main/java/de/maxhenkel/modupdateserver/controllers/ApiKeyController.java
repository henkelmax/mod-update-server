package de.maxhenkel.modupdateserver.controllers;

import com.mongodb.client.result.DeleteResult;
import de.maxhenkel.modupdateserver.annotations.ValidateMasterKey;
import de.maxhenkel.modupdateserver.entities.ApiKey;
import de.maxhenkel.modupdateserver.repositories.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Controller
public class ApiKeyController {

    @Autowired
    ApiKeyRepository apiKeyRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @ValidateMasterKey
    @GetMapping("/apikeys")
    public ResponseEntity<List<ApiKey>> apikeys() {
        return new ResponseEntity<>(apiKeyRepository.findAll(), HttpStatus.OK);
    }

    @ValidateMasterKey
    @PostMapping("/apikeys/add")
    public ResponseEntity<ApiKey> addApikey(@RequestBody @NotNull String[] mods) {
        ApiKey apiKey = apiKeyRepository.save(new ApiKey(UUID.randomUUID(), mods));
        return new ResponseEntity<>(apiKey, HttpStatus.CREATED);
    }

    @ValidateMasterKey
    @DeleteMapping("/apikeys/{apikey}")
    public ResponseEntity<?> addApikey(@PathVariable("apikey") UUID apikey) {
        System.out.println(apikey);
        DeleteResult apiKey = mongoTemplate.remove(Query.query(where("apiKey").is(apikey.toString())), ApiKey.class);
        if (apiKey.getDeletedCount() <= 0L) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ApiKey not found");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
