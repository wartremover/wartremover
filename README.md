# WartRemover

[![scaladoc](https://javadoc.io/badge2/org.wartremover/wartremover_2.13/javadoc.svg)](https://javadoc.io/doc/org.wartremover/wartremover_2.13/latest/org/wartremover/warts/index.html)
[![Maven Central](https://img.shields.io/maven-central/v/org.wartremover/wartremover_2.13?label=Maven%20Central&color=%236DBE42)](https://search.maven.org/search?q=g:%22org.wartremover%22%20AND%20a:%22wartremover_2.13%22)

[![Join us on gitter](https://badges.gitter.im/repo.svg)](https://gitter.im/wartremover/Lobby)

WartRemover is a flexible Scala code linting tool.

## Documentation

Documentation for Wartremover is available [here](https://www.wartremover.org).

## Reporting Issues

It's very useful to get the tree expanded by the Scala compiler,
rather than the original source. Adding the `-Xprint:typer` flag to
the Scala compiler will show code like the following:

```scala
// println("Hello world")
package $line4 {
  object $read extends scala.AnyRef {
    def <init>(): $line4.$read.type = {
      $read.super.<init>();
      ()
    };
    object $iw extends scala.AnyRef {
      def <init>(): type = {
        $iw.super.<init>();
        ()
      };
      object $iw extends scala.AnyRef {
        def <init>(): type = {
          $iw.super.<init>();
          ()
        };
        private[this] val res1: Unit = scala.this.Predef.println("Hello world");
        <stable> <accessor> def res1: Unit = $iw.this.res1
      }
    }
  }
}
```

Adding the generated code to an issue is very useful for debugging.

## License

[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
