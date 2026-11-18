package de.maxhenkel.modupdateserver.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Update extends UpdateWithoutIdAndMod {

    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(as = Long.class)
    @NotNull
    private Long id;
    @NotNull
    private String mod;

}
