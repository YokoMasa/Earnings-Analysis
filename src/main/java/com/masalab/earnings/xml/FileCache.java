package com.masalab.earnings.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.masalab.earnings.exception.AppException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

public class FileCache {

    private Logger logger = LoggerFactory.getLogger(FileCache.class);
    private File cacheDir;

    // key: digest, val: cachedfile
    private Map<String, CachedFile> cachedFiles;
    private long cacheSize;
    private long maxCacheSize = 1024 * 1024 * 500;
    private long maxAgeSeconds = 60 * 60 * 24 * 7;

    public void setMaxCacheSize(long size) {
        this.maxCacheSize = size;
    }

    public void setMaxAgeSeconds(long seconds) {
        this.maxAgeSeconds = seconds;
    }

    public boolean put(String resourceId, InputStream is) {
        String digest = DigestUtils.md5DigestAsHex(resourceId.getBytes());
        File newCachedFile = new File(cacheDir, digest);
        try {
            Files.copy(is, newCachedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        cachedFiles.put(digest, new CachedFile(newCachedFile));
        cacheSize += newCachedFile.length();
        deleteLRU(digest);
        return true;
    }

    public InputStream get(String resourceId) throws AppException {
        String digest = DigestUtils.md5DigestAsHex(resourceId.getBytes());

        if (!cachedFiles.containsKey(digest)) {
            throw new AppException("Cache not found: " + resourceId);
        }

        CachedFile cachedFile = cachedFiles.get(digest);
        cachedFile.lastHit = System.currentTimeMillis();

        try {
            return new FileInputStream(cachedFile.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new AppException("Failed to create FileInputStream: " + cachedFile.file.getAbsolutePath());
        }
    }

    public boolean hasCache(String resourceId) {
        String digest = DigestUtils.md5DigestAsHex(resourceId.getBytes());
        if (cachedFiles.containsKey(digest)) {
            CachedFile cachedFile = cachedFiles.get(digest);
            if (cachedFile.file.lastModified() + maxAgeSeconds * 1000 < System.currentTimeMillis()) {
                long targetFileSize = cachedFile.file.length();
                if (cachedFile.file.delete()) {
                    cacheSize -= targetFileSize;
                }
                logger.debug("Deleting " + cachedFile.file.getName() + " as it exceeds maxAge");
                cachedFiles.remove(digest);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void deleteLRU(String exclude) {
        if (cacheSize < maxCacheSize) {
            return;
        }

        logger.debug("Deleteing least recently used cache file as cacheSize (" + cacheSize + ") exceeding maxCacheSize (" + maxCacheSize + ")");

        List<CachedFile> cachedFilesList = new ArrayList<>(cachedFiles.values());
        Collections.sort(cachedFilesList);
        boolean refreshFlag = false;
        for (int i = 0; i < cachedFilesList.size(); i++) {
            CachedFile target = cachedFilesList.get(i);
            if (target.file.getName().equals(exclude)) {
                continue;
            }

            long targetFileSize = target.file.length();
            if (target.file.delete()) {
                cacheSize -= targetFileSize;
                logger.debug("Deleting " + target.file.getName() + ", last hit: " + new Date(target.lastHit));
                cachedFiles.remove(target.file.getName());
            } else {
                refreshFlag = true;
            }

            if (cacheSize < maxCacheSize * 0.8) {
                break;
            }
        }

        if (refreshFlag) {
            refresh();
        }
    }

    private void refresh() {
        cacheSize = 0;
        cachedFiles = new HashMap<>();
        for (File f: cacheDir.listFiles()) {
            cacheSize += f.length();
            cachedFiles.put(f.getName(), new CachedFile(f));
        }
    }
    
    public FileCache(String cachePath) {
        cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            boolean mkdirResult = cacheDir.mkdir();
            if (!mkdirResult) {
                throw new RuntimeException("Failed to create cache directory: " + cachePath);
            }
        }
        refresh();
    }

    private class CachedFile implements Comparable<CachedFile> {

        File file;
        long lastHit;

        CachedFile(File f) {
            this.file = f;
            lastHit = System.currentTimeMillis();
        }

        @Override
        public int compareTo(CachedFile o) {
            if (o.lastHit < lastHit) {
                return 1;
            } else if (o.lastHit == lastHit) {
                return 0;
            } else {
                return -1;
            }
        }
    }

}
