package modelsTest
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import xmodels._

class UserTest extends Specification {

  "A new user called A N Other at email address ANOther@somee.ma.il" should {
	
    var user = new ChessUser("A N Other", "ANOther@somee.ma.il")
    var friend = new ChessUser("N U friend", "NUfriend@fr.ei.nd")
    "have no friends to start with" in {
      
      user.friends.size == 0
      
    }
    
    "be called A N Other" in {
      
      user.name == "A N Other"
      
    }
    
    "have the email address ANOther@somee.ma.il" in {
      
      user.email == "ANOther@somee.ma.il"
      
    }
    
    "after adding a new friend (N U friend, NUfriend@fr.ei.nd, respond friend added" in {
      var response = user.addFriend(friend)
      response.equals("friend added")
      
    }
    
    "and have one friend" in {
      
      user.friends.size == 1
      
    }
    
    "the friend should have the name N U friend and email address NUfriend@fr.ei.nd" in {
      
      user.friends.last.name.equals("N U friend")
      user.friends.last.email.equals("NUfriend@fr.ei.nd")      
      
    }
    
    "if the friend is readded, respond with an error message" in {
      
      var response = user.addFriend(friend)
      response.equals(friend + " is already a friend")
      
    }
    
  }
  
}