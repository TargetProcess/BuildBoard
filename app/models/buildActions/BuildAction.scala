package models.buildActions

import models.branches.BranchInfo


trait BuildAction {
   val cycle: Cycle

   val branchName: String

   lazy val parameters: List[(String, String)] = List(
     "BRANCHNAME" -> branchName,
     "BUILDPRIORITY" -> (branchName match {
       case BranchInfo.hotfix(_) => "1"
       case BranchInfo.release(_) => "2"
       case BranchInfo.vs(_) => "3"
       case BranchInfo.develop() => "4"
       case BranchInfo.feature(_) => "5"
       case _ => "10"
     })) ++ cycle.parameters



   val name: String
 }

object BuildAction {
   val cycles = List(FullCycle, ShortCycle, BuildPackageOnly)

   def find(name: String) = cycles.find(_.name == name).get

   def unapply(action: BuildAction) = action match {
     case PullRequestBuildAction(id, _) => Some(action.name, Some(id), None, action.cycle.name)
     case BranchBuildAction(branch, _) => Some(action.name, None, Some(branch), action.cycle.name)
    // case customAction@BranchCustomBuildAction(branch, _) => Some(action.name, None, Some(branch), action.cycle.name, customAction.getPossibleBuildParameters)
    // case customAction@PullRequestCustomBuildAction(pullRequestId, _) => Some(action.name, Some(pullRequestId), None, action.cycle.name, customAction.getPossibleBuildParameters)
    // case customAction@BranchWithArtifactsReuseCustomBuildAction(branch, _, _) => Some(action.name, None, Some(branch), action.cycle.name, customAction.getPossibleBuildParameters)
   }
 }