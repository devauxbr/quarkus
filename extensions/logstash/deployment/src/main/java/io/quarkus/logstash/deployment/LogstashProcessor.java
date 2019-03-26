package io.quarkus.logstash.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.logging.HandlerBuildItem;
import io.quarkus.deployment.logging.HandlerContextBuildItem;
import io.quarkus.logstash.runtime.LogstashConfig;
import io.quarkus.logstash.runtime.LogstashSetupTemplate;

public class LogstashProcessor {

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void setupRuntimeLogstashHandler(LogstashSetupTemplate setupTemplate, LogstashConfig logstashConfig,
            HandlerContextBuildItem handlerContextBuildItem, BuildProducer<HandlerBuildItem> handlerProducer) {
        handlerProducer.produce(
                new HandlerBuildItem(
                        setupTemplate.initializeLogstashHandler(logstashConfig, handlerContextBuildItem.getHandlersContext())));
    }
}
