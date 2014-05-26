package models.services

import models._
import scala.util.matching.Regex
import src.Utils.watch
import components._
import scala.Some
import com.mongodb.casbah.commons.MongoDBObject
import models.BuildImplicits._
import models.BuildInfo
import models.Branch


trait BuildServiceComponentImpl extends BuildServiceComponent {
  this: BuildServiceComponentImpl
    with BuildRepositoryComponent
    with BranchRepositoryComponent
  =>

  val buildService:BuildService = new BuildServiceImpl

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


        val optionBranch = branchRepository.getBranch(build.branch)
/*
        val builds: Iterable[Build] = optionBranch.flatMap(buildRepository.getBuilds(_))




        val leafNodes = build.getLeafNodes



        buildRepository.getBuilds(build.branch)
        */

        false
      }




    }
  }

}