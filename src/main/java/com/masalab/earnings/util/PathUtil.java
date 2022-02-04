package com.masalab.earnings.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class PathUtil {
 
    public static String getAbsolutePath(String pathString) {
        if (pathString.startsWith("http")) {
            return pathString;
        } else {
            return FileSystems.getDefault().getPath(pathString).toAbsolutePath().toString();
        }
    }

    public static String getAbsolutePathRelativeToFile(String pathString, String base) throws URISyntaxException {
        if (base.startsWith("http")) {
            URI uri = new URI(base);
            return uri.resolve(pathString).toString();
        } else {
            Path path = FileSystems.getDefault().getPath(base).toAbsolutePath();
            return path.resolveSibling(pathString).toAbsolutePath().normalize().toString();
        }
    }

}
