package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.DateDeserializer;
import de.maxhenkel.modupdateserver.serializers.DateSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class Backup {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date backupDate = new Date();

    @NotNull
    private List<ModWithUpdates> mods;

    public Backup(Date backupDate, List<ModWithUpdates> mods) {
        this.backupDate = backupDate;
        this.mods = mods;
    }

    public Backup(List<ModWithUpdates> mods) {
        this.mods = mods;
    }

    public Backup() {

    }

    public Date getBackupDate() {
        return backupDate;
    }

    public void setBackupDate(Date backupDate) {
        this.backupDate = backupDate;
    }

    public List<ModWithUpdates> getMods() {
        return mods;
    }

    public void setMods(List<ModWithUpdates> mods) {
        this.mods = mods;
    }

}
