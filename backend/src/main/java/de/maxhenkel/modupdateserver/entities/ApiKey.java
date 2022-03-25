package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Document(collection = "apiKeys")
public class ApiKey {

    @MongoId(FieldType.OBJECT_ID)
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonProperty("_id")
    private ObjectId id;
    @NotNull
    @Field(targetType = FieldType.STRING)
    private UUID apiKey;
    @NotNull
    private String[] mods;

    public ApiKey(UUID apiKey, @NotNull String[] mods) {
        this.apiKey = apiKey;
        this.mods = mods;
    }

    public ApiKey() {

    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public UUID getApiKey() {
        return apiKey;
    }

    public void setApiKey(UUID apiKey) {
        this.apiKey = apiKey;
    }

    public String[] getMods() {
        return mods;
    }

    public void setMods(String[] mods) {
        this.mods = mods;
    }
}
