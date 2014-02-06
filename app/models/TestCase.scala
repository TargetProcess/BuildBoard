package models

case class TestCasePackage(name: String, packages: List[TestCasePackage] = Nil, testCases: List[TestCase] = Nil)
case class TestCase(name: String, result: String, duration: Double, message: Option[String] = None, screenshots: List[Artifact] = Nil, stackTrace: Option[String] = None)
