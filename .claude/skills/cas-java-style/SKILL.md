---
name: cas-java-style
description: "Write Java that passes CAS Checkstyle and matches the codebase's Lombok idioms: generate constructors, accessors, builders, equals/hashCode/toString and loggers with Lombok instead of writing them by hand, lombok val/var locals, final parameters, logging, Javadoc and @since tags, import order and import control, banned APIs and annotations, size limits, test naming, and the IntelliJ inspection profile. Use whenever writing or reviewing Java in cas-server, and before running Checkstyle."
---

# Java style: Lombok and Checkstyle

The configuration is the source of truth: `style/checkstyle-rules.xml`, `style/checkstyle-suppressions.xml`,
`style/import-control.xml` and `lombok.config`. This skill summarizes what they enforce and how the codebase uses
Lombok; when in doubt, read the rule there and copy the nearest class of the same module family. How to run Checkstyle
without Gradle is in `cas-handoff`; general conventions stay in AGENTS.md.

## Lombok: generate, do not hand-write

- Never hand-write what a Lombok annotation generates. Before adding a constructor, getter, setter, `equals`,
  `hashCode`, `toString`, builder or logger, check whether the class (or a sibling in the same module family) already
  uses the annotation, and use it.
- Constructors:
  - `@RequiredArgsConstructor` for classes whose collaborators are `final` fields (the usual shape of CAS beans, actions,
    controllers and repositories, about a thousand classes). The `@Bean` method or the caller passes the arguments; do
    not write a constructor that only assigns fields.
  - `@AllArgsConstructor` / `@NoArgsConstructor` where every field, or none, is set at construction. Use
    `access = AccessLevel.PROTECTED` (or `PRIVATE`) when only frameworks or subclasses may call it, such as JPA entities
    and Jackson-built types, and `@NoArgsConstructor(force = true)` when a no-arg constructor must coexist with
    `final` fields.
  - Write a constructor by hand only when it must do work Lombok cannot: validate or derive values, or call a superclass
    constructor that takes arguments (Lombok constructors call `super()` only). Even then, keep it to that work.
- Accessors: `@Getter` and `@Setter` on the class or on individual fields, with `@Accessors(chain = true)` where the
  family uses fluent setters (configuration properties classes and most models do). Restrict visibility with
  `@Getter(AccessLevel.PROTECTED)` / `@Setter(AccessLevel.NONE)` rather than writing the method. Configuration
  properties classes are `@Getter @Setter @Accessors(chain = true)` with `@RequiresModule` (nearly all of them).
- Value objects: `@ToString` and `@EqualsAndHashCode`, with `callSuper = true` in subclasses and `of = ...`/
  `@ToString.Exclude` to leave out secrets and large fields. `lombok.config` sets `doNotUseGetters=true` for both, so
  they read fields directly. Class-level `@Data` and `@Value` are rare here; prefer the explicit annotations the
  neighbouring classes use, or a `record` for an immutable carrier.
- Builders: `@SuperBuilder` across class hierarchies (every class in the hierarchy carries it), `@Builder` on a single
  class or record, `@Builder.Default` for fields with an initializer (without it the builder ignores the initializer),
  and `@Jacksonized` when Jackson must deserialize through the builder (`lombok.config` targets Jackson 3).
- Static helpers: `@UtilityClass` (as `FunctionUtils` does), not a `final` class with a private constructor.
- Loggers: `@Slf4j`. `lombok.config` names the field `LOGGER` and makes it static; Checkstyle refuses a declared
  `Logger` field and a non-static logger.
- Locals: `val` for final locals (including generic collections and arrays) and `var` (or `val`) elsewhere; Checkstyle
  refuses `final var`, a `final` local with a generic type, and an explicitly typed local of a simple type unless it is
  initialized to `null`. Declare the type only where `val` cannot infer it (see the Lombok `val` notes under
  Conventions in AGENTS.md). Enhanced `for` loops use `val`/`var` too.
- Not allowed or discouraged: `@SneakyThrows` (Checkstyle; use the `FunctionUtils` helpers for checked exceptions),
  `@Cleanup` (prefer try-with-resources), `@Synchronized` and `synchronized` (use `CasReentrantLock`).
- `lombok.config` adds `@lombok.Generated` to generated code, which keeps it out of coverage reports; do not hand-write
  generated-looking code to "help" coverage.

## Declarations and parameters

- Method, constructor and catch parameters are `final` (`FinalParameters`); unused ones are named `_`.
- No inline `//` comments in code (`singlelineComment`); explanation goes in Javadoc. `// CHECKSTYLE:OFF`/`ON` markers
  are the only line comments the rule lets through.
- `HiddenField` applies to local variables (constructor and setter parameters may shadow fields).
- Static fields are `UPPER_SNAKE_CASE`; lambda parameters are camel case or `_`; abstract classes are named
  `Abstract*`, `Base*`, `Root*` or `*Factory`; test classes end in `Tests` (never `Test`), suites in `TestSuite`, and
  test methods do not start with `test`.
- `ExplicitInitialization`: do not initialize fields to their default value. `MagicNumber` allows -1 to 10, 100 and
  1000 outside tests; name other values as constants.
- Annotations sit on their own line before the declaration (a single parameterless one may share the line).
- `OverloadMethodsDeclarationOrder`: overloads stay next to each other. `DeclarationOrder`: static fields, instance
  fields, constructors, methods.

## Spring and dependency injection

- `@RefreshScope` on a method always carries `proxyMode` (`@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)`).
- `@Autowired` is refused outside `*Configuration`, `*Controller`, `*Command`, `*WebApplication` and test classes
  (inject through the `@Bean` method instead). Where it is allowed, an injected field carries `@Qualifier`, and in main
  code a qualified field becomes an `ObjectProvider<T>`.
- No `@Component`/`@Service` in main code. Test configuration classes are `@TestConfiguration`, never `@Configuration`.
- Request mappings use `@GetMapping`/`@PostMapping`/... rather than `@RequestMapping(method = RequestMethod.X)`.
- Tests use inline `@TestPropertySource(properties = ...)`, not `locations`.

## Logging, randomness, collections, exceptions

- Log with parameters and brackets around each: `LOGGER.debug("Found [{}]", value)`. No string concatenation, no
  `isDebugEnabled()` checks, no `System.out`/`System.err`, no `printStackTrace()`, no `LogFactory.getLog`.
- Randomness through `RandomUtils` (`org.apereo.cas.util`): never `new SecureRandom()`, `java.util.Random` or
  `RandomStringUtils.random*`.
- No `Collections.emptyList/Map/Set` or `Collections.unmodifiable*` (serialization trouble): return a sized-zero
  mutable collection. No `new ArrayList<>(Arrays.asList(...))` chains or `Lists.newArrayList(...)`: use `List.of`/
  `Stream.of(...).collect(...)`. Zero-length arrays come from `ArrayUtils.EMPTY_*`. An empty `""` literal becomes
  `StringUtils.EMPTY`.
- `stream().forEach(...)` becomes `forEach(...)`. Chained stream operations go one per line, each line starting with
  `.`.
- Never catch or throw `RuntimeException` or `Error` (`IllegalCatch`, `IllegalThrows`); catch the specific exception, or
  `Exception`. Empty catch blocks fail the build. Exceptions are immutable (`MutableException`).
- `} else { return ...; }` after a returning branch is refused (`needlessElseClause`): drop the `else`.
- `org.apache.commons.lang` is refused; use `lang3`.

## Javadoc

- Every Java file, tests included, has at least one `@since` tag, and public types carry Javadoc with `@author`
  (tests and `*Properties` classes are exempt from the type Javadoc check).
  Public methods carry Javadoc (`MissingJavadocMethod`), with `@param`/`@return` for protected and wider
  (`JavadocMethod`; configuration classes and tests are exempt from that part). Javadoc text starts on the line after
  `/**`. The first sentence is a summary ending in a period (`SummaryJavadoc`); every tag has a description;
  use `{@code ...}`, never `<code>`.
- Do not add Javadoc beyond what the rules and a public contract need (AGENTS.md).

## Imports

- Order (`CustomImportOrder`): same package, third-party, `javax`/`jakarta`, `java`, then static imports; alphabetical
  within each group and no blank line between groups. No unused or redundant imports; star imports are refused except
  for the allowed static helpers (JUnit `Assertions`/`Assumptions`, Mockito, Awaitility, MockMvc builders and matchers,
  and a few `java.*` packages).
- Static imports of Mockito or JUnit assertions are star imports (`import static org.mockito.Mockito.*;`), never one
  method at a time.
- `import-control.xml`: JUnit 5 only (`org.junit.Assert`, JUnit 4 runners and categories are refused outside
  `org.apereo.cas.util.junit`); no `org.jetbrains.annotations`; Jackson 2 (`com.fasterxml.jackson.core`/`databind`)
  only in WebAuthn files.

## Formatting and limits

- Lines up to 190 characters, spaces only (no tabs), no trailing spaces, LF line endings with a final newline.
- No space after a cast; `OperatorWrap` puts the operator at the start of the wrapped line; braces always
  (`NeedBraces`); `default` comes last in a `switch`; no trailing comma in enums.
- Limits: files 2,600 lines, methods 180 lines (tests exempt), constructors 15 parameters, methods 20 (configuration
  classes exempt), anonymous classes 45 lines, boolean expressions 10 operators, class fan-out 130.
- Group `&&` inside `||` with parentheses (Error Prone, see AGENTS.md); `OperatorPrecedence` warnings fail the build.

## IntelliJ inspection profile

`style/intellij-idea-inspections.xml` is the IntelliJ IDEA inspection profile (import it as the project profile). The
build does not run it, but most of its inspections are set to error, so code is expected to come out clean in the IDE:

- Lambdas and method references instead of anonymous classes and block lambdas (`Convert2Lambda`, `Convert2MethodRef`,
  `CodeBlock2Expr`, `AnonymousHasLambdaAlternative`); `Comparator.comparing(...)` combinators instead of hand-written
  comparators; the stream API instead of loops that collect or filter (`Convert2streamapi`); `Map.computeIfAbsent`,
  `getOrDefault`, `merge` and friends instead of get-then-put sequences (`Java8MapApi`).
- Fields that can be `final`, `static` or local are made so; methods that use no instance state are `static`; inner
  classes that do not use the outer instance are `static` (`FieldMayBeFinal`, `FieldMayBeStatic`, `FieldCanBeLocal`,
  `MethodMayBeStatic`, `InnerClassMayBeStatic`). `final` on a private or static method, or a method of a final class,
  is redundant and flagged.
- `Serializable` classes, inner ones included, declare `serialVersionUID` (annotated `@Serial` as the codebase does).
- Collections and strings: `isEmpty()` instead of `size() == 0` or `equals("")`, `toArray(new T[0])`, bulk operations
  (`addAll`, constructors taking a collection) instead of element loops, `entrySet()` when iterating a map for keys and
  values, `EnumSet`/`EnumMap` for enum keys, no string concatenation in loops, no `new String(...)`/`new Boolean(...)`,
  no redundant `toString()`, `String.format` or temporaries in conversions.
- `String.matches`, `replaceAll`, `replaceFirst` and `split` with a literal regular expression become a precompiled
  `Pattern` constant (`DynamicRegexReplaceableByCompiledPattern`); `replaceAll(".")` is flagged.
- No `synchronized` on `this`, a local, a parameter or a non-final field (CAS avoids `synchronized` altogether);
  no `System.gc()`, `Thread.dumpStack()`, `printStackTrace()` or `System.out`/`err`.
- Logging calls have as many arguments as `{}` placeholders (`PlaceholderCountMatchesArgumentCount`); one logger per
  class.
- Results of calls on immutable types (`String`, boxed numbers, `BigInteger`, `URI`, `UUID`, `Pattern`, ...) are used,
  never ignored; empty `catch`, `finally`, `try` blocks and initializers are flagged.
- Nullability annotations stay consistent across overrides (`NullableProblems`); no fully qualified names where an import
  works, no `java.lang` or same-package imports, no on-demand (non-static) imports, no redundant `@SuppressWarnings`,
  no `TODO` comments.

## Where the rules relax

- `checkstyle-suppressions.xml` exempts tests (`*Tests`, `Mock*`, `Test*`) from Javadoc, magic numbers, indentation,
  method length, `forbiddenAnnotations`, console stack traces and the collection rules, and exempts `*Configuration`
  classes from the method parameter limit. Do not lean on a suppression in main code; fix the code instead.
