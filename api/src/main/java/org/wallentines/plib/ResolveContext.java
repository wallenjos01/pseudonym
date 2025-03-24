package org.wallentines.plib;

public record ResolveContext<T, P>(PlaceholderContext context, PlaceholderInstance<T, P> placeholder) {
}
