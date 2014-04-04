package models.jenkins

import play.api.Play
import scala.util.Try
import scalaj.http.Http

trait JenkinsApi {

  private val jenkinsUrl = Play.configuration.getString("jenkins.url").get
  protected val rootJobName = "StartBuild"

  def forceBuild(action: models.BuildAction) = Try {
    val url = s"$jenkinsUrl/job/$rootJobName/buildWithParameters"

    play.Logger.info(s"Force build to $url with parameters ${action.parameters}")

    Http.post(url)
      .params(action.parameters)
      .asString
  }
}
