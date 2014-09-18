package components

import java.io.File

import models._
import models.branches.Branch
import models.buildActions.BuildAction

//import models.buildActions.{BranchWithArtifactsReuseCustomBuildAction, BuildAction}

import scala.util.Try

trait JenkinsServiceComponent {

  val jenkinsService: JenkinsService


  trait JenkinsService {
   // def getCustomBuildActions(branch: String) : Map[Int, List[BuildAction]]

    def forceBuild(action: BuildAction): Try[String]

    //def forceReuseArtifactsBuild(action: BranchWithArtifactsReuseCustomBuildAction): Try[String]

    def getUpdatedBuilds(existingBuilds:List[Build], buildNamesToUpdate: Seq[String]):List[Build]

    def getTestRun(branch: Branch, buildNumber: Int, part: String, run: String) : Option[BuildNode]

    def getArtifact(file: String): File
  }

}
