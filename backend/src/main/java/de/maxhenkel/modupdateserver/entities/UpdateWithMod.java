package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Document(collection = "updates")
public class UpdateWithMod {

    @MongoId(FieldType.OBJECT_ID)
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonProperty("_id")
    private ObjectId id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date publishDate;
    private String gameVersion;
    private String version;
    private String[] updateMessages;
    private String releaseType;
    private String[] tags;
    private String modLoader;
    private Mod mod;

    public UpdateWithMod(Date publishDate, String gameVersion, String version, String[] updateMessages, String releaseType, String[] tags, String modLoader, Mod mod) {
        this.publishDate = publishDate;
        this.gameVersion = gameVersion;
        this.version = version;
        this.updateMessages = updateMessages;
        this.releaseType = releaseType;
        this.tags = tags;
        this.modLoader = modLoader;
        this.mod = mod;
    }

    public UpdateWithMod() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getUpdateMessages() {
        return updateMessages;
    }

    public void setUpdateMessages(String[] updateMessages) {
        this.updateMessages = updateMessages;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getModLoader() {
        return modLoader;
    }

    public void setModLoader(String modLoader) {
        this.modLoader = modLoader;
    }

    public Mod getMod() {
        return mod;
    }

    public void setMod(Mod mod) {
        this.mod = mod;
    }
}
