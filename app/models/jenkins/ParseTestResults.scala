package models.jenkins

import java.io.File

import models.{Artifact, TestCase, TestCasePackage}

import scala.runtime.Nothing$
import scala.xml.{Node, XML}

trait ParseTestResults extends Artifacts {
  protected val screenshotQualifiedFileNameRegex = """.*\\(\w+)\.(\w+)[-].*$""".r
  protected val screenshotFileNameRegex = """.*\\(\w+)[-].*$""".r

  def getFailedTestCases(artifacts: List[Artifact]) : List[TestCase] = {
    def getFailedTestCasesInner(testCasePackage: TestCasePackage): List[TestCase] = {
      testCasePackage.testCases.filter(testCase => testCase.result.toLowerCase() != "success") ++
        testCasePackage.packages.map(getFailedTestCasesInner).flatten
    }

    getTestCasePackages(artifacts)
      .map(testCasePackage => getFailedTestCasesInner(testCasePackage))
      .flatten
  }

  def getTestCasePackages(artifacts: List[Artifact]): List[TestCasePackage] = {
    val screenshots = artifacts.filter(a => a.name == screenshot)

    def getNUnitTestCasePackage(xml: String): List[TestCasePackage] = {
      def getTestCasePackage(node: Node): TestCasePackage = {
        def getTestCasePackageInner(node: Node, namespace: String = ""): TestCasePackage = {

          val name = node.attribute("name").get.head.text
          val currentNamespace = getAttribute(node, "type") match {
            case Some("Namespace") => if (namespace.isEmpty) name else s"$namespace.$name"
            case _ => namespace
          }

          val children = (node \ "results" \ "test-suite")
            .filter(n => getAttribute(n, "result").get != "Inconclusive")
            .map(n => getTestCasePackageInner(n, currentNamespace))
            .toList

          val testCases = (node \ "results" \ "test-case").map(tcNode => {
            val executed = getAttribute(tcNode, "executed").get.toBoolean
            val result = if (!executed) "Ignored" else if (getAttribute(tcNode, "success").get != "True") "Failure" else "Success"
            val (message, stackTrace) = if (result == "Failure")
              ((tcNode \\ "message").headOption.map(_.text), (tcNode \\ "stack-trace").headOption.map(_.text))
            else (None, None)
            val tcName: String = getAttribute(tcNode, "name").get

            val tcScreenshots = tcName match {
              case testNameRegex(className, methodName) =>
                screenshots.filter(s => s.url match {
                  case screenshotQualifiedFileNameRegex(scrClassName, scrMethodName) => className == scrClassName && methodName == scrMethodName
                  case screenshotFileNameRegex(scrMethodName) => methodName == scrMethodName
                  case _ => false
                }).map(s => Artifact(s"$className.$methodName", s.url))
              case _ => Nil
            }

            TestCase(tcName, result, getAttribute(tcNode, "time").map(_.toDouble).getOrElse(0.0), message, tcScreenshots, stackTrace)
          }).toList

          TestCasePackage(if (currentNamespace.isEmpty) name else s"$namespace.$name", children, testCases)
        }

        getTestCasePackageInner(node)
      }

      (XML.loadString(xml) \ "test-suite").map(getTestCasePackage).toList
    }

    def getJUnitTestCasePackage(xml: String): List[TestCasePackage] = {
      def getTestCasePackage(node: Node): TestCasePackage = {
        def getTestCasePackageInner(node: Node, namespace: String = ""): TestCasePackage = {

          val name = node.attribute("name").map(a => a.head.text).getOrElse("All")
          val testCases = (node \ "testcase").map(tcNode => {
            val executed = (tcNode \ "skipped").length == 0
            val error = tcNode \ "error"
            val failure = tcNode \ "failure"
            val failureNode: Option[Node] = (if (error.length > 0) error else failure).headOption
            val result = if (!executed) "Ignored" else if (failureNode.isDefined) "Failure" else "Success"

            val (message, stackTrace) = failureNode.map(node => (getAttribute(node, "message"), Some(node.text))).getOrElse(None, None)
            val tcName: String = s"${getAttribute(tcNode, "classname").get}.${getAttribute(tcNode, "name").get}"

            TestCase(tcName, result, getAttribute(tcNode, "time").map(_.toDouble).getOrElse(0.0), message = message, stackTrace = stackTrace)
          }).toList

          TestCasePackage(name, Nil, testCases)
        }

        getTestCasePackageInner(node)
      }

      XML.loadString(xml).map(getTestCasePackage).toList
    }

    val nunitParser: PartialFunction[String, List[TestCasePackage]] = {
      case xml => getNUnitTestCasePackage(xml)
    }
    val junitParser: PartialFunction[String, List[TestCasePackage]] = {
      case xml => getJUnitTestCasePackage(xml)
    }

    val testResultAdapters = Map(
      ("nunit", nunitParser),
      ("junit", junitParser)
    )

    artifacts
      .find(a => a.name == "testResults")
      .map(file => (file, FileApi.read(this.getArtifact(file.url))))
      .flatMap(fileXmlPair => {
        val fileName: String = new File(fileXmlPair._1.url).getName
        testResultAdapters
          .find(pair => fileName.toLowerCase.startsWith(pair._1))
          .flatMap(pair => fileXmlPair._2.map(xml => pair._2(xml)))
      })
      .getOrElse(Nil)
  }
}