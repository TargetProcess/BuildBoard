package models

import models.mongo.{Branches, Builds}
import com.mongodb.casbah.commons.MongoDBObject


class BuildRepository {
  def getBuild(branch: Branch, number: Int): Option[Build] = Builds.findOne(MongoDBObject("number" -> number, "branch" -> branch.name))

  def toggleBuild(branch: Branch, number: Int, toggled:Boolean): Option[BuildInfo] = {
    val build = branch.activity.map {
      case b: BuildInfo if b.number == number => Some(b)
      case _ => None
    }
      .flatten
      .headOption

    build.foreach(b => b.toggled = toggled)

    branch.lastBuild
      .filter(b => b.number == number)
      .foreach(b => b.toggled = toggled)

    Branches.update(MongoDBObject("name" -> branch.name), branch, upsert = false, multi = false, Branches.dao.collection.writeConcern)

    build
  }
}
