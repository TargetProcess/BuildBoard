package globals

import org.specs2.matcher.{Expectable, Matcher}

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
