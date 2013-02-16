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
        parseIntoUser(row[String]("username"), row[String]("email"))
      ).toList
	      
    }
    return returnUsers
  }
  
  def parseIntoUser(name : String, email : String) : ChessUser = {
    
    var user =  new ChessUser(name, email)
    	
    	user.friends = getFriends(user.name)
    	
    	user.games = getGames(user.name)
    	
    	return user
    
  }
  
  def authCheck(appID:String) : Boolean = {
    DB.withConnection{implicit conn =>
      var rows = SQL("Select * from application_ids where appID={id}").on("id" -> appID).apply()
      
      println("returned row length for app auth: " + rows.length)
      
      if(rows.length == 0){
          println("app auth returns false")
          return false
      }else{
          return true
      }
    }
  }
  
  def getUser(username : String) : ChessUser = {
    
    DB.withConnection{ implicit conn =>
      
    	var rows = SQL("Select username, email from xusers where username = {name}").on("name" -> username).apply()
    	
    	if(rows.length == 0){
    	  
    	  return null
    	  
    	}
    	var row = rows.head
    	return parseIntoUser(row[String]("username"), row[String]("email"))
    	
    	
    }
    
    
   // return user
  }
  
  def getFriends(username : String) : List[String] = {
    
    DB.withConnection{implicit conn =>
      
      return SQL("SELECT xusers.username, xusers.email FROM xusers, friendships WHERE friendships.userone = {user} AND xusers.username = friendships.usertwo").on(
    	"user" -> username
      ).apply().map(row=>
      	row[String]("username")
      ).toList
      
    }
    
  }
  
  def getGames(user : String) : List[Game] = {
      
      DB.withConnection{ implicit conn =>
          
          return SQL("select * from games where white = {user} OR black = {user}").on(
        	"user" -> user
          ).apply().map( row=>
          	new Game(row[Long]("id"), row[String]("white"), row[String]("black"))
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
  
  def addMove(gameID : Long, player : String, move : String) {
    
    DB.withTransaction{ implicit conn =>
      
      SQL("insert into transcripts(game, player, move) values({game}, {player}, {move})").on(
    		  
          "game" -> gameID,
          "player" -> player,
          "move" -> move
          
      ).executeInsert().head
      
      conn.commit()
      
    }
    
  }
  
  def getTranscript(gameID : Long) : List[String] = {
    
    DB.withConnection { implicit conn =>
      
      return SQL("select move from transcripts where game = {gameID} order by timePlayed").on(
          "gameID" -> gameID
          ).apply().map( row=> 
            new String(row[String]("move"))
          ).toList
      
      
    }
    
  }
  
  def createPendingFriendship(requester : String, requestee : String) : Long = {
    
    DB.withConnection{ implicit conn =>
    
      return SQL("insert into pending_friend_requests(requester, requestee) values({requester}, {requestee})").on(
        "requester" -> requester,
        "requestee" -> requestee
      ).executeInsert().head
      
    }
    
  }
  
}