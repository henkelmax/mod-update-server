package de.maxhenkel.modupdateserver.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModAndUpdateCount {

    private ModEntity mod;
    private long updateCount;

}
