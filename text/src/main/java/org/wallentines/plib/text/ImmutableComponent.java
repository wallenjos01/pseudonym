package org.wallentines.plib.text;

import java.util.List;

public record ImmutableComponent(Content content, Style style, List<Component> children) implements Component { }
