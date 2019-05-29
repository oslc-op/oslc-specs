package net.open_services.scheck.util;

import java.util.List;

import org.junit.Test;

import net.open_services.scheck.util.GlobExpander.PathParts;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;


/**
 * JUnit test cases for GlobExpander.
 * @author Nick Crossley. Released to public domain 2016.
 */
public class GlobExpanderTest
{
    /**
     * JUnit test cases for GlobExpander.
     */
    @Test
    public void testSplit()
    {
        String[][] trials = {
                // Input        // Expected prefix      // Expected glob
                {"/a/b/c",       "NULL",                 "NULL"},
                {"a/b/c",        "NULL",                 "NULL"},
                {"a/b]c",        "NULL",                 "NULL"},
                {"/a/b*/c",      "/a",                   "b*/c"},
                {"/b*/c",        "/",                    "b*/c"},
                {"b*/c",         ".",                    "b*/c"},
                {"b*",           ".",                    "b*"},
                {"b/c*",         "b",                    "c*"},
                {"b/c?",         "b",                    "c?"},
                {"b/c[abc]",     "b",                    "c[abc]"},
                {"b/c{a,b,c}",   "b",                    "c{a,b,c}"}
        };
        for (String[] trial : trials)
        {
            checkPathSplit(trial);
        }
    }

    private static void checkPathSplit(String[] trial)
    {
        PathParts parts = GlobExpander.splitGlobRoot(trial[0]);
        if (trial[1].equals("NULL"))
        {
            assertNull("Split should not have worked on "+trial[0],parts);
        }
        else
        {
            assertNotNull("Failed to split "+trial[0],parts);
            assertEquals("Wrong prefix result for "+trial[0],trial[1],parts.getPrefix().toString());
            assertEquals("Wrong suffix result for "+trial[0],trial[2],parts.getGlob().toString());
        }
    }


    /**
     * Test glob expansion in the test directory for this project
     * Should really use a dummy file system for this kind of test.
     */
    @Test
    public void testExpand()
    {
        List<String> files = GlobExpander.expand("./src/test/java/**/*.java");
        System.out.println("Matched "+files.size()+" files");
        for (String s : files)
        {
            System.out.println(s);
        }
        assertThat(files, hasItem("./src/test/java/net/open_services/scheck/util/GlobExpanderTest.java"));
        assertThat(files, not(hasItem("./src/main/java/net/open_services/scheck/util/GlobExpander.java")));
    }
}
