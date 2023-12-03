package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
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

}
