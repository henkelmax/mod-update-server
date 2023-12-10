package de.maxhenkel.modupdateserver.entities;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Date;

@Data
@Entity(name = "update")
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date publishDate;
    private String gameVersion;
    private String version;
    @Type(StringArrayType.class)
    @Column(columnDefinition = "text ARRAY")
    private String[] updateMessages;
    private String releaseType;
    @Type(StringArrayType.class)
    @Column(columnDefinition = "text ARRAY")
    private String[] tags;
    private String modLoader = "forge";
    private String mod;

}
