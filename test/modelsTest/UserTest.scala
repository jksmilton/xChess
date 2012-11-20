package modelsTest
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import xmodels._

class UserTest extends Specification {

  "A new user called A N Other at email address ANOther@somee.ma.il" should {
	
    var user = new ChessUser("A N Other", "ANOther@somee.ma.il")
    var freind = new ChessUser("N U Freind", "NUFreind@fr.ei.nd")
    "have no freinds to start with" in {
      
      user.freinds.size == 0
      
    }
    
    "be called A N Other" in {
      
      user.name == "A N Other"
      
    }
    
    "have the email address ANOther@somee.ma.il" in {
      
      user.email == "ANOther@somee.ma.il"
      
    }
    
    "after adding a new freind (N U Freind, NUFreind@fr.ei.nd, respond freind added" in {
      var response = user.addFreind(freind)
      response.equals("freind added")
      
    }
    
    "and have one freind" in {
      
      user.freinds.size == 1
      
    }
    
    "the freind should have the name N U Freind and email address NUFreind@fr.ei.nd" in {
      
      user.freinds.last.name.equals("N U Freind")
      user.freinds.last.email.equals("NUFreind@fr.ei.nd")      
      
    }
    
    "if the freind is readded, respond with an error message" in {
      
      var response = user.addFreind(freind)
      response.equals(freind + " is already a freind")
      
    }
    
  }
  
}