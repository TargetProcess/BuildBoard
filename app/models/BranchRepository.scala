package models

import scala.language.postfixOps
import models.mongo.Branches
import com.mongodb.casbah.commons.MongoDBObject

class BranchRepository {

  def getBranch(id: String): Option[Branch] = Branches.findOne(MongoDBObject("name" -> id))

  def getBranches: List[Branch] = Branches.findAll().toList
}
