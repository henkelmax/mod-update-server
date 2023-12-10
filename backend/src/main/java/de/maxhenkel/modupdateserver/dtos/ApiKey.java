package de.maxhenkel.modupdateserver.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @NotNull
    private UUID apiKey;
    @NotNull
    private String[] mods;

}

