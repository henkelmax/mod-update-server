package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Document(collection = "mods")
public class Mod {

    @MongoId(FieldType.OBJECT_ID)
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonProperty("_id")
    private ObjectId id;
    @NotNull
    @Pattern(regexp = "[a-zA-Z_]+")
    private String modID;
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

    public Mod(Mod mod) {
        this.id = mod.getId();
        this.modID = mod.getModID();
        this.name = mod.getName();
        this.description = mod.getDescription();
        this.websiteURL = mod.getWebsiteURL();
        this.downloadURL = mod.getDownloadURL();
        this.issueURL = mod.getIssueURL();
    }

    public Mod(String modID, String name, String description, String websiteURL, String downloadURL, String issueURL) {
        this.modID = modID;
        this.name = name;
        this.description = description;
        this.websiteURL = websiteURL;
        this.downloadURL = downloadURL;
        this.issueURL = issueURL;
    }

    public Mod() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getModID() {
        return modID;
    }

    public void setModID(String modID) {
        this.modID = modID;
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
