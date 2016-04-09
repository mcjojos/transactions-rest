package com.jojos.challenge.resource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test http server starting and stopping
 *
 * Created by karanikasg@gmail.com.
 */
public class ServerTest {

    private final Server server = new Server();

    @Before
    public void setUp() {
        server.start();
    }

    @After
    public void cleanUp() {
        server.stop();
    }

    @Test
    public void testStartOnce() {
        Assert.assertTrue("Server is not started as it should be", server.isStarted());
    }

    // expect a {@link IllegalStateException} if we attempt to start the server twice.
    @Test(expected=IllegalStateException.class)
    public void testStartTwice() {
        Assert.assertTrue("Server is not started as it should be", server.isStarted());
        server.start();
    }

}