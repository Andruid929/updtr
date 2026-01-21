package net.druidlabs.updtr.util;

import java.io.FileNotFoundException;

public class ResourceNotFoundException extends FileNotFoundException {

    public ResourceNotFoundException(String s) {
        super(s);
    }

}
