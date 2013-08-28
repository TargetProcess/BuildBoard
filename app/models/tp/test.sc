import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._

object test {
  case class EntityState(
    id: Int,
    name: String,
    nextStates: Option[List[EntityState]])



  implicit var creatureReads: Reads[EntityState] = null;
                                                  //> creatureReads  : play.api.libs.json.Reads[test.EntityState] = null

  creatureReads = (
    (__ \ "Id").read[Int] ~
    (__ \ "Name").read[String] ~
    (__ \ "NextStates").readNullable(
      (__ \ "Items").lazyRead(list[EntityState](creatureReads))))(EntityState)


	val text ="""{
	"Id" : 5,
	"Name" : "In dev",
	"NextStates" : {
			"Items": [
				{
					"Id": 6,
					"Name": "In Test"
				}
			]
		}
	}"""                                      //> text  : String = {
                                                  //| 	"Id" : 5,
                                                  //| 	"Name" : "In dev",
                                                  //| 	"NextStates" : {
                                                  //| 			"Items": [
                                                  //| 				{
                                                  //| 					"Id": 6,
                                                  //| 					"Name": "In Test"
                                                  //| 				}
                                                  //| 			]
                                                  //| 		}
                                                  //| 	}
val state = Json.parse(text).as[EntityState]      //> state  : test.EntityState = EntityState(5,In dev,Some(List(EntityState(6,In 
                                                  //| Test,None))))

}