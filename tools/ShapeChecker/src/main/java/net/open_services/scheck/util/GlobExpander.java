package net.open_services.scheck.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Expand shell-style wildcards in paths ('globs').
 * @author Nick Crossley. Released to public domain 2016.
 */
public class GlobExpander extends SimpleFileVisitor<Path>
{
    private static final Pattern GLOB_CHARS = Pattern.compile("[\\[{*?]");
    private final String fullGlob;
    private final PathParts pathParts;
    private final PathMatcher matcher;
    private List<String> results = new ArrayList<>();


    /**
     * Construct a new GlobExpander.
     * @param fullGlob the entire path we are going to expand
     * @param pathParts the parts of the path before, and with, any wildcards
     */
    public GlobExpander(String fullGlob, PathParts pathParts)
    {
        this.fullGlob = fullGlob;
        this.pathParts = pathParts;
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + pathParts.getGlob());
    }


    // Run the file tree walk from the extracted prefix, and match against the extracted glob
    private List<String> expandMe()
    {
        try
        {
            Files.walkFileTree(pathParts.getPrefix(), this);
        }
        catch (IOException e)
        {
            System.err.println("Failed to expand the path " + fullGlob);
        }

        return results;
    }


    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
    {
        Path below = pathParts.getPrefix().relativize(path);
        if (matcher.matches(below))
        {
            results.add(path.toString());
        }
        return FileVisitResult.CONTINUE;
    }


    /**
     * Expand a single shell-style glob into a list of paths.
     * @param glob the glob to be expanded
     * @return a list of paths matching the given glob
     */
    public static List<String> expand(String glob)
    {
        PathParts parts = splitGlobRoot(glob);

        if (parts == null)
        {
            return Collections.singletonList(glob);
        }

        GlobExpander expander = new GlobExpander(glob,parts);
        return expander.expandMe();
    }


    static PathParts splitGlobRoot(String path)
    {
        String prefix;
        String glob;
        Matcher matcher = GLOB_CHARS.matcher(path);
        if (!matcher.find())
        {
            return null;
        }
        else
        {
            int divider = path.substring(0, matcher.start()).lastIndexOf('/');
            if (divider < 0)
            {
                prefix = ".";
                glob = path;
            }
            else if (divider == 0)
            {
                prefix = "/";
                glob = path.substring(1);
            }
            else
            {
                prefix = path.substring(0, divider);
                glob = path.substring(divider + 1);
            }
            return new PathParts(Paths.get(prefix),Paths.get(glob));
        }
    }


    /**
     * Make a list of URIs for an argument that is either a single URI string,
     * or a file path that contains shell-style globs to be expanded.
     * @param argVal an argument that is either a URI string or a file path with globs
     * @return a list of URIs that are formed from the provided string
     * @throws URISyntaxException if the URI is not valid
     */
    @javax.annotation.CheckReturnValue
    public static List<URI> checkFileOrURI(String argVal) throws URISyntaxException
    {
        if (argVal.startsWith("http://") || argVal.startsWith("https://"))
        {
            return Collections.singletonList(new URI(argVal));
        }
        else
        {
           List<URI> uris = new ArrayList<>();
           for (String path : expand(argVal))
           {
               uris.add(new File(path).toURI());
           }
           if (uris.isEmpty())
           {
               System.err.println("Warning: nothing matches "+argVal);
           }
           return uris;
        }
    }


    /**
     * A value class representing a path split into the elements
     * before and including any shell wildcard characters.
     */
    public static final class PathParts
    {
        private final Path prefix;
        private final Path glob;

        /**
         * Construct a new PathParts.
         * @param prefix the leading part of the path before any elements with wildcards
         * @param glob the trailing part of the path including any wildcards
         */
        public PathParts(Path prefix, Path glob)
        {
            this.prefix = prefix;
            this.glob = glob;
        }

        /**
         * Get the leading part of the path before any elements with wildcards.
         * @return returns the leading part of the path before any elements with wildcards.
         */
        public Path getPrefix()
        {
            return prefix;
        }

        /**
         * Get the trailing part of the path including any wildcards.
         * @return returns the trailing part of the path including any wildcards.
         */
        public Path getGlob()
        {
            return glob;
        }

    }
}
