import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.plib.text.Component;
import org.wallentines.plib.text.Style;
import org.wallentines.plib.text.TextColor;

public class TestComponents {

    @Test
    public void canParseString() {

        String toParse = "Hello, World";
        Component cmp = Component.SERIALIZER.deserialize(ConfigContext.INSTANCE, new ConfigPrimitive(toParse)).getOrThrow();

        Assertions.assertEquals(Component.text("Hello, World"), cmp);
    }

    @Test
    public void canParseList() {

        ConfigList toParse = ConfigList.of("Hello, ", "World");
        Component cmp = Component.SERIALIZER.deserialize(ConfigContext.INSTANCE, toParse).getOrThrow();

        Assertions.assertEquals(Component.text("Hello, ").append("World"), cmp);
    }

    @Test
    public void canParseSection() {

        ConfigSection toParse = new ConfigSection().with("text", "Hello, World");
        Component cmp = Component.SERIALIZER.deserialize(ConfigContext.INSTANCE, toParse).getOrThrow();

        Assertions.assertEquals(Component.text("Hello, World"), cmp);
    }

    @Test
    public void canParseColored() {

        ConfigSection toParse = new ConfigSection().with("text", "Hello, World").with("color", "red");
        Component cmp = Component.SERIALIZER.deserialize(ConfigContext.INSTANCE, toParse).getOrThrow();

        Assertions.assertEquals(Component.text("Hello, World").withStyle(Style.EMPTY.withColor(TextColor.RED)), cmp);
    }

}
