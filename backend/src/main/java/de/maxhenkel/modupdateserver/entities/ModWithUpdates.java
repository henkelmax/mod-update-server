package de.maxhenkel.modupdateserver.entities;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModWithUpdates extends Mod {

    @NotNull
    private List<Update> updates;

    public ModWithUpdates(String modID, String name, String description, String websiteURL, String downloadURL, String issueURL, List<Update> updates) {
        super(modID, name, description, websiteURL, downloadURL, issueURL);
        this.updates = updates;
    }

}
