import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.plib.*;
import org.wallentines.plib.lang.LangManager;
import org.wallentines.plib.lang.LangProvider;
import org.wallentines.plib.lang.LangRegistry;

import java.util.Map;
import java.util.Optional;

public class TestLangManager {

    private final PlaceholderManager manager = new PlaceholderManager();
    private final MessagePipeline<String, UnresolvedMessage<String>> parser = MessagePipeline.parse(manager);
    private final LangRegistry defaults;
    private final LangProvider provider;


    public TestLangManager() {
        manager.register(new Placeholder<>("name", String.class, PlaceholderSupplier.of("World"), null));
        manager.register(new Placeholder<>("greeting", String.class, PlaceholderSupplier.of("Hello,"), null));
        manager.register(new Placeholder<>("to_upper", String.class, ctx ->
                Optional.of(ctx.param().toUpperCase()),
                ParameterTransformer.RESOLVE_EARLY));
        manager.register(new Placeholder<>("reverse", String.class, ctx ->
                Optional.of(new StringBuilder(ctx.param()).reverse().toString()),
                ParameterTransformer.RESOLVE_EARLY));

        defaults = new LangRegistry(Map.of(
                "message.literal", parser.accept("Hello"),
                "message.greeting", parser.accept("<greeting> <name>")
        ));

        LangRegistry esp = new LangRegistry(Map.of(
                "message.literal", parser.accept("Hola"),
                "message.greeting", parser.accept("Hola, <name>")
        ));

        provider = new LangProvider() {
            @Override
            public <T> Optional<LangRegistry> get(LangManager<T> manager, String language) {
                return Optional.of(esp);
            }
        };

    }

    @Test
    public void canReturnDefault() {

        LangManager<String> manager = new LangManager<>(String.class, defaults, provider, parser, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.literal", null);
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hello", msg);
    }

    @Test
    public void canResolvePlaceholder() {

        LangManager<String> manager = new LangManager<>(String.class, defaults, provider, parser, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.greeting", null);
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hello, World", msg);
    }

    @Test
    public void canReturnLanguage() {

        LangManager<String> manager = new LangManager<>(String.class, defaults, provider, parser, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.literal", "other");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hola", msg);
    }

    @Test
    public void canResolvePlaceholderForLanguage() {

        LangManager<String> manager = new LangManager<>(String.class, defaults, provider, parser, MessagePipeline.RESOLVE_STRING);

        String msg = manager.getMessage("message.greeting", "other");
        Assertions.assertNotNull(msg);
        Assertions.assertEquals("Hola, World", msg);
    }

}
