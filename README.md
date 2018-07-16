# WartRemover

[![Build Status](https://travis-ci.org/wartremover/wartremover.svg?branch=master)](https://travis-ci.org/wartremover/wartremover)
[![scaladoc](https://javadoc-badge.appspot.com/org.wartremover/wartremover_2.12.svg?label=scaladoc)](https://javadoc-badge.appspot.com/org.wartremover/wartremover_2.12/org/wartremover/index.html?javadocio=true)

[![Join us on gitter](http://badges.gitter.im/wartremover/Lobby.png)](https://gitter.im/wartremover/Lobby)

WartRemover is a flexible Scala code linting tool.

## Documentation

Documentation for Wartremover is available [here](http://www.wartremover.org).

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
