package de.maxhenkel.modupdateserver.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModWithUpdates extends Mod {

    @NotNull
    private List<UpdateWithoutIdAndMod> updates;

}
