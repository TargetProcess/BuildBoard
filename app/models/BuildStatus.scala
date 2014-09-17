package models

trait BuildStatus {
  val name: String
  val obj: String
  val success: Option[Boolean]
}

abstract class BuildStatusBase(
                                val name: String,
                                val success: Option[Boolean] = Some(false))
  extends BuildStatus
{
  override val obj = name
}


object BuildStatus {
  def apply(string: Option[String], toggled: Boolean) =
    if (toggled)
      Toggled
    else
      string match {
        case None => InProgress
        case Some(text) => text.toLowerCase match {
          case "fail" | "failure" | "failed" => Failure
          case "in progress" => InProgress
          case "success" | "ok" => Ok
          case "aborted" => Aborted
          case "timed out" => TimedOut
          case _ => Unknown
        }
      }


  def calculate(status: Option[String], children: List[BuildNode]): BuildStatusBase = {

    if (children.isEmpty)
      BuildStatus(status, toggled = false)
    else
      children.foldLeft[BuildStatusBase](BuildStatus.Unknown)((status, node) => {
        val nodeStatus: BuildStatusBase = node.buildStatus
        (status, nodeStatus) match {
          case (BuildStatus.Failure, _) => BuildStatus.Failure
          case (BuildStatus.Aborted, _) => BuildStatus.Aborted
          case (BuildStatus.TimedOut, _) => BuildStatus.TimedOut
          case (BuildStatus.InProgress, _) => BuildStatus.InProgress
          case _ => nodeStatus
        }
      })

  }


  case object Unknown extends BuildStatusBase("unknown", None)

  case object Failure extends BuildStatusBase("failure") {
    override val obj = "failed"
  }

  case object InProgress extends BuildStatusBase("in progress", None)

  case object Ok extends BuildStatusBase("ok", Some(true))

  case object Aborted extends BuildStatusBase("aborted")

  case object TimedOut extends BuildStatusBase("timed out")

  case object Toggled extends BuildStatusBase("toggled", Some(true))

}
