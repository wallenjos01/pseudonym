package org.wallentines.plib.text;

import org.wallentines.mdcfg.serializer.DelegatedContext;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.function.Function;

public class ProtocolContext<T> extends DelegatedContext<T, Integer> {

    /**
     * The maximum protocol version for release versions. Starting at 1.16.4-pre1, snapshot versions use numbers
     * greater than this.
     */
    public static final int RELEASE_MAX_VERSION = 0x40000000;

    public ProtocolContext(SerializeContext<T> delegate, int value) {
        super(delegate, value);
    }

    public int getProtocolVersion() {
        return getContextValue();
    }

    public boolean isSnapshotVersion() {
        return getProtocolVersion() > RELEASE_MAX_VERSION;
    }

    public int getSnapshotVersion() {
        return getProtocolVersion() - RELEASE_MAX_VERSION;
    }

    boolean hasFeature(Feature f) {
        if(isSnapshotVersion()) {
            return getSnapshotVersion() >= f.snapshotVersion;
        }
        return getProtocolVersion() >= f.version;
    }

    static boolean hasFeature(int version, Feature f) {
        if(version > RELEASE_MAX_VERSION) {
            return (version - RELEASE_MAX_VERSION) >= f.snapshotVersion;
        }
        return version > f.version;
    }

    public record Feature(int version, int snapshotVersion) { }

    public static <O> Serializer<O> select(Function<ProtocolContext<?>, Serializer<O>> selector) {

        return new Serializer<O>() {
            @Override
            public <O1> SerializeResult<O1> serialize(SerializeContext<O1> context, O value) {
                if(context instanceof ProtocolContext<?> pc) {
                    return selector.apply(pc).serialize(context, value);
                } else {
                    return selector.apply(new ProtocolContext<>(context, RELEASE_MAX_VERSION)).serialize(context, value);
                }
            }

            @Override
            public <O1> SerializeResult<O> deserialize(SerializeContext<O1> context, O1 value) {
                if(context instanceof ProtocolContext<?> pc) {
                    return selector.apply(pc).deserialize(context, value);
                } else {
                    return selector.apply(new ProtocolContext<>(context, RELEASE_MAX_VERSION)).deserialize(context, value);
                }
            }
        };

    }

}
