package models.jenkins

import components._
import models.buildActions._
import models.{Branch, Build}



trait JenkinsServiceComponentImpl extends JenkinsServiceComponent {
  this: JenkinsServiceComponentImpl
    with BranchRepositoryComponent
    with BuildRepositoryComponent
    with LoggedUserProviderComponent
    with NotificationComponent
    with ConfigComponent
    with CycleBuilderComponent
  =>

  val jenkinsService: JenkinsService = new JenkinsServiceImpl

  class JenkinsServiceImpl extends JenkinsService with ParseFolder {

    lazy val directory = config.jenkinsDataPath

    def unstableNodeNames = config.buildConfig.build.unstableNodes

    override def getUpdatedBuilds(existingBuilds: List[Build], buildNamesToUpdate: Seq[String]): Stream[Build] = {

      val existingBuildsMap: Map[String, Build] = existingBuilds.map(x => (x.name, x)).toMap


      val folders = if (buildNamesToUpdate.nonEmpty) {
        buildNamesToUpdate.map(x => new Folder(directory, x))
      }
      else {
        val allFolders = new Folder(directory).listFiles().filter(_.isDirectory).toList
        val newFolders: List[Folder] = allFolders.filterNot(x => existingBuildsMap.get(x.getName).isDefined)
        val foldersToUpdate: List[Folder] = existingBuilds.filter(_.status.isEmpty).map(x => new Folder(directory, x.name))
        newFolders ++ foldersToUpdate
      }

      val buildSources: Stream[BuildSource] = folders.distinct.toStream
        .flatMap(createBuildSource)

      val result = buildSources.flatMap(buildSource => {
        val name: String = buildSource.folder.getName
        val toggled = existingBuildsMap.get(name).fold(false)(_.toggled)
        getBuild(buildSource, toggled)
      })

      result
    }

    def getTestRun(branch: Branch, build: Int, part: String, run: String) =
      findBuild(branch, build)
        .flatMap(b => b.getTestRunBuildNode(part, run))
        .map(testRunBuildNode => testRunBuildNode.copy(testResults = getTestCasePackages(testRunBuildNode)))


    override def getBuildActions(build: Build) = List(ReuseArtifactsBuildAction(build.name, build.number, cycleBuilder.emptyCustomCycle))


    def getBuildNumbers(name: String): Option[(Int, Option[Int])] = {
      val prR = """pr_(\d+)_(\d+)""".r
      val buildR = """.*_(\d+)$""".r
      name match {
        case prR(prID, build) => Some((build.toInt, Some(prID.toInt)))
        case buildR(build) => Some((build.toInt, None))
        case _ => None
      }
    }

    def createBuildSource(folder: Folder): Option[BuildSource] = {
      val paramsFile = getParamsFile(folder)

      for {
        buildParams <- BuildParams(paramsFile)
        (buildNumber, prId) <- getBuildNumbers(folder.getName)
        branch <- prId.fold(branchRepository.getBranch(buildParams.branch.substring(7)))(f = id => branchRepository.getBranchByPullRequest(id))

      } yield BuildSource(branch.name, buildNumber, prId, folder, buildParams)
    }

    def findBuild(branch: Branch, buildId: Int): Option[Build] = buildRepository.getBuild(branch, buildId)

  }

}