package globals

import com.mongodb.casbah.MongoConnection
import org.specs2.matcher.{Expectable, Matcher}
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


object Matchers {

  def beTheSeqSameAs[T](expected: Seq[T]): Matcher[Seq[T]] = new Matcher[Seq[T]] {
    def apply[S <: Seq[T]](expectable: Expectable[S]) = {
      val actual = expectable.value
      val errorMsg = if (expected.length != actual.length) {
        s"Expected has length ${expected.length} while actual has ${actual.length}"
      }
      else {
        val zipped = expected.zip(actual)

        val foldedResult = zipped.foldLeft[Option[String]](None) {
          case (Some(message), (a, b)) => Some(message + s", $a <-> $b")
          case (None, (a, b)) if a != b => Some(s"Collections are different: $a <-> $b")
          case _ => None
        }

        foldedResult.getOrElse("")
      }

      result(errorMsg.isEmpty,
        "The two collections are matching",
        errorMsg,
        expectable
      )
    }
  }
}