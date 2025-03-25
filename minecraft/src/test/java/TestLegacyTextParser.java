import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.pseudonym.mc.ConfigTextParser;

public class TestLegacyTextParser {


    @Test
    public void canParseUnformatted() {
        String toParse = "Hello, World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World"), cmp);
    }

    @Test
    public void canParseColored() {
        String toParse = "\u00A7cHello, World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED), cmp);
    }


    @Test
    public void canParseFormatted() {
        String toParse = "\u00A7lHello, World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.BOLD), cmp);
    }

    @Test
    public void canParseFormattedAndColored() {
        String toParse = "\u00A7c\u00A7lHello, World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, World").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), cmp);
    }
    @Test
    public void canParseDelayedFormattedAndColored() {
        String toParse = "\u00A7cHello, \u00A7lWorld";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.BOLD)), cmp);
    }

    @Test
    public void canParseMultiColored() {
        String toParse = "\u00A7cHello, \u00A76World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(
                Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(
                        Component.literal("World").withStyle(ChatFormatting.GOLD)
                ), cmp);
    }

    @Test
    public void canParseMultiColoredAndFormatted() {
        String toParse = "\u00A7c\u00A7oHello, \u00A76World";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(
                Component.literal("Hello, ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                        .append(Component.literal("World").withStyle(ChatFormatting.GOLD)),
                cmp);
    }

    @Test
    public void canParseWithReset() {
        String toParse = "\u00A7c\u00A7oHello, \u00A7rWorld";
        Component cmp = ConfigTextParser.LEGACY.parse(toParse);
        Assertions.assertEquals(
                Component.empty()
                        .append(Component.literal("Hello, ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                        .append(Component.literal("World")),
                cmp);
    }


    @Test
    public void canSerializeEmpty() {
        Component toWrite = Component.empty();
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("", out);
    }

    @Test
    public void canSerializeSimple() {
        Component toWrite = Component.literal("Hello, World");
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("Hello, World", out);
    }

    @Test
    public void canSerializeColored() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.RED);
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("\u00A7cHello, World", out);
    }

    @Test
    public void canSerializeMultiColored() {
        Component toWrite = Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.GOLD));
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("\u00A7cHello, \u00A76World", out);
    }

    @Test
    public void canSerializeFormatted() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.BOLD);
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("\u00A7lHello, World", out);
    }

    @Test
    public void canSerializeFormattedAndColored() {
        Component toWrite = Component.literal("Hello, World").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("\u00A7c\u00A7lHello, World", out);
    }

    @Test
    public void canSerializeDelayedFormattedAndColored() {
        Component toWrite = Component.literal("Hello, ").withStyle(ChatFormatting.RED).append(Component.literal("World").withStyle(ChatFormatting.BOLD));
        String out = ConfigTextParser.LEGACY.serialize(toWrite);
        Assertions.assertEquals("\u00A7cHello, \u00A7lWorld", out);
    }
    
}
