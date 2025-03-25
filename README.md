# Pseudonym
*A placeholder system for Java projects*

<br/>

## Usage

`MessagePipeline<I, O>`: An interface for transforming `I`'s into `O`'s, according to a `PipelineContext` object. They 
can be created using the `MessagePipeline.Builder<I, O>` class. For example:
```java
MessagePipeline<Object, String> toStringPipeline = 
        MessagePipeline.<String>builder()
                .add((i, ctx) -> Objects.toString(i))
                .build();

int i = 10;
String o = toStringPipeline.accept(i); // Will become "10"
```
This example pipeline converts any object given to it into a String by calling toString on it. This pipeline is simple,
but much more complicated pipelines can be created.

`PipelineContext`: Simply contains a list of objects. These can be accessed by index or by class. For example:
```java
PipelineContext ctx = PipelineContext.of(new Player("Steve"));
Optional<Player> pl = ctx.getFirst(Player.class);
```
<br/>

`UnresolvedMessage<T>`: An object which contains a list of objects that are either `T` or a `PlaceholderInstance<T, P>`.

`PlaceholderInstance<T, P>`: An instance of a `Placeholder<T, P>` (see below.) Contains a reference to the `Placeholder<T, P>`,
and a (nullable) parameter of type `P`

`Placeholder<T,P>`: Represents a placeholder which resolves to a `T`. Contains a name, some logic (`PlaceholderSupplier<T, P>`) 
to resolve an instance of itself according to a `ResolveContext<P>` object, and some logic (`ParameterTransformer<P>`) 
to convert a parsed text parameter into a `P` (if necessary.)

A simple placeholder (without a parameter) can be created as follows:
```java
Placeholder<String, Void> simple = Placeholder.of("name", String.class, ctx -> Optional.of("Steve")); 
```
This example placeholder is named "name" and will always resolve to "Steve"

As mentioned above placeholders can also have parameters. Parameters are always parsed as type `UnresolvedMessage<String>`.
Each placeholder needs to provide some logic to convert that parameter to its `P` type, at parse-time. There are two 
included transformers:
- `ParameterTransformer.IDENTITY`: Keeps the parameter unchanged. (`P` in this case must be `UnresolvedMessage<String>`)
- `ParameterTransformer.RESOLVE_EARLY`: Resolves the parameter's placeholders with an empty `ResolveContext<P>` object.
  (`P` in this case must be `String`)

A parametrized placeholder could be defined as follows:
```java
Placeholder<String, String> toUpper = Placeholder.of(
        "to_upper", 
        String.class, 
        ctx -> Optional.of(ctx.param().toUpperCase()), 
        ParameterTransformer.RESOLVE_EARLY);
```
This example placeholder will simply return its parameter, converted to upper case. Note that since `ParameterTransformer.RESOLVE_EARLY`
was used here, any nested placeholders within the parameter itself will not be resolved according to the right context.
In most cases, it will be more desirable to use `ParameterTransformer.IDENTITY` along with `UnresolvedMessage.resolve(ctx.param(), ctx.context())`
at resolve-time.

`PlaceholderManager`: Holds a mapping from strings to `Placeholder<?,?>` objects. An instance can be created using the
default constructor, and registering a placeholder is as simple as calling `register(Placeholder<?,?>)` on it.

`PlaceholderParser`: A pipeline stage for parsing placeholders from strings, according to a given `PlaceholderManager`.
One could be integrated into a pipeline as follows:
```java
PlaceholderManager manager;
Pipeline<String, UnresolvedMessage<String>> pipeline = 
        MessagePipeline.<String>builder()
                .add(new PlaceholderParser(manager))
                .build();
```

### Placeholder string format
- Parameterless placeholders are formatted as such: `<name>`
- Parametrized placeholders are formatted as such: `<to_upper>param</to_upper>`
    - Note: If a placeholder accepts a parameter, one *must* be specified.
- Parametrized placeholders can have placeholders within the parameter: `<to_upper><name></to_upper>`


`PlaceholderResolver<T>`: A pipeline stage for resolving placeholders of type `T`. It both accepts and returns an object
of type `UnresolvedMessage<T>`. However, all the placeholders of type `Placeholder<T, ?>` will be resolved in the output.

`PlaceholderStripper<T>`: A pipeline stage which removes all unresolved placeholders from the input. It accepts an
`UnresolvedMessage<T>` and returns a `List<T>`

`MessageJoiner<T>`: A pipeline stage for joining resolved message parts together. It accepts a `List<T>` and returns a `T`.
This is only an interface and needs to be implemented for each message type you use. There is a default implementation for
`String` at `MessageJoiner.STRING`


### Putting it All Together
Using all the tools listed above, you can create a pipeline which parses, resolves, strips and joins messages. One for
Strings would look something like the following:
```java
public static void main(String[] args) {
    
    PlaceholderManager manager = new PlaceholderManager();
    manager.register(Placeholder.of("name", String.class, ctx -> ctx.getFirst(Player.class).map(Player::name)));
    manager.register(Placeholder.of(
          "to_upper",
          String.class,
          ctx -> Optional.of(UnresolvedMessage.resolve(ctx.param(), ctx.context()).toUpperCase()),
          ParameterTransformer.IDENTITY));
    
    Pipeline<String, String> pipeline = 
            MessagePipeline.<String>builder()
                    .add(new PlaceholderParser(manager))
                    .add(new PlaceholderResolver<>(String.class))
                    .add(new PlaceholderStripper<>())
                    .add(MessageJoiner.STRING)
                    .build();
    
    System.out.println(pipeline.accept("Hello, <to_upper><name></to_upper>", PipelineContext.of(new Player("Steve")))); // This will print "Hello, STEVE"
}
```


### Context Placeholders
The `PipelineContext` class contains a `contextPlaceholders` field. The placeholder resolver will look in there if no
placeholder with that name could be found at parse-time.

<br/>

