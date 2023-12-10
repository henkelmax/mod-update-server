package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.ApiKey;
import de.maxhenkel.modupdateserver.entities.ApiKeyEntity;
import de.maxhenkel.modupdateserver.repositories.ApiKeyRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<ApiKey> getApikeys() {
        return apiKeyRepository.findAll().stream().map(apiKeyEntity -> modelMapper.map(apiKeyEntity, ApiKey.class)).toList();
    }

    public Optional<ApiKey> getApikey(UUID apiKey) {
        return apiKeyRepository.findById(apiKey).map(apiKeyEntity -> modelMapper.map(apiKeyEntity, ApiKey.class));
    }

    public ApiKey addApikey(String[] mods) {
        return modelMapper.map(apiKeyRepository.save(new ApiKeyEntity(UUID.randomUUID(), mods)), ApiKey.class);
    }

    /**
     * @param apikey the API key to remove
     * @return if the API key was found and deleted
     */
    public boolean removeApikey(UUID apikey) {
        if (!apiKeyRepository.existsById(apikey)) {
            return false;
        }
        apiKeyRepository.deleteById(apikey);
        return true;
    }

}
