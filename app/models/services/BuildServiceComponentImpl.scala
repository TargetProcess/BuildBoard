package models.services

import models._
import components._
import models.BuildImplicits._
import models.BuildInfo
import models.Branch
import scala.collection.mutable.Map
import scala.collection.{mutable, immutable}


trait BuildServiceComponentImpl extends BuildServiceComponent {
  this: BuildServiceComponentImpl
    with BuildRepositoryComponent
    with BranchRepositoryComponent
  =>

  val buildService: BuildService = new BuildServiceImpl

  class BuildServiceImpl extends BuildService {
    def toggleBuild(branch: Branch, number: Int, toggled: Boolean): Option[BuildInfo] = {

      val build = buildRepository.getBuild(branch, number)
        .map(b => b.copy(toggled = toggled))

      build.foreach(buildRepository.update)

      build.map(toBuildInfo)
    }

    override def canBeToggled(build: Build): Boolean = {
      if (build.toggled) true
      else {


        val optionBranch: Option[Branch] = branchRepository.getBranch(build.branch)
        val r: Boolean = optionBranch.fold(false)(branch => {

          val builds: Iterator[Build] = buildRepository.getBuilds(branch)
          val toMap: immutable.Map[String, Int] = build.getLeafNodes
            .filter(isFailed)
            .map(n => (n.name, 1))
            .toMap
          val failedNodes: mutable.Map[String, Int] = mutable.Map() ++ toMap




            for (buildToCheck <- builds if buildToCheck.number <= build.number) {
              for (node <- buildToCheck.getLeafNodes) {
                if (failedNodes.contains(node.name)) {
                  val failed = isFailed(node)
                  if (failed){
                    failedNodes(node.name)+=1
                  }else{

                  }
                }
              }
            }






          true
        })
        r
      }

    }


    def isFailed(leaf: BuildNode): Boolean = {
      !BuildStatus(leaf.status, toggled = false).success.getOrElse(false)
    }
  }


}