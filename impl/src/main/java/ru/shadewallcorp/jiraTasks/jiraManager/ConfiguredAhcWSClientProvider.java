package ru.shadewallcorp.jiraTasks.jiraManager;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.libs.ws.WSClient;
import play.api.libs.ws.WSRequest;
import play.api.libs.ws.ahc.AhcCurlRequestLogger;
import play.api.libs.ws.ahc.AhcWSClient;
import play.api.libs.ws.ahc.StandaloneAhcWSClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * Конфигурация для логирования исходящих запросов.
 * В конфигурации новая настройка configured-ahc-ws-client / request-logging-enabled = false.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Singleton
public class ConfiguredAhcWSClientProvider implements Provider<WSClient> {
    private final AhcWSClient client;

    @Inject
    public ConfiguredAhcWSClientProvider(Config config, AsyncHttpClient asyncHttpClient, Materializer materializer) {
        client = new ConfiguredAhcWSClient(new StandaloneAhcWSClient(asyncHttpClient, materializer), config);
    }

    @Override
    public WSClient get() {
        return client;
    }

    /**
     * @author Dilvish {@literal <dilvish@newmail.ru>}
     */
    public static class ConfiguredAhcWSClient extends AhcWSClient {
        private static final Logger logger = LoggerFactory.getLogger(ConfiguredAhcWSClient.class);
        private final Cfg config;

        ConfiguredAhcWSClient(StandaloneAhcWSClient underlyingClient, Config config) {
            super(underlyingClient);
            this.config = ConfigBeanFactory.create(config.getConfig("configured-ahc-ws-client"), Cfg.class);
        }

        @Override
        public WSRequest url(String url) {
            WSRequest request = super.url(url);
            if (isTrue(config.getRequestLoggingEnabled())) {
                request = request.withRequestFilter(new AhcCurlRequestLogger(logger));
            }
            return request;
        }
    }

    /**
     * @author Dilvish {@literal <dilvish@newmail.ru>}
     */
    @Data
    @NoArgsConstructor
    public static class Cfg {
        @Optional
        private Boolean requestLoggingEnabled;
    }
}
