package org.raml.jaxrs.model.impl;

import org.raml.jaxrs.model.Path;
import org.raml.jaxrs.model.PathFragment;
import org.raml.jaxrs.model.Utilities;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PathImpl implements Path {

    private final String pathString;

    private PathImpl(String pathString) {
        this.pathString = pathString;
    }

    public static PathImpl fromString(String pathString) {
        checkNotNull(pathString);

        return new PathImpl(Utilities.uniformizePath(pathString));
    }

    @Override
    public List<PathFragment> getFragments() {
        throw new UnsupportedOperationException("unimplemented yet");
    }

    @Override
    public String getStringRepresentation() {
        return this.pathString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathImpl path = (PathImpl) o;

        return pathString.equals(path.pathString);

    }

    @Override
    public int hashCode() {
        return pathString.hashCode();
    }
}
