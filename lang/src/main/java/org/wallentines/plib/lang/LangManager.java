package org.wallentines.plib.lang;

import org.wallentines.plib.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class LangManager<T> {

    private final Map<String, LangRegistry> registries = new HashMap<>();

    private final Class<T> messageClass;
    private final LangRegistry defaults;
    private final LangProvider provider;

    public final MessagePipeline<String, UnresolvedMessage<String>> parser;
    public final MessagePipeline<UnresolvedMessage<String>, T> resolver;

    private final HashMap<String, String> languageMappings = new HashMap<>();

    public LangManager(Class<T> messageClass, LangRegistry defaults, LangProvider provider, MessagePipeline<String, UnresolvedMessage<String>> parser, MessagePipeline<UnresolvedMessage<String>, T> resolver) {
        this.messageClass = messageClass;
        this.defaults = defaults;
        this.provider = provider;
        this.parser = parser;
        this.resolver = resolver;
    }

    public Class<T> getMessageClass() {
        return messageClass;
    }

    public T getMessage(String key, String language, Object... args) {

        LangRegistry reg;
        if(language == null) {
            reg = defaults;
        } else {
            reg = registries.getOrDefault(findClosestLanguage(language), defaults);
        }
        UnresolvedMessage<String> message = reg.registry().get(key);
        if (message == null) {
            message = defaults.registry().get(key);
            if(message == null) {
                return null;
            }
        }

        return resolver.accept(message, PlaceholderContext.of(args));
    }

    public LangRegistry getRegistry(String lang) {
        return registries.computeIfAbsent(lang, k -> provider.get(this, k).orElse(defaults));
    }

    public void clearCache() {
        registries.clear();
    }

    private String findClosestLanguage(String language) {

        if (language == null) {
            return null;
        }

        if (registries.containsKey(language) || !language.contains("_")) {
            return language;
        }

        return languageMappings.computeIfAbsent(language, l -> {
            String lang = l.split("_")[0];
            for (String key : registries.keySet()) {
                if (!key.contains("_")) {
                    continue;
                }
                String targetLang = key.split("_")[0];
                if (lang.equals(targetLang)) {
                    return key;
                }
            }
            return l;
        });
    }

    @SuppressWarnings("unchecked")
    public static void registerPlaceholders(PlaceholderManager manager) {

        manager.register(new Placeholder<>("lang", Object.class,
                (other, ctx) -> ctx.getFirst(LangManager.class)
                        .map(LangManager::getMessageClass)
                        .filter(clz -> clz.isAssignableFrom(other))
                        .isPresent(),
                ctx -> {

                    LangManager<?> man = ctx.context().getFirst(LangManager.class).orElse(null);
                    if (man == null) return Optional.empty();

                    String language = ctx.context().getFirst(LocaleHolder.class).map(LocaleHolder::getLanguage).orElse(null);
                    String key = UnresolvedMessage.resolve(ctx.param(), ctx.context());
                    return Optional.of(man.getMessage(key, language));

                }, ParameterTransformer.IDENTITY));

    }


}
