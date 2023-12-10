package de.maxhenkel.modupdateserver.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "mod")
@NoArgsConstructor
@AllArgsConstructor
public class ModEntity {

    @Id
    private String modID;
    private String name;
    private String description;
    private String websiteURL;
    private String downloadURL;
    private String issueURL;

}
