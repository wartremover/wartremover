---
layout: default
title: "Wartremover"
---

# WartRemover: a flexible scala linter

WartRemover takes the pain out of writing scala by removing some of the language's nastier features.

Its main goal is to help you write safe and correct software without having to constantly double-check yourself.

If you want to get started right away, head over to [Installation & Setup]({{ site.baseurl }}{% post_url 2017-02-11-install-setup %}). If you want to see all that Wartremover has to offer check out the [full list of warts]({{ site.baseurl }}{% post_url 2017-02-11-warts %}).

```console
[error] /tmp/src/main/scala/test/Foo.scala:2: [wartremover:Null] null is disabled
[error] 	val foo = null
[error] 	          ^
[error] one error found
[error] (compile:compileIncremental) Compilation failed
```

## Project Information

Wartremover is released under the [Apache 2.0](https://choosealicense.com/licenses/apache-2.0/) license, the code is on [GitHub](https://github.com/wartremover/wartremover), the documentation is on [GitHub Pages](https://wartremover.github.io/wartremover), and releases are on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.wartremover%22). It is tested against the three latest versions of Scala (2.10, 2.11, 2.12).

## Where to Get Help

Feel free to ask questions in Wartremover's [Gitter](https://gitter.im/wartremover/Lobby#) channel.

Issues are managed using [GitHub Issues](https://github.com/wartremover/wartremover/issues).
