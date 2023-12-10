package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.Mod;
import de.maxhenkel.modupdateserver.dtos.ModWithUpdateCount;
import de.maxhenkel.modupdateserver.dtos.ModWithoutModId;
import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ModService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UpdateRepository updateRepository;

    public boolean doesModExist(String modId) {
        return modRepository.findById(modId).isPresent();
    }

    public List<Mod> getMods() {
        return modRepository.findAll().stream().map(modEntity -> modelMapper.map(modEntity, Mod.class)).toList();
    }

    /**
     * @param mod the mod
     * @return <code>false</code> if the mod already exists
     */
    @Transactional
    public boolean addMod(Mod mod) {
        if (doesModExist(mod.getModID())) {
            return false;
        }
        modRepository.save(modelMapper.map(mod, ModEntity.class));
        return true;
    }

    /**
     * @param modID the mod ID
     * @param mod   the mod
     * @return <code>false</code> if the mod does not exist
     */
    @Transactional
    public boolean editMod(String modID, ModWithoutModId mod) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            return false;
        }
        ModEntity modToUpdate = optionalMod.get();

        modToUpdate.setName(mod.getName());
        modToUpdate.setDescription(mod.getDescription());
        modToUpdate.setWebsiteURL(mod.getWebsiteURL());
        modToUpdate.setDownloadURL(mod.getDownloadURL());
        modToUpdate.setIssueURL(mod.getIssueURL());

        modRepository.save(modToUpdate);
        return true;
    }

    public Optional<ModWithUpdateCount> getMod(String modID) {
        return modRepository.getModWithUpdateCount(modID).map(o -> {
            ModWithUpdateCount mod = modelMapper.map(o.getMod(), ModWithUpdateCount.class);
            mod.setUpdateCount(o.getUpdateCount());
            return mod;
        });
    }

    /**
     * @param modID the mod ID
     * @throws ResponseStatusException if the mod or update doesn't exist or the update couldn't get deleted
     */
    @Transactional
    public void deleteMod(String modID) {
        Optional<ModEntity> optionalMod = modRepository.findById(modID);
        if (optionalMod.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mod does not exist");
        }
        ModEntity mod = optionalMod.get();

        updateRepository.removeAllByMod(mod.getModID());
        modRepository.deleteById(mod.getModID());
    }

}
