import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.plib.*;
import org.wallentines.plib.mc.TextUtil;

import java.util.Optional;

public class TestConfigPipeline {

    private final PlaceholderManager manager = new PlaceholderManager();

    private final MessagePipeline<String, Component> pipeline = MessagePipeline.<String>builder()
            .add(new PlaceholderParser(manager))
            .add(TextUtil.COMPONENT_RESOLVER)
            .build();

    private final Component displayName = Component.literal("World").withStyle(ChatFormatting.AQUA);

    public TestConfigPipeline() {
        manager.register(new Placeholder<>("name", String.class, PlaceholderSupplier.of("World"), null));
        manager.register(new Placeholder<>("greeting", String.class, PlaceholderSupplier.of("Hello,"), null));
        manager.register(new Placeholder<>("to_upper", String.class, ctx ->
                Optional.of(ctx.param().toUpperCase()),
                ParameterTransformer.RESOLVE_EARLY));
        manager.register(new Placeholder<>("reverse", String.class, ctx ->
                Optional.of(new StringBuilder(ctx.param()).reverse().toString()),
                ParameterTransformer.RESOLVE_EARLY));
        manager.register(new Placeholder<>("display_name", Component.class, PlaceholderSupplier.of(displayName), null));
    }

    @Test
    public void worksWithoutPlaceholders() {
        String toParse = "Hello, World";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World"), parsed);
    }

    @Test
    public void worksWithStringPlaceholder() {
        String toParse = "Hello, <name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World"), parsed);
    }

    @Test
    public void worksWithMultipleStringPlaceholders() {
        String toParse = "<greeting> <name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World"), parsed);
    }

    @Test
    public void worksWithComponentPlaceholder() {
        String toParse = "Hello, <display_name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, ").append(displayName), parsed);
    }

    @Test
    public void worksWithStringAndComponentPlaceholders() {
        String toParse = "<greeting> <display_name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, ").append(displayName), parsed);
    }


    @Test
    public void worksWithStyle() {
        String toParse = "&cHello, World";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED), parsed);
    }

    @Test
    public void worksWithStyleAndStringPlaceholder() {
        String toParse = "&cHello, <name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED), parsed);
    }

    @Test
    public void worksWithStyleAndMultipleStringPlaceholder() {
        String toParse = "&c<greeting> <name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED), parsed);
    }

    @Test
    public void worksWithStyleAndComponentPlaceholder() {
        String toParse = "&c<greeting> <display_name>";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(displayName), parsed);
    }

    @Test
    public void worksWithStyleAndComponentPlaceholderWithTrailingText() {
        String toParse = "&c<greeting> <display_name>!";
        Component parsed = pipeline.accept(toParse, new PipelineContext());
        Assertions.assertEquals(Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(displayName.copy()).append("!"), parsed);
    }

    @Test
    public void canParseReset() {

        String toParse = "&cHello, &r<display_name>!";
        Component parsed = pipeline.accept(toParse, new PipelineContext());

        Assertions.assertEquals(
                Component.empty()
                        .append(Component.literal("Hello, ").withStyle(ChatFormatting.RED))
                        .append(displayName.copy())
                        .append(Component.literal("!")),
                parsed);
    }

    @Test
    public void canParseResetAfterPlaceholder() {

        String toParse = "&cHello, <display_name>&r!";
        Component parsed = pipeline.accept(toParse, new PipelineContext());

        Assertions.assertEquals(
                Component.empty()
                        .append(Component.literal("Hello, ").withStyle(ChatFormatting.RED)
                                .append(displayName.copy())
                        )
                        .append(Component.literal("!")),
                parsed);
    }
}
