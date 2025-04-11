import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.pseudonym.*;
import org.wallentines.pseudonym.lang.LangManager;
import org.wallentines.pseudonym.lang.LangProvider;
import org.wallentines.pseudonym.lang.LangRegistry;

import java.util.Map;
import java.util.Optional;

public class TestLangManager {

    private final PlaceholderManager manager = new PlaceholderManager();
    private final LangRegistry<PartialMessage<String>> defaults;
    private final LangProvider<PartialMessage<String>> provider;


    public TestLangManager() {
        manager.register(new Placeholder<>("name", String.class, PlaceholderSupplier.of("World"), null));
        manager.register(new Placeholder<>("greeting", String.class, PlaceholderSupplier.of("Hello,"), null));
        manager.register(new Placeholder<>("to_upper", String.class, ctx ->
                Optional.of(ctx.param().toUpperCase()),
                ParameterTransformer.RESOLVE_EARLY));
        manager.register(new Placeholder<>("reverse", String.class, ctx ->
                Optional.of(new StringBuilder(ctx.param()).reverse().toString()),
                ParameterTransformer.RESOLVE_EARLY));

        LangManager.registerPlaceholders(manager);

        MessagePipeline<String, PartialMessage<String>> parser = MessagePipeline.parser(manager);
        defaults = new LangRegistry<>(Map.of(
                "message.literal", parser.accept("Hello"),
                "message.greeting", parser.accept("<lang>greeting</lang> <name>")
        ));

        LangRegistry<PartialMessage<String>> en = LangRegistry.builder(parser)
                .add("message.literal", "Hello")
                .add("greeting", "Hello,")
                .build();

        LangRegistry<PartialMessage<String>> es = new LangRegistry<>(Map.of(
                "message.literal", parser.accept("Hola"),
                "greeting", parser.accept("Hola,")
        ));

        provider = new LangProvider<>() {
            @Override
            public <R> Optional<LangRegistry<PartialMessage<String>>> get(LangManager<PartialMessage<String>, R> manager, String language) {
                return Optional.of(language.equals("en_us") ? en : es);
            }
        };

    }

    @Test
    public void canReturnDefault() {

        LangManager<PartialMessage<String>, String> manager = new LangManager<>(String.class, defaults, provider, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.literal");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hello", msg);
    }

    @Test
    public void canResolvePlaceholder() {

        LangManager<PartialMessage<String>, String> manager = new LangManager<>(String.class, defaults, provider, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.greeting");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hello, World", msg);
    }

    @Test
    public void canReturnLanguage() {

        LangManager<PartialMessage<String>, String> manager = new LangManager<>(String.class, defaults, provider, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.literal", "other");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hola", msg);
    }

    @Test
    public void canResolvePlaceholderForLanguage() {

        LangManager<PartialMessage<String>, String> manager = new LangManager<>(String.class, defaults, provider, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.greeting", "other");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hola, World", msg);
    }

}
