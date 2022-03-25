package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "mods")
public class ModWithUpdateCount {

    @MongoId(FieldType.OBJECT_ID)
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonProperty("_id")
    private ObjectId id;
    private String modID;
    private String name;
    private String description;
    private String websiteURL;
    private String downloadURL;
    private String issueURL;
    private int updateCount;

    public ModWithUpdateCount(Mod mod, int updateCount) {
        this.id = mod.getId();
        this.modID = mod.getModID();
        this.name = mod.getName();
        this.description = mod.getDescription();
        this.websiteURL = mod.getWebsiteURL();
        this.downloadURL = mod.getDownloadURL();
        this.issueURL = mod.getIssueURL();
        this.updateCount = updateCount;
    }

    public ModWithUpdateCount() {

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

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
}
