package io.quarkus.logstash.runtime;

import java.util.logging.Level;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class LogstashConfig {

    /**
     * If logstash logging should be enabled
     */
    @ConfigItem(defaultValue = "false")
    boolean enable;

    /**
     * The logstash log level
     */
    @ConfigItem(defaultValue = "INFO")
    Level level;

    /**
     * The logstash target instance hostname
     */
    @ConfigItem
    String hostname;

    /**
     * The logstash target instance port
     */
    @ConfigItem
    int port;
}
