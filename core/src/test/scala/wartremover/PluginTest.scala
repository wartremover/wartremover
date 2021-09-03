package org.wartremover

import org.scalatest.funsuite.AnyFunSuite
import warts._

import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.{Global, Settings}

class PluginTest extends AnyFunSuite {
	test("plugin parameters") {
		val settings = new Settings()
		val plugin = new Plugin(Global(settings, new ConsoleReporter(settings)))
		plugin.init(List(
			 "traverser:While; AnyVal; Any"
			,"traverser: org.wartremover.warts.Unsafe; AnyVal "
			,"only-warn-traverser:org.wartremover.warts.Unsafe; FinalCaseClass"
			,"only-warn-traverser:FinalVal;AnyVal"
			,"skip:While;org.wartremover.warts.Any;Return"
			,"excluded:file:file1"
		),println)
		assert(plugin.traversers.equals(List(
			AnyVal,
			//Any,
			AsInstanceOf,
			DefaultArguments,
			EitherProjectionPartial,
			IsInstanceOf,
			TraversableOps,
			NonUnitStatements,
			Null,
			OptionPartial,
			Product,
			//Return,
			Serializable,
			StringPlusAny,
			Throw,
			TryPartial,
			Var)),"errors")
		assert(plugin.onlyWarnTraversers.equals(List(FinalCaseClass, FinalVal)),"warns")
	}
}
