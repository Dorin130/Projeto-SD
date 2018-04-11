package org.binas.ws.it;

import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;


/**
 * Test suite
 */
public class PingIT extends BaseIT {


    @Test
    public void pingEmptyTest() {
		assertNotNull(client.testPing("test"));
    }

}
