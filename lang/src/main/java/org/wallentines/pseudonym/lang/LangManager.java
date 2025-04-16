package org.wallentines.pseudonym.lang;

import org.wallentines.pseudonym.*;

import java.util.*;


public class LangManager<P, R> {

    private final Map<String, LangRegistry<P>> registries = new HashMap<>();

    private final Class<R> messageClass;
    private final LangRegistry<P> defaults;
    private final LangProvider<P> provider;
    private final String defaultLanguage;

    public final MessagePipeline<P, R> resolver;

    public LangManager(Class<R> messageClass, LangRegistry<P> defaults, LangProvider<P> provider, MessagePipeline<P, R> resolver) {
        this(messageClass, defaults, provider, resolver, "en_us");
    }

    public LangManager(Class<R> messageClass, LangRegistry<P> defaults, LangProvider<P> provider, MessagePipeline<P, R> resolver, String defaultLanguage) {
        this.messageClass = messageClass;
        this.defaults = defaults;
        this.provider = provider;
        this.resolver = resolver;
        this.defaultLanguage = defaultLanguage;
    }

    public Class<R> getMessageClass() {
        return messageClass;
    }

    public R getMessage(String key, Object... args) {
        PipelineContext ctx = PipelineContext.of(args);
        return getMessageFor(key, ctx);
    }

    public R getMessage(String key, String language, Object... args) {
        return getMessageFor(key, language, PipelineContext.of(args));
    }


    public R getMessageFor(String key, PipelineContext context) {
        String lang;
        Optional<LocaleHolder> localeHolder = context.getFirst(LocaleHolder.class);
        if(localeHolder.isPresent()) {
            lang = localeHolder.get().getLanguage();
            context = context.and(PipelineContext.of(this));
        } else {
            lang = defaultLanguage;
            context = context.and(PipelineContext.of(this, LocaleHolder.direct(lang)));
        }
        return getInternal(key, lang, context);
    }

    public R getMessageFor(String key, String language, PipelineContext context) {
        PipelineContext finalCtx = context.and(PipelineContext.of(this, LocaleHolder.direct(language)));
        return getInternal(key, language, finalCtx);
    }

    private R getInternal(String key, String language, PipelineContext context) {

        LangRegistry<P> reg;
        if(language == null) {
            language = defaultLanguage;
        }
        reg = getRegistry(language);

        P message = reg.registry().get(key);
        if (message == null) {
            message = defaults.registry().get(key);
            if(message == null) {
                return null;
            }
        }

        return resolver.accept(message, context);
    }

    public P getParsed(String key, String language) {
        return getRegistry(language).registry().get(key);
    }

    public Message<R> message(String key) {
        return ctx -> getMessageFor(key, ctx);
    }

    public Message<R> message(String key, Object... args) {
        return ctx -> getMessageFor(key, PipelineContext.of(args).and(ctx));
    }

    public Message<R> messageFor(String key, PipelineContext context) {
        return ctx -> getMessageFor(key, context.and(ctx));
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
                    String key = PartialMessage.resolve(ctx.param(), ctx.context());
                    return Optional.of(man.getMessage(key, language));

                }, ParameterTransformer.IDENTITY));

    }


}
