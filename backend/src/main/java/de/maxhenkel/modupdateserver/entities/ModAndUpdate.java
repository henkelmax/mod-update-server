package de.maxhenkel.modupdateserver.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModAndUpdate {

    private ModEntity mod;
    private UpdateEntity update;

}
