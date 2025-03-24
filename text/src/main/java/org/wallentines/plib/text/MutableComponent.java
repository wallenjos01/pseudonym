package org.wallentines.plib.text;

import org.wallentines.plib.Color;

import java.util.ArrayList;
import java.util.List;

public class MutableComponent implements Component {

    public Content content;
    public Style style;
    public final List<Component> children;

    public MutableComponent(Content content) {
        this.content = content;
        this.style = Style.EMPTY;
        this.children = new ArrayList<>();
    }

    public MutableComponent(Content content, Style style, List<Component> children) {
        this.content = content;
        this.style = style;
        this.children = new ArrayList<>(children);
    }

    @Override
    public Content content() {
        return content;
    }

    @Override
    public Style style() {
        return style;
    }

    @Override
    public List<Component> children() {
        return children;
    }

    public MutableComponent withContent(Content content) {
        this.content = content;
        return this;
    }

    public MutableComponent withStyle(Style style) {
        this.style = style;
        return this;
    }

    public MutableComponent withColor(Color color) {
        this.style = this.style.withColor(color);
        return this;
    }

    public MutableComponent append(Component child) {
        children.add(child);
        return this;
    }
    public MutableComponent append(String child) {
        children.add(Component.text(child));
        return this;
    }


    @Override
    public String toString() {
        return "MutableComponent [content=" + content + ", style=" + style + ", children=" + children + "]";
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Component)) return false;

        if(((Component) other).children().size() != children.size()) return false;
        if(!((Component) other).content().equals(content) && ((Component) other).style().equals(style)) return false;

        for(int i = 0; i < children.size(); i++) {
            if(!((Component) other).children().get(i).equals(children.get(i))) return false;
        }
        return true;
    }

}
