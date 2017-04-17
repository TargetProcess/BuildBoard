package models.jenkins

import java.io.File
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.github.nscala_time.time.Imports._
import models._
import org.joda.time.DateTime
import src.Utils.watch
import components.DefaultRegistry

trait FileHelper {
  type Folder = File
}

trait ParseFolder extends ParseTestResults with FileHelper {
  protected val rootJobName = "StartBuild"
  private val timeout = 8.hours


  case class BuildSource(branch: String, number: Int, pullRequestId: Option[Int], folder: Folder, params: BuildParams)

  def getParamsFile(folder: Folder): File = {
    new File(folder, "Build/StartBuild/StartBuild.params")
  }

  def allNodes(node: BuildNode): Stream[BuildNode] = {
    val map: Stream[BuildNode] = node.children.map(child => allNodes(child)).toStream.flatten
    node #:: map
  }

  def getBuild(existingBuild: Option[Build], buildSource: BuildSource, toggled: Boolean): Option[Build] = {
    val name: String = buildSource.folder.getName

    val maybeNode = watch(s"Get node ${buildSource.branch} ${buildSource.number}") {
      getBuildNode(new Folder(buildSource.folder, "Build"), existingBuild)
    }
    val folder = new Folder(buildSource.folder, "Build/StartBuild")

    if (folder.exists) {
      val commits = getCommits(new File(folder, "Checkout/GitChanges.log"))
      val ref = getRef(new File(folder, "Checkout/sha.txt"))

      val finishedReruns = maybeNode.map(
        node => allNodes(node)
          .filter(x => x.rerun.getOrElse(false))
          .map(node => s"${node.runName}_${node.name}")
          .toList)
        .getOrElse(Nil)

      getBuildDetails(folder)
        .map(buildDetails => {
          Build(
            number = buildSource.number,
            branch = buildSource.branch,
            status = buildDetails.status,
            timestamp = buildDetails.startTime,
            timestampEnd = buildDetails.endTime,
            toggled = toggled,
            commits = commits,
            ref = ref,
            pullRequestId = buildSource.pullRequestId,
            initiator = buildSource.params.parameters.get("WHO_STARTS"),
            description = buildSource.params.parameters.get("DESCRIPTION").orElse(buildSource.params.parameters.get("UID")),
            node = maybeNode,
            name = name,
            artifacts = getBuildArtifacts(folder),
            pendingReruns = existingBuild.map(build => build.pendingReruns.filter(r => !finishedReruns.contains(r))).getOrElse(Nil)
          )
        }
        )
    } else {
      None
    }
  }


  val splitRegex = "(?m)^commit(?:(?:\r\n|[\r\n]).+$)*".r
  val commitRegex = """\s*(\w+)[\r\n].*[\r\n]?s*Author:\s*(.*)\s*<(.*)>[\r\n]\s*Date:\s+(.*)[\r\n]([\w\W]*)""".r

  def getCommits(file: File): List[Commit] = {
    if (!file.exists) {
      return Nil
    }

    FileApi.read(file) match {
      case Some(contents) => splitRegex.split(contents)
        .toList
        .filter(_.length > 0).flatMap {
        case commitRegex(sha1, name, email, date, comment) =>
          val normalizedComment = comment.trim
          val commitName = getCommitName(name, normalizedComment)
          Some(Commit(sha1, normalizedComment, commitName, email, new DateTime(new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").parse(date).getTime)))
        case _ => None
      }
      case None => Nil
    }
  }

  def getRef(file: File) = {
    if (!file.exists) {
      None
    } else {
      FileApi.read(file).map(_.trim)
    }
  }

  val complexNameRegex = "(.+)_(.+)".r
  val mergerRegex = "(?s).*Merged by ([^(]*).*".r
  val buildNumberRegex = ".*/(\\d+)/".r

  private def getCommitName(name: String, comment: String): String = comment match {
    case mergerRegex(author) => s"$name [${author.trim}]"
    case _ => name
  }

  def unstableNodeNames: List[String]

  def isUnstable(name: String): Boolean = unstableNodeNames.contains(name)

  def getBuildNode(f: File, existingBuild: Option[Build]): Option[BuildNode] = {

    def getBuildNodeInner(existingBuildNode: Option[BuildNode], folder: File, path: String): Option[BuildNode] = {
      if (!folder.exists) return None

      val contents = folder.listFiles.sortBy(_.getName).toList
      val artifacts = getArtifacts(contents)

      val (runName, name) = getBuildNodeName(folder)

      play.Logger.info(s"get build Node ${name}")

      val children: List[BuildNode] = contents
        .filter(file => file.isDirectory && !file.getName.startsWith("."))
        .flatMap(folder => {
          val (_, name) = getBuildNodeName(folder)
          val existingChildNode = existingBuildNode
            .flatMap(buildNode => buildNode.children.find(childNode => childNode.name == name))
          getBuildNodeInner(existingChildNode, folder, folder.getPath)
        })

      val buildDetails = getBuildDetails(folder)
      val maybeBuildNode = buildDetails
        .map(buildDetails => {
          val jobRunInfo = getJobRunInfo(name, buildDetails, artifacts)
          DefaultRegistry.jobRunRepository.update(jobRunInfo)

          BuildNode(
            buildDetails.number.toString,
            name,
            runName,
            buildDetails.number,
            buildDetails.status,
            buildDetails.statusUrl.getOrElse(""),
            if (buildDetails.statusUrl.isDefined) {
              Artifact("output", buildDetails.statusUrl.map(url => s"${url}consoleText").get) :: artifacts
            } else artifacts,
            buildDetails.startTime,
            Some(buildDetails.endTime.getOrElse(new DateTime(0))),
            buildDetails.rerun,
            children = children,
            isUnstable = Some(isUnstable(name)))
        })

      //check if it's container job
      if (maybeBuildNode.isEmpty && children.length == 1) Some(children.head) else maybeBuildNode
    }

    //todo: add artifacts to root node
    val folder = new File(f, rootJobName)
    if (folder.exists) getBuildNodeInner(existingBuild.flatMap(build => build.node), folder, f.getPath) else None
  }

  private def getJobRunInfo(nodeName: String, buildDetails: BuildDetails, artifacts: List[Artifact]): JobRun = {
    val id = s"${buildDetails.number}-${nodeName}-${buildDetails.startTime}"
    JobRun(id, buildDetails.number, nodeName, getFailedTestCases(artifacts), buildDetails.startTime, buildDetails.endTime)
  }

  private def getBuildNodeName(folder: File): (String, String) = {
    folder.getName match {
      case complexNameRegex(runNme, name) => (runNme, name)
      case name => (name, name)
    }
  }

  private case class BuildDetails(number: Int, status: Option[String], statusUrl: Option[String], startTime: DateTime, endTime: Option[DateTime], rerun: Option[Boolean])

  val dateFormat: SimpleDateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z")
  dateFormat.setTimeZone(TimeZone.getDefault)

  private def getBuildDetails(folder: File): Option[BuildDetails] = {
    val contents = folder.listFiles

    def getFile(extension: String): Option[File] = {
      contents.find(_.getName.endsWith(extension))
    }

    val startedFile: Option[File] = getFile("started")
    val finishedFile: Option[File] = getFile("finished")


    startedFile.map(started => {
      val startedFileContent: Option[Map[Int, String]] = FileApi.readAsMap(started)
      val statusUrl: Option[String] = startedFileContent.flatMap(_.get(0))

      val number = statusUrl.map { case buildNumberRegex(num) => num.toInt }.getOrElse(-1)

      val startTime = startedFileContent.flatMap(_.get(1))
        .map(x => new DateTime(dateFormat.parse(x + " +0300").getTime))
        .getOrElse(new DateTime(started.lastModified))

      val rerunRegex = "RERUN=(true|false)".r

      val rerun = startedFileContent.flatMap(_.get(2)).flatMap {
        case rerunRegex(value) => Some(value == "true")
        case _ => None
      }

      val status: Option[String] =
        finishedFile.flatMap(FileApi.read)
          .orElse(if ((DateTime.now - timeout) > startTime) Some("TIMED OUT") else None)

      val endTime = finishedFile.map(x => new DateTime(x.lastModified))

      BuildDetails(number, status, statusUrl, startTime, endTime, rerun)
    })
  }
}


