package net.open_services.scheck.shapechecker;

import java.net.URISyntaxException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for HttpHandlerTest. *
 * @author Nick Crossley. Released to public domain 2018.
 */
public class HttpHandlerTest
{
    private HttpHandler httpHandler = new HttpHandler(0,null);

    /**
     * Test removeFragment method.
     * @throws URISyntaxException if something goes wrong
     */
    @Test
    public void testRemoveFragment() throws URISyntaxException
    {
        assertEquals(
            "http://open-services.net/ns/core",
            httpHandler.removeFragment("http://open-services.net/ns/core#name").toString());
        assertEquals(
            "http://open-services.net/ns/core",
            httpHandler.removeFragment("http://open-services.net/ns/core#").toString());
    }

}
