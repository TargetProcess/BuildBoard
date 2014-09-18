package globals

import com.mongodb.casbah.MongoConnection
import org.specs2.mutable.BeforeAfter
import play.api.test.FakeApplication

/**
* Created by fomin on 9/18/2014.
*/
object context extends BeforeAfter {

  lazy val dummy_db_name = "test_app_db"

  lazy val fakeApp = FakeApplication(
    additionalConfiguration = Map("mongodb.default.db" -> dummy_db_name),
    withGlobal = Some(EmptyGlobal)
  )

  def before = {
    implicit val mongoDB = MongoConnection()(dummy_db_name)
    mongoDB.dropDatabase()
  }

  def after = ()
}
