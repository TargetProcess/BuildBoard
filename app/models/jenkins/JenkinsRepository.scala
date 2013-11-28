package models.jenkins


object JenkinsRepository {
  val jenkinsUrl = "http://jenkinsmaster-hv:8080"
/*
  case class Parameter(name: String, value: String)
  case class Action(parameters: Option[List[Parameter]])

  private implicit val parameterReads: Reads[Parameter] = (
      (__ \ "name").read[String] ~
      (__ \ "value").read[String]
    )(Parameter)


//  val r1:Reads[Option[List[Parameter]]] =

  private implicit val actionReads:Reads[Action] = (__ \ "parameters").readNullable(list[Parameter]).map(Action)

  private implicit val buildReads: Reads[Build] = (
      (__ \ "number").read[Int] ~
      (__ \ "timestamp").read[Long].map(new DateTime(_)) ~
      (__ \ "result").read[String] ~
      (__ \ "url").read[String] ~
      (__ \ "actions").read(list[Action]).map( (x: List[Action]) => {
        x.map((a: Action) => a.parameters.map(params => params.filter(p => p.name == "ghprbPullId")))
          .flatten
          .flatMap(a => a).head.value
      })
    )((number, timestamp, buildResult, url, pullRequestId) => {
    Build(pullRequestId, buildResult, url, timestamp, number)
  })

  def getBuilds = Try {
    val url = s"$jenkinsUrl/job/BuildPullRequest/api/json?depth=2&tree=builds[url,actions[parameters[name,value],lastBuiltRevision[branch[name]]],number,result,timestamp]"
    val response = Http(url)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .asString
    val json = Json.parse(response)

    json.validate((__ \ "builds").read(list[Build])).get
  } getOrElse Nil
  */
}
