package org.freedesktop;

import java.io.IOException;
import java.io.Writer;

public class DesktopEntryWriter {

    private IniStyleFileWriter delegate;

    /**
     * @param writer the writer must be using a utf-8 encoding
     */
    public DesktopEntryWriter(Writer writer) {
        this.delegate = new IniStyleFileWriter(writer);
    }

    public void write(DesktopEntry file) throws IOException {
        delegate.write(file);
    }
}
