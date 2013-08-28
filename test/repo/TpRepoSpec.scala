package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.tp.EntityRepo
import models.EntityState

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class TpRepoSpec extends Specification {

  "TpRepo" should {

    "parse JSON for EntityStates correctly" in {
      val entityStates = EntityRepo.parseEntityStates("""
         {
  "Next": "http://plan.tpondemand.com/api/v1/EntityStates/?include=[Id,Name,NextStates[Id,Name]]&format=json&take=25&skip=25",
  "Items": [
    {
      "Id": 1,
      "Name": "Open",
      "NextStates": {
        "Items": [
          {
            "Id": 158,
            "Name": "Planned"
          },
          {
            "Id": 160,
            "Name": "Planned"
          }
        ]
      }
    },
    {
      "Id": 5,
      "Name": "Open"
    }
    ]
 }
          """)

      entityStates === List(
        EntityState(1, "Open", Some(List(
          EntityState(158, "Planned"),
          EntityState(160, "Planned")))),
        EntityState(5, "Open"))

    }

  }

}