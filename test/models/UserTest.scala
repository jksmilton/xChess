package models
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.ChessUser
class UserTest extends Specification {

  "The new user 'A N Other' at 'ANOther@some.ma.il'" should {
    var frank = new ChessUser "A N Other" "ANOther@some.mail"
    "have no freinds" in {
      "Hello world" must have size(11)
    }
  }
}