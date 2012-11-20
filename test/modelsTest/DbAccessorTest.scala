package modelsTest
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import xmodels._
class DbAccessorTest extends Specification{

  "A clean database" should{
    
    var user = new ChessUser("A N Other","ANOther@somee.ma.il")
    var otherUser = new ChessUser("Som Eguy", "SomEguy@anadd.re.ss")
    "have 0 user entries" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
    	  DatabaseAccessor.allUsers.size == 0
      }
    }
    
    "have 1 user entry after adding a single user" in {
      
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
      
	      DatabaseAccessor.createUser(user)
	      
	      DatabaseAccessor.allUsers.size == 1
      
      }
    }
    
    "that entry should be equal to user A N Other" in {
      
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())){
      
	      DatabaseAccessor.createUser(user)
	      
	      var retreivedUser = DatabaseAccessor.getUser(user.name)
	      
	      user.name.equals(retreivedUser.name)
      
      }
    }
    
    "After saving friendship A N Other -> Som Eguy be able to read in the user A N Other with this friend included" in {
      
      running(FakeApplication()){
      
	      DatabaseAccessor.createUser(user)
	      DatabaseAccessor.createUser(otherUser)
	      
	      DatabaseAccessor.createFriendship(user, otherUser)
	      
	      var dbUser = DatabaseAccessor.getUser(user.name)	      
      
	      dbUser.friends.contains(otherUser)
	      
      }
      
    }
    
    "After creating a game between A N Other and Som Eguy you can withdraw it with either account" in {
        
        running(FakeApplication()){
      
	      DatabaseAccessor.createUser(user)
	      DatabaseAccessor.createUser(otherUser)
	      
	      DatabaseAccessor.createGame(user, otherUser)
	      
	      DatabaseAccessor.getGames(user.name).head.black.equals(otherUser)
	      
      }
        
    }
    
    
    
  }
  
}