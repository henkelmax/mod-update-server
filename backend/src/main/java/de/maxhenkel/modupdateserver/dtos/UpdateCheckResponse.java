package de.maxhenkel.modupdateserver.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCheckResponse {

    @Nullable
    private String homepage;
    @Nonnull
    private Map<String, VersionUpdateInfo> versions;

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VersionUpdateInfo {

        @Nonnull
        private Version latest;
        @Nullable
        private Version recommended;

    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Version {

        @Nonnull
        private String version;
        @Nonnull
        private String[] changelog;
        @Nullable
        private String[] downloadLinks;

    }

}
