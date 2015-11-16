package models.buildActions

case class BuildParametersCategory(name: String, parts: List[String], params: Map[String, String] = Map.empty)
