package de.maxhenkel.modupdateserver.entities;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Data
@Entity(name = "api_key")
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyEntity {

    @Id
    private UUID apiKey;
    @Type(StringArrayType.class)
    @Column(columnDefinition = "text ARRAY")
    private String[] mods;

}
