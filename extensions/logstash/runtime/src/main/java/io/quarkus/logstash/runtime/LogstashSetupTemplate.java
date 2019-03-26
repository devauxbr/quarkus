package io.quarkus.logstash.runtime;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;

import org.jboss.logmanager.ext.formatters.LogstashFormatter;
import org.jboss.logmanager.handlers.SocketHandler;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Template;
import io.quarkus.runtime.logging.HandlerContext;
import io.quarkus.runtime.logging.LogCleanupFilter;

/**
 *
 */
@Template
public class LogstashSetupTemplate {
    public RuntimeValue<Optional<Handler>> initializeLogstashHandler(
            LogstashConfig logstashConfig, RuntimeValue<HandlerContext> handlerContextRuntimeValue) {
        HandlerContext context = handlerContextRuntimeValue.getValue();
        if (logstashConfig.enable &&
                logstashConfig.hostname != null && logstashConfig.port > 0) {
            try {
                // TODO : we should expose "keyOverrides" constructor parameter to config too :
                final LogstashFormatter formatter = new LogstashFormatter();
                final SocketHandler handler = new SocketHandler(logstashConfig.hostname, logstashConfig.port);
                handler.setFormatter(formatter);
                handler.setErrorManager(context.getErrorManager());
                handler.setLevel(logstashConfig.level);
                handler.setFilter(new LogCleanupFilter(context.getFilterElements()));
                return new RuntimeValue<>(of(handler));
            } catch (UnknownHostException e) {
                context.getErrorManager().error("Failed to create logstash handler", e, ErrorManager.OPEN_FAILURE);
            }
        }
        return new RuntimeValue<>(empty());
    }
}
