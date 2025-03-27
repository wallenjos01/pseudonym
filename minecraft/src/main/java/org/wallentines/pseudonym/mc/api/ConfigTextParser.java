package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.pseudonym.mc.impl.ConfigTextParserImpl;

import java.util.List;

public interface ConfigTextParser extends Serializer<Component> {

    /**
     * Writes a component to a config text string
     * @param component The component to write
     * @return A string representing the component.
     */
    String serialize(Component component);

    /**
     * Parses a single component from the given config text
     * @param text Config text to parse
     * @return A new component
     */
    Component parse(String text);

    /**
     * Parses config text as a list of components, split over reset codes ('&r')
     * @param text The text to parse
     * @return A list of parsed components
     */
    List<Component> parseSplit(String text);

    /**
     * A standard ConfigTextParser which uses '&' as the color code character and supports both hex colors and shadow colors.
     */
    ConfigTextParser INSTANCE = create('&', true, true);

    /**
     * A ConfigTextParser for legacy text, which uses '§' as the color code character and does not hex colors or shadow colors.
     */
    ConfigTextParser LEGACY = create('\u00A7', false, false);

    /**
     * Creates a new ConfigTextParser
     * @param colorChar The character to use for color codes ('&' for config text and '§' for legacy text)
     * @param hexSupport Whether this new parser should support 24-bit colors, represented as hex codes. (ex. &#123abc)
     * @param shadowSupport Whether this new parser should support setting the shadow color of the component. (ex. &6:eHello)
     * @return A new ConfigTextParser
     */
    static ConfigTextParser create(char colorChar, boolean hexSupport, boolean shadowSupport) {
        return new ConfigTextParserImpl(colorChar, hexSupport, shadowSupport);
    }
}
