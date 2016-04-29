package models.jenkins

import java.io.File

import components._
import models.buildActions.{BranchBuildAction, JenkinsBuildAction, PullRequestBuildAction, ReuseArtifactsBuildAction}
import models.configuration.CycleParameters
import models.cycles.CycleConstants

import scala.util.Try
import scalaj.http.{Http, HttpOptions}

trait ForceBuildComponentImpl extends ForceBuildComponent {
  this: ForceBuildComponentImpl
    with BranchRepositoryComponent
    with BuildRepositoryComponent
    with LoggedUserProviderComponent
    with ConfigComponent
  =>

  val forceBuildService = new ForceBuildServiceImpl


  class ForceBuildServiceImpl extends ForceBuildService {

    lazy val directory = config.jenkinsDataPath

    def post(url: String, parameters: List[(String, String)]) = Try {

      play.Logger.info(s"Force build to $url with parameters $parameters")

      Http.post(url)
        .params(parameters)
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }

    override def forceBuild(action: JenkinsBuildAction) = action match {
      case x: ReuseArtifactsBuildAction => forceReuseArtifactsBuild(x)
      case x: JenkinsBuildAction => forceSimpleBuild(x)
    }


    def forceSimpleBuild(action: JenkinsBuildAction) = {
      val branch = action match {
        case PullRequestBuildAction(prId, _) => branchRepository.getBranchByPullRequest(prId).map(_.name)
        case BranchBuildAction(name, _) => Some(name)
        case _ => None
      }

      val lastBuild = branch.flatMap(buildRepository.getLastBuilds(_, 1).headOption)
      val url = s"${config.jenkinsUrl}/job/${action.jobName}/buildWithParameters"

      val parameters = action.parameters ++
        loggedUser.map("WHO_STARTS" -> _.fullName) ++
        List("DESCRIPTION" -> action.name) ++
        lastBuild.flatMap(_.ref).map("PREVIOUS_COMMIT" -> _.trim)

      post(url, parameters)
    }

    def forceReuseArtifactsBuild(action: ReuseArtifactsBuildAction): Try[Unit] = Try {
      val buildFolder = new File(s"$directory/${action.buildName}")
      val revision = FileApi.readAsMap(new File(buildFolder, "Artifacts/Revision.txt"))
        .flatMap(
          _.values
            .filter(_.toString.startsWith("REVISION="))
            .map(_.replaceAll("REVISION=", ""))
            .headOption
        )
        .get

      val buildParams = BuildParams(getParamsFile(buildFolder)).get

      val cycleParameters: CycleParameters = action.cycle.config

      def forcePart(job: String, postfix: String, params: Map[String, String] = Map.empty): Unit = {
        forceRerunBuildCategory(s"${config.jenkinsUrl}/job/$job/buildWithParameters", buildParams, revision, action.buildNumber, postfix, params)
      }
      def forcePartWithFilter(job: String, postfix: String, filterKey: String, filter: List[String]): Unit = {
        if (filter.nonEmpty)
          forcePart(job, postfix, Map((filterKey, filter.mkString(" ").replaceAll( """^\"|\"$""", ""))))
      }


      CycleConstants.allTestCategories.values.foreach(category => {
        forcePartWithFilter(category.runName, category.postfix, category.filter, cycleParameters.tests(category))
      })

      forcePart("CometOutOfProcess", "FuncTests")
      forcePart("RunSliceLoadTest", "FuncTests")

      if (cycleParameters.includeDb) {
        forcePart("RunDBTest", "FuncTests")
      }

      if (cycleParameters.includePerfTests) {
        val parameters: Map[String, String] = action.cycle.getParamsByCategory(CycleConstants.perfCategoryName)
        forcePart("RunPerfTests", "PerfTests", parameters)
      }
    }


    def forceRerunBuildCategory(url: String, buildParams: BuildParams, revision: String, buildNumber: Int, buildPathPostfix: String, additionalParameters: Map[String, String]) = {


      val params: List[(String, String)] = buildParams.parameters.flatMap {
        case ("Cycle", value) => Some("CYCLE", value)
        case ("BUILDPATH", value) => Some("BUILDPATH", s"$value\\$buildPathPostfix")
        case ("LOCAL_BRANCH", value) => Some("LOCALREPONAME", value)
        case ("ARTIFACTS", value) => Some("ARTIFACTS", value)
        case _ => None
      }.toList ++
        List(
          ("VERSION", revision + "." + buildNumber),
          ("BUILDPRIORITY", "10"),
          ("RERUN", "true")
        )

      post(
        url,
        params ++ additionalParameters.filter(p => p._2 != "")
      )

    }

    def getParamsFile(folder: File): File = {
      new File(folder, "Build/StartBuild/StartBuild.params")
    }
  }

}