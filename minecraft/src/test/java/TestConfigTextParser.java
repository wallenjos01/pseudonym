import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.pseudonym.mc.ConfigTextParser;

public class TestConfigTextParser {

    @Test
    public void canParseUnformatted() {
        String toParse = "Hello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World"), cmp);
    }

    @Test
    public void canParseColored() {
        String toParse = "&cHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED), cmp);
    }


    @Test
    public void canParseFormatted() {
        String toParse = "&lHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.BOLD), cmp);
    }

    @Test
    public void canParseFormattedAndColored() {
        String toParse = "&c&lHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cmp);
    }
    @Test
    public void canParseDelayedFormattedAndColored() {
        String toParse = "&cHello, &lWorld";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.BOLD)), cmp);
    }

    @Test
    public void canParseMultiColored() {
        String toParse = "&cHello, &6World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(
                        Component.literal("World").withStyle(ChatFormatting.GOLD)
                ), cmp);
    }

    @Test
    public void canParseMultiColoredAndFormatted() {
        String toParse = "&c&oHello, &6World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.literal("Hello, ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.literal("World").withStyle(ChatFormatting.GOLD)),
                cmp);
    }

    @Test
    public void canParseWithReset() {
        String toParse = "&c&oHello, &rWorld";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.empty()
                        .append(Component.literal("Hello, ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                        .append(Component.literal("World")),
                cmp);
    }

    @Test
    public void canParseShadowColor() {
        String toParse = "&c:aHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                        .withShadowColor(0xFF000000 | ChatFormatting.GREEN.getColor())), cmp);
    }
    @Test
    public void canParseJustShadowColor() {
        String toParse = "&:aHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World")
                .withStyle(Style.EMPTY
                        .withShadowColor(0xFF000000 | ChatFormatting.GREEN.getColor())), cmp);
    }

    @Test
    public void canParseHexColor() {
        String toParse = "&#123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(Style.EMPTY.withColor(0x123ABC)), cmp);

    }

    @Test
    public void canParseHexShadowColor() {
        String toParse = "&#123ABC:#CC123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(Style.EMPTY.withColor(0x123ABC).withShadowColor(0xCC123ABC)), cmp);
    }

    @Test
    public void canParseJustHexShadowColor() {
        String toParse = "&:#CC123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(Style.EMPTY.withShadowColor(0xCC123ABC)), cmp);
    }


    @Test
    public void canSerializeEmpty() {
        Component toWrite = Component.empty();
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("", out);
    }

    @Test
    public void canSerializeSimple() {
        Component toWrite = Component.literal("Hello, World");
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("Hello, World", out);
    }

    @Test
    public void canSerializeColored() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.RED);
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, World", out);
    }

    @Test
    public void canSerializeMultiColored() {
        Component toWrite = Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.GOLD));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, &#FFAA00World", out);
    }

    @Test
    public void canSerializeFormatted() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.BOLD);
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&lHello, World", out);
    }

    @Test
    public void canSerializeFormattedAndColored() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555&lHello, World", out);
    }

    @Test
    public void canSerializeDelayedFormattedAndColored() {
        Component toWrite = Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.BOLD));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, &lWorld", out);
    }

    @Test
    public void canSerializeShadowColor() {
        Component toWrite = Component.literal("Hello, World").withStyle(Style.EMPTY.withColor(0x123ABC).withShadowColor(0xCC123ABC));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#123ABC:#CC123ABCHello, World", out);
    }

    @Test
    public void canSerializeShadowColorOnly() {
        Component toWrite = Component.literal("Hello, World").withStyle(Style.EMPTY.withShadowColor(0xCC123ABC));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&:#CC123ABCHello, World", out);
    }
}
