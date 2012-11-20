package modelsTest
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import xmodels._
class DbAccessorTest extends Specification{

  "A clean database" should{
    
    var user = new ChessUser("A N Other","ANOther@somee.ma.il")
    
    "have 0 entries" in {
      
      //DatabaseAccessor.all.size == 0
      
    }
    
    "have 1 entry after adding a single user" in {
      
      DatabaseAccessor.createUser(user)
      0 == 1
     // DatabaseAccessor.all.size == 1
      
    }
    
  }
  
}