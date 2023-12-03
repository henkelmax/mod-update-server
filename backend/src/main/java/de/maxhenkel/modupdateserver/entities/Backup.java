package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.DateDeserializer;
import de.maxhenkel.modupdateserver.serializers.DateSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Backup {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date backupDate = new Date();

    @NotNull
    private List<ModWithUpdates> mods;

    public Backup(List<ModWithUpdates> mods) {
        this.mods = mods;
    }

}
