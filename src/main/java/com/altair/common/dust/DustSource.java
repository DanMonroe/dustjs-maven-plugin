package com.altair.common.dust;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dmonroe
 * Date: 4/10/13
 * Time: 3:26 PM
 */
public class DustSource {
    private File file;
    public DustSource(File file) throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file.getAbsolutePath() + " not found.");
        }
        this.file = file;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public long getLastModified() {
        return file.lastModified();
    }

    public File getFile() {
        return file;
    }

    public String getBaseName() {
        return FilenameUtils.getBaseName(file.getName());
    }

    public String getContents() {
        String returnString = null;
        try {
            returnString = FileUtils.fileRead(file, "UTF-8");
        } catch (IOException e) {
        }
        return returnString;
    }
}
