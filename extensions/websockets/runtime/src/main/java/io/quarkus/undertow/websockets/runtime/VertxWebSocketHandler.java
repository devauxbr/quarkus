package io.quarkus.undertow.websockets.runtime;

import static io.undertow.websockets.ServerWebSocketContainer.WebSocketHandshakeHolder;

import java.security.Principal;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.arc.ManagedContext;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.undertow.websockets.ConfiguredServerEndpoint;
import io.undertow.websockets.EndpointSessionHandler;
import io.undertow.websockets.ServerWebSocketContainer;
import io.undertow.websockets.UndertowSession;
import io.undertow.websockets.WebSocketDeploymentInfo;
import io.undertow.websockets.handshake.Handshake;
import io.undertow.websockets.handshake.HandshakeUtil;
import io.undertow.websockets.util.WebsocketPathMatcher;
import io.undertow.websockets.vertx.VertxWebSocketHttpExchange;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Filter that provides HTTP upgrade functionality. This should be run after all user filters, but before any servlets.
 * <p>
 * The use of a filter rather than a servlet allows for normal HTTP requests to be served from the same location
 * as a web socket endpoint if no upgrade header is found.
 * <p>
 *
 * @author Stuart Douglas
 */
public class VertxWebSocketHandler implements Handler<RoutingContext> {

    private final EndpointSessionHandler callback;
    private final WebsocketPathMatcher<WebSocketHandshakeHolder> pathTemplateMatcher;
    private final ServerWebSocketContainer container;
    private final Executor executor;
    private final BeanContainer beanContainer;
    private final CurrentIdentityAssociation association;

    private static final String SESSION_ATTRIBUTE = "io.undertow.websocket.current-connections";

    public VertxWebSocketHandler(ServerWebSocketContainer container, WebSocketDeploymentInfo info,
            BeanContainer beanContainer) {
        this.container = container;
        this.executor = info.getExecutor().get();
        this.beanContainer = beanContainer;
        container.deploymentComplete();
        pathTemplateMatcher = new WebsocketPathMatcher<>();
        for (ConfiguredServerEndpoint endpoint : container.getConfiguredServerEndpoints()) {
            if (info == null || info.getServerExtensions().isEmpty()) {
                pathTemplateMatcher.add(endpoint.getPathTemplate(), container.handshakes(endpoint));
            } else {
                pathTemplateMatcher.add(endpoint.getPathTemplate(), container.handshakes(endpoint, info.getServerExtensions()));
            }
        }
        this.callback = new EndpointSessionHandler(container);
        Instance<CurrentIdentityAssociation> association = CDI.current().select(CurrentIdentityAssociation.class);
        this.association = association.isResolvable() ? association.get() : null;
    }

    @Override
    public void handle(RoutingContext event) {
        HttpServerRequest req = event.request();
        HttpServerResponse resp = event.response();
        if (req.getHeader(HttpHeaderNames.UPGRADE) != null) {
            final VertxWebSocketHttpExchange facade = new VertxWebSocketHttpExchange(executor, event);

            String path = event.normalisedPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            WebsocketPathMatcher.PathMatchResult<WebSocketHandshakeHolder> matchResult = pathTemplateMatcher.match(path);
            if (matchResult != null) {

                Handshake handshaker = null;
                for (Handshake method : matchResult.getValue().handshakes) {
                    if (method.matches(facade)) {
                        handshaker = method;
                        break;
                    }
                }

                if (handshaker != null) {
                    if (container.isClosed()) {
                        resp.setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end();
                        return;
                    }

                    ManagedContext requestContext = beanContainer.requestContext();
                    requestContext.activate();
                    Principal principal = null;
                    if (association != null) {
                        QuarkusHttpUser existing = (QuarkusHttpUser) event.user();
                        if (existing != null) {
                            SecurityIdentity identity = existing.getSecurityIdentity();
                            association.setIdentity(identity);
                            principal = identity.getPrincipal();
                        } else {
                            association.setIdentity(QuarkusHttpUser.getSecurityIdentity(event, null));
                        }
                    }

                    facade.putAttachment(HandshakeUtil.PATH_PARAMS, matchResult.getParameters());
                    facade.putAttachment(HandshakeUtil.PRINCIPAL, principal);
                    final Handshake selected = handshaker;
                    handshaker.handshake(facade, new Consumer<ChannelHandlerContext>() {
                        @Override
                        public void accept(ChannelHandlerContext context) {
                            UndertowSession channel = callback.connected(context, selected.getConfig(), facade,
                                    resp.headers().get(io.netty.handler.codec.http.HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));

                        }
                    });
                    return;
                }
            }
        }
        event.next();
    }
}
