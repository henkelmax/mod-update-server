package de.maxhenkel.modupdateserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.maxhenkel.modupdateserver.serializers.DateDeserializer;
import de.maxhenkel.modupdateserver.serializers.DateSerializer;
import de.maxhenkel.modupdateserver.serializers.ObjectIdSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "updates")
public class Update {

    @MongoId(FieldType.OBJECT_ID)
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonProperty("_id")
    private ObjectId id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = DateSerializer.class)
    @JsonDeserialize(using = DateDeserializer.class)
    private Date publishDate = new Date();
    @NotNull
    @Size(min = 1)
    private String gameVersion;
    @NotNull
    @Size(min = 1)
    private String version;
    @NotNull
    private String[] updateMessages;
    @NotNull
    @Pattern(regexp = "alpha|beta|release")
    private String releaseType;
    @NotNull
    private String[] tags;
    @NotNull
    @Pattern(regexp = "forge|neoforge|fabric|quilt")
    private String modLoader = "forge";
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId mod;

}
