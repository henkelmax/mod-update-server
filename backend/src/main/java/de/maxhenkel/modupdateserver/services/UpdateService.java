package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.Mod;
import de.maxhenkel.modupdateserver.dtos.Update;
import de.maxhenkel.modupdateserver.dtos.UpdateWithoutIdAndMod;
import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.entities.UpdateEntity;
import de.maxhenkel.modupdateserver.entities.ModAndUpdate;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<de.maxhenkel.modupdateserver.dtos.UpdateWithMod> getUpdates(int amount, int page) {
        Page<ModAndUpdate> p = updateRepository.getAllUpdatesWithMod(PageRequest.of(page, amount));
        return p.get().map(e -> {
            de.maxhenkel.modupdateserver.dtos.UpdateWithMod updateWithMod = modelMapper.map(e.getUpdate(), de.maxhenkel.modupdateserver.dtos.UpdateWithMod.class);
            updateWithMod.setMod(modelMapper.map(e.getMod(), Mod.class));
            return updateWithMod;
        }).toList();
    }

    /**
     * @param modID  the mod id
     * @param amount the amount per page
     * @param page   the page
     * @return <code>null</code> if the mod does not exist
     */
    @Nullable
    public List<Update> getModUpdates(String modID, int amount, int page) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            return null;
        }
        ModEntity mod = optionalMod.get();

        Page<UpdateEntity> p = updateRepository.findAllByModOrderByPublishDateDesc(mod.getModID(), PageRequest.of(page, amount));

        return p.get().map(updateEntity -> modelMapper.map(updateEntity, Update.class)).toList();
    }

    /**
     * @param modID  the mod ID
     * @param update the update
     * @return <code>false</code> if the mod does not exist
     */
    @Transactional
    public boolean addUpdate(String modID, UpdateWithoutIdAndMod update) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            return false;
        }
        ModEntity mod = optionalMod.get();
        UpdateEntity updateEntity = modelMapper.map(update, UpdateEntity.class);
        updateEntity.setMod(mod.getModID());
        updateEntity.setId(null);
        updateRepository.save(updateEntity);
        return true;
    }


    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @return the update
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public Update getUpdate(String modID, long updateID) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        ModEntity mod = optionalMod.get();
        Optional<UpdateEntity> optionalUpdate = updateRepository.findFirstByModAndId(mod.getModID(), updateID);
        if (optionalUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        return optionalUpdate.map(updateEntity -> modelMapper.map(updateEntity, Update.class)).get();
    }

    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @param update   the update
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public void editUpdate(String modID, long updateID, UpdateWithoutIdAndMod update) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        ModEntity mod = optionalMod.get();
        Optional<UpdateEntity> optionalUpdate = updateRepository.findFirstByModAndId(mod.getModID(), updateID);

        if (optionalUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        UpdateEntity updateEntity = optionalUpdate.get();

        updateEntity.setPublishDate(update.getPublishDate());
        updateEntity.setGameVersion(update.getGameVersion());
        updateEntity.setVersion(update.getVersion());
        updateEntity.setUpdateMessages(update.getUpdateMessages());
        updateEntity.setReleaseType(update.getReleaseType());
        updateEntity.setTags(update.getTags());
        updateEntity.setModLoader(update.getModLoader());
        updateRepository.save(updateEntity);
    }

    /**
     * @param modID    the mod ID
     * @param updateID the update ID
     * @throws ResponseStatusException if the mod or update doesn't exist
     */
    public void deleteUpdate(String modID, long updateID) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        ModEntity mod = optionalMod.get();
        Optional<UpdateEntity> optionalUpdate = updateRepository.findFirstByModAndId(mod.getModID(), updateID);

        if (optionalUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Update does not exist");
        }
        updateRepository.delete(optionalUpdate.get());
    }

}
