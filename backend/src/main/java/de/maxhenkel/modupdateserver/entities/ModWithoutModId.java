package de.maxhenkel.modupdateserver.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

}
