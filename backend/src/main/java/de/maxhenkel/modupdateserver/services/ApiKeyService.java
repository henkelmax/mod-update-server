package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.entities.ApiKey;
import de.maxhenkel.modupdateserver.repositories.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ApiKey> getApikeys() {
        return apiKeyRepository.findAll();
    }

    public ApiKey addApikey(String[] mods) {
        return apiKeyRepository.save(new ApiKey(UUID.randomUUID(), mods));
    }

    /**
     * @param apikey the API key
     * @return if the API key was found and deleted
     */
    public boolean removeApikey(UUID apikey) {
        return mongoTemplate.remove(Query.query(where("apiKey").is(apikey.toString())), ApiKey.class).getDeletedCount() > 0L;
    }

}
