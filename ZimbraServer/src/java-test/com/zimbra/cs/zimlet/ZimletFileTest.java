package com.zimbra.cs.zimlet;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.cs.zimlet.ZimletFile.ZimletEntry;

public class ZimletFileTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFileName() {
        try {
            ZimletEntry entry = new ZimletFile.ZimletRawEntry(null, "../../conf/something", 1);
            fail("Should throw ZimletException");
        } catch (ZimletException e) {
            assertTrue("Wrong error message", e.getMessage().indexOf("Invalid entry") > -1);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
