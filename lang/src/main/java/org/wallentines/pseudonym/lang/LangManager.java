package org.wallentines.pseudonym.lang;

import org.wallentines.pseudonym.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class LangManager<P, R> {

    private final Map<String, LangRegistry<P>> registries = new HashMap<>();

    private final Class<R> messageClass;
    private final LangRegistry<P> defaults;
    private final LangProvider<P> provider;

    public final MessagePipeline<P, R> resolver;

    public LangManager(Class<R> messageClass, LangRegistry<P> defaults, LangProvider<P> provider, MessagePipeline<P, R> resolver) {
        this.messageClass = messageClass;
        this.defaults = defaults;
        this.provider = provider;
        this.resolver = resolver;
    }

    public Class<R> getMessageClass() {
        return messageClass;
    }

    public R getMessage(String key, Object... args) {
        PipelineContext ctx = PipelineContext.of(args);
        return getMessage(key, ctx.getFirst(LocaleHolder.class).map(LocaleHolder::getLanguage).orElse(null), ctx);
    }


    public R getMessage(String key, String language, Object... args) {
        return getMessage(key, language, PipelineContext.of(args));
    }

    public R getMessage(String key, String language, PipelineContext context) {

        LangRegistry<P> reg;
        if(language == null) {
            reg = defaults;
        } else {
            reg = getRegistry(language);
        }
        P message = reg.registry().get(key);
        if (message == null) {
            message = defaults.registry().get(key);
            if(message == null) {
                return null;
            }
        }

        return resolver.accept(message, context);
    }

    public LangRegistry<P> getRegistry(String lang) {
        return registries.computeIfAbsent(lang, k -> provider.get(this, k).orElse(defaults));
    }

    public void clearCache() {
        registries.clear();
    }

    @SuppressWarnings("unchecked")
    public static void registerPlaceholders(PlaceholderManager manager) {

        manager.register(new Placeholder<>("lang", Object.class,
                (other, ctx) -> ctx.getFirst(LangManager.class)
                        .map(LangManager::getMessageClass)
                        .filter(clz -> clz.isAssignableFrom(other))
                        .isPresent(),
                ctx -> {

                    LangManager<?, ?> man = ctx.context().getFirst(LangManager.class).orElse(null);
                    if (man == null) return Optional.empty();

                    String language = ctx.context().getFirst(LocaleHolder.class).map(LocaleHolder::getLanguage).orElse(null);
                    String key = UnresolvedMessage.resolve(ctx.param(), ctx.context());
                    return Optional.of(man.getMessage(key, language));

                }, ParameterTransformer.IDENTITY));

    }


}
