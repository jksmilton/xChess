package xmodels
import play.api.Play.current
import play.api.db._
import anorm._
object DatabaseAccessor {

  
  def allUsers : List[ChessUser] = {
    
    var returnUsers = List[ChessUser]()
    
    DB.withConnection{ implicit conn =>
	      
      val getUsers = SQL("Select username, email from users")
      
      returnUsers = getUsers().map(row =>
        new ChessUser(row[String]("username"), row[String]("email"))
      ).toList
	      
    }
    return returnUsers
  }
  
  
  def getUser(username : String) : ChessUser = {
    
    DB.withConnection{ implicit conn =>
      
    	var row = SQL("Select username, email from users where username = {name}").on("name" -> username).apply().head
    	
    	var user =  new ChessUser(row[String]("username"), row[String]("email"))
    	
    	user.friends = getFriends(user.name)
    	
    	return user
    	
    }
    
    
   // return user
  }
  
  def getFriends(username : String) : List[ChessUser] = {
    
    DB.withConnection{implicit conn =>
      
      return SQL("Select user.username, user.email from users, friendships where friendships.userone = {user} AND user.username = friendships.usertwo").on(
    	"user" -> username
      ).apply().map(row=>
      	new ChessUser(row[String]("username"), row[String]("email"))
      ).toList
      
    }
    
  }
  
  
  def createUser(user : ChessUser) = {
    
    DB.withTransaction { implicit conn =>
     
    val id = SQL("insert into users(username, email) values({username},{email})").on(
         "username" -> user.name, 
         "email" ->user.email).executeUpdate()
     
     conn.commit()
         
   }
    
  } 
  
  def createFriendship(user : ChessUser, friend : ChessUser) = {
    
    DB.withTransaction{ implicit conn =>
      
      SQL("insert into friendships(userone, usertwo) values({user}, {friend})").on(
          "user" -> user.name,
          "friend" -> friend.name
      ).executeUpdate()
      
      conn.commit()
      
    }
    
  }
  
}