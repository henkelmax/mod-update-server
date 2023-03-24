package de.maxhenkel.modupdateserver.entities;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ModWithUpdates extends Mod {

    @NotNull
    private List<Update> updates;

    public ModWithUpdates(String modID, String name, String description, String websiteURL, String downloadURL, String issueURL, List<Update> updates) {
        super(modID, name, description, websiteURL, downloadURL, issueURL);
        this.updates = updates;
    }

    public ModWithUpdates() {

    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }

}
