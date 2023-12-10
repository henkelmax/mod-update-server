package de.maxhenkel.modupdateserver.services;

import de.maxhenkel.modupdateserver.dtos.*;
import de.maxhenkel.modupdateserver.entities.ModEntity;
import de.maxhenkel.modupdateserver.entities.UpdateEntity;
import de.maxhenkel.modupdateserver.repositories.ModRepository;
import de.maxhenkel.modupdateserver.repositories.UpdateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BackupService {

    @Autowired
    private ModRepository modRepository;

    @Autowired
    private UpdateRepository updateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public Backup getBackup() {
        List<ModWithUpdates> mods = new ArrayList<>();
        for (ModEntity mod : modRepository.findAll()) {
            ModWithUpdates modWithUpdates = modelMapper.map(mod, ModWithUpdates.class);
            modWithUpdates.setUpdates(updateRepository.getAllByMod(mod.getModID()).stream().map(e -> modelMapper.map(e, UpdateWithoutIdAndMod.class)).toList());
            mods.add(modWithUpdates);
        }
        return new Backup(mods);
    }

    /**
     * @param backup the backup
     * @return <code>false</code> if the database is not empty
     */
    @Transactional
    public boolean restore(Backup backup) {
        if (modRepository.count() > 0L || updateRepository.count() > 0L) {
            return false;
        }

        for (ModWithUpdates mod : backup.getMods()) {
            ModEntity modEntity = modRepository.save(modelMapper.map(mod, ModEntity.class));
            modRepository.save(modEntity);
            for (UpdateWithoutIdAndMod update : mod.getUpdates()) {
                UpdateEntity updateEntity = modelMapper.map(update, UpdateEntity.class);
                updateEntity.setMod(modEntity.getModID());
                updateRepository.save(updateEntity);
            }
        }

        return true;
    }

}
