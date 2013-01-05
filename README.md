# Wart Remover

This project aims to clean up some of Scala's warts by shadowing
things or making implicits ambiguous.

## Usage

`WartRemover` is meant to by extended by a `package object` for your
project's package (via the special `package.scala`). For example:

    import org.brianmckenna.wartremover.WartRemover

    package object com.precog extends WartRemover

You don't have to do anything else to your project to get more safety!

## Warts

### any2stringadd

What do you expect the following to do?

    println({} + "test")

Print `()test`, of course... Scala has an implicit which will convert
anything to a String if the right side of `+` is a String. Get some
type safety back by making this not compile.
