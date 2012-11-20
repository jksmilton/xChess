package xmodels
import play.api.Play.current
import play.api.db._
import anorm._
object DatabaseAccessor {

  
  def allUsers : List[ChessUser] = {
    
    var returnUsers = List[ChessUser]()
    
    DB.withConnection{ implicit conn =>
	      
      val getUsers = SQL("Select username, email from xusers")
      
      returnUsers = getUsers().map(row =>
        new ChessUser(row[String]("username"), row[String]("email"))
      ).toList
	      
    }
    return returnUsers
  }
  
  
  def getUser(username : String) : ChessUser = {
    
    DB.withConnection{ implicit conn =>
      
    	var row = SQL("Select username, email from xusers where username = {name}").on("name" -> username).apply().head
    	
    	var user =  new ChessUser(row[String]("username"), row[String]("email"))
    	
    	user.friends = getFriends(user.name)
    	
    	return user
    	
    }
    
    
   // return user
  }
  
  def getFriends(username : String) : List[ChessUser] = {
    
    DB.withConnection{implicit conn =>
      
      return SQL("SELECT xusers.username, xusers.email FROM xusers, friendships WHERE friendships.userone = {user} AND xusers.username = friendships.usertwo").on(
    	"user" -> username
      ).apply().map(row=>
      	new ChessUser(row[String]("username"), row[String]("email"))
      ).toList
      
    }
    
  }
  
  def getGames(user : String) : List[Game] = {
      
      DB.withConnection{ implicit conn =>
          
          return SQL("select * from games where white = {user} OR black = {user}").on(
        	"user" -> user
          ).apply().map( row=>
          	new Game(row[Long]("id"), getUser(row[String]("white")), getUser(row[String]("black")), List[String]())
          ).toList
          
      }
      
  }
  
  def createUser(user : ChessUser) = {
    
    DB.withTransaction { implicit conn =>
     
    val id = SQL("INSERT INTO xusers(username, email) values({username},{email})").on(
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
  

  def createGame(white : ChessUser, black : ChessUser) : Long = {
      
      DB.withConnection{ implicit conn =>
          
          return SQL("insert into games(white, black) values({white},{black})").on(
        	"white" -> white.name,
        	"black" -> black.name
          ).executeInsert().head
         
      }
      
  }
  
}