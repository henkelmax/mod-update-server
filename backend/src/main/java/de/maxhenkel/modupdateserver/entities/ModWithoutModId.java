package de.maxhenkel.modupdateserver.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ModWithoutModId {

    @NotNull
    @Size(min = 1)
    private String name;
    @NotNull
    private String description;
    @NotNull
    private String websiteURL;
    @NotNull
    private String downloadURL;
    @NotNull
    private String issueURL;

    public ModWithoutModId(String name, String description, String websiteURL, String downloadURL, String issueURL) {
        this.name = name;
        this.description = description;
        this.websiteURL = websiteURL;
        this.downloadURL = downloadURL;
        this.issueURL = issueURL;
    }

    public ModWithoutModId() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getIssueURL() {
        return issueURL;
    }

    public void setIssueURL(String issueURL) {
        this.issueURL = issueURL;
    }
}
