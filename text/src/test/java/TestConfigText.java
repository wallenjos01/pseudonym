import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.plib.Color;
import org.wallentines.plib.text.Component;
import org.wallentines.plib.text.ConfigTextParser;
import org.wallentines.plib.text.Style;
import org.wallentines.plib.text.TextColor;


public class TestConfigText {


    @Test
    public void canParseUnformatted() {
        String toParse = "Hello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World"), cmp);
    }

    @Test
    public void canParseColored() {
        String toParse = "&cHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withColor(TextColor.RED), cmp);
    }


    @Test
    public void canParseFormatted() {
        String toParse = "&lHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withBold(true)), cmp);
    }

    @Test
    public void canParseFormattedAndColored() {
        String toParse = "&c&lHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withColor(TextColor.RED).withBold(true)), cmp);
    }
    @Test
    public void canParseDelayedFormattedAndColored() {
        String toParse = "&cHello, &lWorld";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, ").withColor(TextColor.RED).append(Component.text("World").withStyle(Style.EMPTY.withBold(true))), cmp);
    }

    @Test
    public void canParseMultiColored() {
        String toParse = "&cHello, &6World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.text("Hello, ").withColor(TextColor.RED).append(
                        Component.text("World").withColor(TextColor.GOLD)
                ), cmp);
    }

    @Test
    public void canParseMultiColoredAndFormatted() {
        String toParse = "&c&oHello, &6World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.text("Hello, ").withColor(TextColor.RED).withStyle(Style.EMPTY.withItalic(true))
                        .append(Component.text("World").withColor(TextColor.GOLD)),
                cmp);
    }

    @Test
    public void canParseWithReset() {
        String toParse = "&c&oHello, &rWorld";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(
                Component.empty()
                        .append(Component.text("Hello, ").withColor(TextColor.RED).withStyle(Style.EMPTY.withItalic(true)))
                        .append(Component.text("World")),
                cmp);
    }

    @Test
    public void canParseShadowColor() {
        String toParse = "&c:aHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World")
                .withStyle(Style.EMPTY.withColor(TextColor.RED)
                        .withShadowColor(TextColor.GREEN)), cmp);
    }
    @Test
    public void canParseJustShadowColor() {
        String toParse = "&:aHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World")
                .withStyle(Style.EMPTY
                        .withShadowColor(TextColor.GREEN)), cmp);
    }

    @Test
    public void canParseHexColor() {
        String toParse = "&#123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withColor(new Color.RGB(0x123ABC))), cmp);

    }

    @Test
    public void canParseHexShadowColor() {
        String toParse = "&#123ABC:#CC123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withColor(new Color.RGB(0x123ABC)).withShadowColor(new Color.ARGB(0xCC123ABC))), cmp);
    }

    @Test
    public void canParseJustHexShadowColor() {
        String toParse = "&:#CC123ABCHello, World";
        Component cmp = ConfigTextParser.INSTANCE.parse(toParse);
        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withShadowColor(new Color.ARGB(0xCC123ABC))), cmp);
    }


    @Test
    public void canSerializeEmpty() {
        Component toWrite = Component.empty();
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("", out);
    }

    @Test
    public void canSerializeSimple() {
        Component toWrite = Component.text("Hello, World");
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("Hello, World", out);
    }

    @Test
    public void canSerializeColored() {
        Component toWrite = Component.text("Hello, World").withColor(TextColor.RED);
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, World", out);
    }

    @Test
    public void canSerializeMultiColored() {
        Component toWrite = Component.text("Hello, ").withColor(TextColor.RED).append(Component.text("World").withColor(TextColor.GOLD));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, &#FFAA00World", out);
    }

    @Test
    public void canSerializeFormatted() {
        Component toWrite = Component.text("Hello, World").withStyle(Style.EMPTY.withBold(true));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&lHello, World", out);
    }

    @Test
    public void canSerializeFormattedAndColored() {
        Component toWrite = Component.text("Hello, World").withStyle(Style.EMPTY.withBold(true).withColor(TextColor.RED));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555&lHello, World", out);
    }

    @Test
    public void canSerializeDelayedFormattedAndColored() {
        Component toWrite = Component.text("Hello, ").withColor(TextColor.RED).append(Component.text("World").withStyle(Style.EMPTY.withBold(true)));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#FF5555Hello, &lWorld", out);
    }

    @Test
    public void canSerializeShadowColor() {
        Component toWrite = Component.text("Hello, World").withStyle(Style.EMPTY.withColor(new Color.RGB(0x123ABC)).withShadowColor(new Color.ARGB(0xCC123ABC)));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&#123ABC:#CC123ABCHello, World", out);
    }

    @Test
    public void canSerializeShadowColorOnly() {
        Component toWrite = Component.text("Hello, World").withStyle(Style.EMPTY.withShadowColor(new Color.ARGB(0xCC123ABC)));
        String out = ConfigTextParser.INSTANCE.serialize(toWrite);
        Assertions.assertEquals("&:#CC123ABCHello, World", out);
    }
    
}
