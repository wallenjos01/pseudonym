package org.wallentines.plib.lang;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.plib.PipelineContext;
import org.wallentines.plib.UnresolvedMessage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface LangProvider {

    <T> Optional<LangRegistry> get(LangManager<T> manager, String language);

    static LangProvider forDirectory(Path directory, FileCodecRegistry codecRegistry) {
        return new Directory(directory, codecRegistry);
    }

    record Directory(Path searchDirectory, FileCodecRegistry registry) implements LangProvider {

        @Override
        public <T> Optional<LangRegistry> get(LangManager<T> manager, String language) {

            FileWrapper<ConfigObject> wrapper = registry.find(ConfigContext.INSTANCE, language, searchDirectory);
            if (wrapper == null) return Optional.empty();

            if(wrapper.getRoot() == null || !wrapper.getRoot().isSection()) return Optional.empty();

            ConfigSection sec = wrapper.getRoot().asSection();

            Map<String, UnresolvedMessage<String>> messages = new HashMap<>();
            for(String key : sec.getKeys()) {
                ConfigObject obj = sec.get(key);
                if(!obj.isString()) continue;
                messages.put(key, manager.parser.accept(obj.asString(), PipelineContext.of(manager)));
            }

            return Optional.of(new LangRegistry(Map.copyOf(messages)));
        }
    }

}
