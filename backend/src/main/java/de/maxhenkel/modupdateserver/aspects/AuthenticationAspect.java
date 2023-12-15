package de.maxhenkel.modupdateserver.aspects;

import de.maxhenkel.modupdateserver.dtos.ApiKey;
import de.maxhenkel.modupdateserver.services.ApiKeyService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Aspect
public class AuthenticationAspect {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private Environment env;

    @Before("@annotation(de.maxhenkel.modupdateserver.annotations.ValidateApiKey)")
    public void validateHeaders(JoinPoint joinPoint) {
        HttpServletRequest request = getRequest();
        Map<String, Object> attributes = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String modID = (String) attributes.getOrDefault("modID", "*");

        if (!hasPermission(request.getHeader("apikey"), modID)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Insufficient permissions");
        }
    }

    @Before("@annotation(de.maxhenkel.modupdateserver.annotations.ValidateMasterKey)")
    public void validateHeadersMaster(JoinPoint joinPoint) {
        HttpServletRequest request = getRequest();
        UUID apikey = parseApiKey(request.getHeader("apikey"));
        if (!apikey.equals(getMasterKey())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Insufficient permissions");
        }
    }

    private boolean hasPermission(@Nullable String key, String modID) {
        UUID uuid = parseApiKey(key);

        if (uuid.equals(getMasterKey())) {
            return true;
        }

        Optional<ApiKey> apikey = apiKeyService.getApikey(uuid);
        if (apikey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        ApiKey apiKey = apikey.get();
        return Arrays.stream(apiKey.getMods()).anyMatch(s -> s.equals("*") || s.equals(modID));
    }

    @Nullable
    public UUID getMasterKey() {
        try {
            if (env.acceptsProfiles(Profiles.of("!prod"))) {
                return UUID.fromString(env.getProperty("modupdateserver.masterkey", ""));
            }
            return UUID.fromString(System.getenv("MASTER_KEY"));
        } catch (Exception e) {
            return null;
        }
    }

    public UUID parseApiKey(@Nullable String apiKey) {
        if (apiKey == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No API key provided");
        }
        try {
            return UUID.fromString(apiKey);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
    }

    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

}