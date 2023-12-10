package de.maxhenkel.modupdateserver.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Mod extends ModWithoutModId {

    @NotNull
    @Pattern(regexp = "[a-zA-Z_]+")
    private String modID;

}
