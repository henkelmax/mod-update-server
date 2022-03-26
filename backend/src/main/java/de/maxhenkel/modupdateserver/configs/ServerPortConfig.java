package de.maxhenkel.modupdateserver.configs;

import de.maxhenkel.modupdateserver.Utils;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ServerPortConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(Integer.parseInt(Utils.getEnv("PORT", "8088")));
    }
}
