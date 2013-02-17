package xmodels
import play.api.Play.current
import play.api.db._
import anorm._
object DatabaseAccessor {

  
  def allUsers : List[ChessUser] = {
    
    var returnUsers = List[ChessUser]()
    
    DB.withConnection{ implicit conn =>
	      
      val getUsers = SQL("Select * from \"xusers\"")
      
      returnUsers = getUsers().map(row =>
        parseIntoUser(row[String]("handle"), row[String]("email"), row[String]("xauthkey"))
      ).toList
	      
    }
    return returnUsers
  }
  
  def parseIntoUser(handle : String, email : String, xauth : String) : ChessUser = {
    
    var user =  new ChessUser("xxx", "xxx", email, handle,"xxx")
    	
	user.friends = getFriends(xauth)
    	
	user.games = getGames(xauth)
    	
    return user
    
  }
  
  def authCheck(appID:String) : Boolean = {
    DB.withConnection{implicit conn =>
      var rows = SQL("Select * from \"application_ids\" where appID={id}").on("id" -> appID).apply()
      
      println("returned row length for app auth: " + rows.length)
      
      if(rows.length == 0){
          println("app auth returns false")
          return false
      }else{
          return true
      }
    }
  }
  
  def getUser(xauthkey : String) : ChessUser = {
    
    DB.withConnection{ implicit conn =>
      
    	var rows = SQL("Select * from \"xusers\" where xauthkey = {xauth}").on("xauth" -> xauthkey).apply()
    	
    	if(rows.length == 0){
    	  
    	  return null
    	  
    	}
    	var row = rows.head
    	return parseIntoUser(row[String]("handle"), row[String]("email"), row[String]("xauthkey"))
    	
    	
    }
    
    
   
  }
  
  def getFriends(xauth : String) : List[String] = {
    
    DB.withConnection{implicit conn =>
      
      return SQL("SELECT \"xusers\".xauthkey, \"xusers\".handle FROM \"xusers\", \"friendships\" WHERE (\"friendships\".userone = {user} AND \"xusers\".xauthkey = \"friendships\".usertwo) OR (\"friendships\".usertwo = {user} AND \"xusers\".xauthkey = \"friendships\".userone)").on(
    	"user" -> xauth
      ).apply().map(row=>
      	row[String]("handle")
      ).toList
      
    }
    
  }
  
  def getGames(user : String) : List[Game] = {
      
      DB.withConnection{ implicit conn =>
          
          return SQL("select * from \"games\" where white = {user} OR black = {user}").on(
        	"user" -> user
          ).apply().map( row=>
          	new Game(row[Long]("id"), row[String]("white"), row[String]("black"))
          ).toList
          
      }
      
  }
  
  def createUser(user : ChessUser) = {
    
    DB.withTransaction { implicit conn =>
     
    val id = SQL("INSERT INTO \"xusers\"(xauthkey, oauthkey, handle, secret, email) values({xauthkey},{oauthkey}, {handle}, {secret}, {email})").on(
         "xauthkey" -> user.xauth, 
         "email" ->user.email,
         "oauthkey" -> user.authString,
         "handle" -> user.handle,
         "secret" -> user.authSecret
         ).executeUpdate()
     
     conn.commit()
         
   }
    
  } 
  
  def createFriendship(user : ChessUser, friend : ChessUser) = {
    
    DB.withTransaction{ implicit conn =>
      
      SQL("insert into \"friendships\"(userone, usertwo) values({user}, {friend})").on(
          "user" -> user.xauth,
          "friend" -> friend.xauth
      ).executeUpdate()
      
      conn.commit()
      
    }
    
  }
  
  def createGame(white : ChessUser, black : ChessUser) : Long = {
      
      DB.withConnection{ implicit conn =>
          
          return SQL("insert into \"games\"(white, black) values({white},{black})").on(
        	"white" -> white.xauth,
        	"black" -> black.xauth
          ).executeInsert().head
         
      }
      
  }
  
  def addMove(gameID : Long, player : String, move : String) {
    
    DB.withTransaction{ implicit conn =>
      
      SQL("insert into \"transcripts\"(game, player, move) values({game}, {player}, {move})").on(
    		  
          "game" -> gameID,
          "player" -> player,
          "move" -> move
          
      ).executeInsert().head
      
      conn.commit()
      
    }
    
  }
  
  def getTranscript(gameID : Long) : List[String] = {
    
    DB.withConnection { implicit conn =>
      
      return SQL("select move from \"transcripts\" where game = {gameID} order by timePlayed").on(
          "gameID" -> gameID
          ).apply().map( row=> 
            new String(row[String]("move"))
          ).toList
      
      
    }
    
  }
  
  def createPendingFriendship(requester : String, requestee : String) : Long = {
    
    DB.withConnection{ implicit conn =>
    
      return SQL("insert into \"pending_friend_requests\"(requester, requestee) values({requester}, {requestee})").on(
        "requester" -> requester,
        "requestee" -> requestee
      ).executeInsert().head
      
    }
    
  }
  
}