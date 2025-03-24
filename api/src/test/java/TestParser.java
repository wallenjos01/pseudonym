import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.plib.*;

import java.util.Optional;

public class TestParser {

    private final PlaceholderManager manager = new PlaceholderManager();

    private final MessagePipeline<String, String> pipeline = MessagePipeline.<String>builder()
            .add(new PlaceholderParser(manager))
            .add(MessagePipeline.RESOLVE_STRING)
            .build();

    public TestParser() {
        manager.register(new Placeholder<>("name", String.class, PlaceholderSupplier.of("World"), null));
        manager.register(new Placeholder<>("greeting", String.class, PlaceholderSupplier.of("Hello,"), null));
        manager.register(new Placeholder<>("to_upper", String.class, ctx ->
                Optional.of(ctx.param().toUpperCase()),
                ParameterTransformer.RESOLVE_EARLY));
        manager.register(new Placeholder<>("reverse", String.class, ctx ->
                Optional.of(new StringBuilder(ctx.param()).reverse().toString()),
                ParameterTransformer.RESOLVE_EARLY));

    }

    @Test
    public void worksWithNoPlaceholders() {

        String toParse = "Hello";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals(toParse, resolved);
    }

    @Test
    public void worksWithSinglePlaceholder() {

        String toParse = "Hello, <name>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("Hello, World", resolved);
    }

    @Test
    public void worksWithMultiplePlaceholders() {
        String toParse = "<greeting> <name>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("Hello, World", resolved);
    }

    @Test
    public void worksWithParameter() {
        String toParse = "<to_upper>hello</to_upper>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("HELLO", resolved);
    }

    @Test
    public void worksWithNestedPlaceholder() {
        String toParse = "<to_upper><name></to_upper>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("WORLD", resolved);
    }

    @Test
    public void worksWithMultipleNestedPlaceholders() {
        String toParse = "<to_upper><greeting> <name></to_upper>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("HELLO, WORLD", resolved);
    }

    @Test
    public void worksWithMultipleNestedParametrizedPlaceholders() {
        String toParse = "<to_upper><reverse><greeting> <name></reverse></to_upper>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals("DLROW ,OLLEH", resolved);
    }

    @Test
    public void breaksWithWrongOrder() {
        String toParse = "<reverse><to_upper><greeting> <name></reverse></to_upper>";
        String resolved = pipeline.accept(toParse, new PlaceholderContext());

        Assertions.assertEquals(">ESREVER/<DLROW ,OLLEH", resolved);
    }



}
