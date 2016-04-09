package com.jojos.challenge.resource;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * todo: create javadoc
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final AtomicBoolean started;

    private final HttpServer server;
    private final ExecutorService executorService;
    private final URI uri;

    /**
     * Public constructor of our server.
     * @throws ProcessingException when problems during server creation occurs.
     */
    public Server() throws ProcessingException {

        // load resources first
        ResourceConfig resourceConfig = new ResourceConfig(ResourceApi.class);

        uri = UriBuilder.fromUri("http://" + createHostName() + "/").port(8089).build();
        server = JdkHttpServerFactory.createHttpServer(uri, resourceConfig, false);
        started = new AtomicBoolean();
        executorService = Executors.newCachedThreadPool();
        // set the executors BEFORE the server is started
        server.setExecutor(executorService);
    }

    /**
     * Create and start a {@link HttpServer}.
     * It's not allowed to start the server twice.
     * @throws IllegalStateException if an attempt is made to start an already started server.
     */
    public void start() throws IllegalStateException {
        log.info("Starting JDK HttpServer...");

        if (started.compareAndSet(false, true)) {
            server.start();
            log.info(String.format("Jersey HttpServer started with WADL available at " + "%sapplication.wadl", uri));
        } else {
            String msg = "Server already started. Don't start me twice!";
            log.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * stop the current instance of the http server
     */
    public void stop() {
        if (started.compareAndSet(true, false)) {
            server.stop(1);
            executorService.shutdownNow();
        } else {
            log.warn("Attempting to stop an already stopped http server.");
        }
    }

    public boolean isStarted() {
        return started.get();
    }

    public URI getURI() {
        return uri;
    }

    private String createHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }
}
