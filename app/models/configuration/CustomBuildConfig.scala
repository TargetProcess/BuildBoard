package models.configuration


case class BuildConfig(cycles:List[CycleConfig],customCyclesAvailability:List[String], tests:Map[String,List[String]], unstableNodes:List[String])
case class BuildBoardConfig(branches:Map[String, String], build:BuildConfig, autoRerun:Map[String, Boolean], teams:List[Team])
case class CycleConfig(name:String,branches:List[String],parameters:CycleParameters)
case class Team(name:String, channel:String, deployTo:String)
case class CycleParameters(
                            isFull: Boolean,
                            includeUnstable: Boolean,
                            includeDb:Boolean,
                            includeComet:Boolean,
                            includeSlice:Boolean,
                            includePerfTests:Boolean,
                            buildFullPackage:Boolean,
                            casperTests:List[String],
                            karmaTests:List[String],
                            unitTests:List[String],
                            pythonFuncTests:List[String],
                            funcTests:List[String]
                          )


/*class CustomBuildConfig extends BuildBoardConfig {


  def loadFile: File = {
    play.api.Play.getFile("conf/build.json")
  }

  private def getConfigList(path: String) = config.getConfigList(path).map(_.asScala.toList).getOrElse(Nil)


  def config = play.api.Configuration(ConfigFactory.parseFileAnySyntax(loadFile))

  override def teams: List[Team] = (for (
    teamConfig <- config.getConfigList("teams").get.asScala;
    name <- teamConfig.getString("name");
    deployTo <- teamConfig.getString("deployTo");
    channel <- teamConfig.getString("channel")
  ) yield Team(name, channel, deployTo)).toList

  override def unstableNodes: List[String] = config.getStringList("build.unstableNodes").get.asScala.toList

  override def getTests(testName: String): List[String] = config.getConfig("build.tests").get.getStringList(testName).get.asScala.toList.distinct

  override def getCycleConfiguration(name: String): Configuration = {
    val list: List[Configuration] = getConfigList("build.cycles")
    list.find(x => x.getString("name").exists(_ == name)).get
  }

  override def autoRerun(name: String): Boolean = config.getConfig("autoRerun").flatMap(_.getBoolean(name)).getOrElse(false)

  override def getTestParts(category: String): List[String] = config.getConfig("build")
    .flatMap(_.getStringList(category))
    .map(_.asScala.toList)
    .getOrElse(Nil)

  override def toJson: String = config.underlying.root().render(ConfigRenderOptions.concise())

  override def replace(value: String): Unit = {
    val file = loadFile
    val fileWriter = new FileWriter(file, false)
    fileWriter.write(value)
    fileWriter.close()
  }

  override def getAllCyclesConfiguration: List[CycleConfiguration] =
    config.getConfigList("build.cycles").get.asScala.toList
      .map(config => CycleConfiguration(
        config.getString("name").get,
        config.getStringList("branches").get.asScala.toList,
        config.getConfig("parameters").get))


  override def branchRegexes: Set[(String, Regex)] = {
    config.getConfig("branches")
      .get
      .entrySet
      .map { case (name, value) => name -> value.unwrapped.toString.r }

  }
}
*/