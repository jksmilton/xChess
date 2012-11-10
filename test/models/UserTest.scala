package models
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.ChessUser._
class UserTest extends Specification {

  "A new user should called A N Other at email address ANOther@somee.ma.il should" should {
	
    var user = ChessUser("A N Other", "ANOther@somee.ma.il")
    
    "have no freinds to start with" in {
      
      
    }
    
  }
  
}