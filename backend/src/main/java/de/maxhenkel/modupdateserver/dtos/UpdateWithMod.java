package de.maxhenkel.modupdateserver.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UpdateWithMod extends UpdateWithoutIdAndMod {

    @JsonProperty("_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(as = Long.class)
    @NotNull
    private Long id;
    private Mod mod;

}
