package com.jojos.challenge;

import com.jojos.challenge.resource.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The application's main entry point.
 *
 * @author karanikasg@gmail.com.
 */
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	private Server server;

	public static void main(String[] args) {
		App app = new App();
		app.start();
	}

	public void start() {
		log.info("Starting Embedded Jersey HTTPServer...");

		server = new Server();
		server.start();
		attachShutDownHook();
	}

	private void attachShutDownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (server != null) {
					// every piece of instructions that JVM should execute before going down should be defined here
					server.stop();
				}
			}
		});
	}
}