package xmodels
import play.api.Play.current
import play.api.db._
import anorm._
object DatabaseAccessor {

  val AUTHKEY = "xauthkey"
  val HANDLE = "handle"
  
  def allUsers : List[ChessUser] = {
    
    var returnUsers = List[ChessUser]()
    
    DB.withConnection{ implicit conn =>
	      
      val getUsers = SQL("Select * from \"xusers\"")
      
      returnUsers = getUsers().map(row =>
        parseIntoUser(ChessUser(row[String]("oauthkey"), row[String]("xauthkey"), row[String]("handle"), row[String]("email"), row[String]("secret")), true)
      ).toList
	      
    }
    return returnUsers
  }
  
  def parseIntoUser(user:ChessUser, internal : Boolean) : ChessUser = {
    var tempUser = user
    if(!internal){
      tempUser = ChessUser("xxx", "xxx", user.handle, user.email, "xxx")
    }
	
	tempUser.friends = getFriends(user.xauth)
    	
	tempUser.games = getGames(user.xauth)
    	
    return tempUser
    
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
  
  def getUser(xauthkey : String, searchField : String,internal : Boolean) : ChessUser = {
    
    DB.withConnection{ implicit conn =>
      
        var rows : Stream[anorm.SqlRow] = null 
        if(searchField.equals(HANDLE)){
          rows = SQL("Select * from \"xusers\" where handle = {xauth}").on("xauth" -> xauthkey, "toSearch" -> searchField).apply()
        } else {
          rows = SQL("Select * from \"xusers\" where xauthkey = {xauth}").on("xauth" -> xauthkey, "toSearch" -> searchField).apply()
        }
    	
    	if(rows.length == 0){
    	  
    	  return null
    	  
    	}
    	var row = rows.head
    	return parseIntoUser(ChessUser(row[String]("oauthkey"), row[String]("xauthkey"), row[String]("handle"), row[String]("email"), row[String]("secret")), internal)
    	
    	
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
  
  def getGame(gameID : Long) : Game = {
    
    var game : Game = null
    var row : anorm.SqlRow = null 
    DB.withConnection{ implicit conn=>
    
      row = SQL("select * from \"games\" where id = {gameID}").on(
          "gameID" -> gameID
          ).apply.head
          
    }
    
    game = new Game(row[Long]("id"), row[String]("white"), row[String]("black"))
    
    return game
    
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
  
  def updateEmail(user : String, email : String) { 
    
    DB.withConnection {implicit conn=>
    
      SQL("UPDATE \"xusers\" SET email={email} WHERE xauthkey={user}").on(
      
          "email" -> email,
          "user" -> user
          
      ).executeUpdate
    
    }
    
  }
  
  def randomGameCreate(user : String) : String = {
    
    var otherPlayer : String = null
    var rows : List[String] = null
    
    DB.withConnection( implicit conn =>
      
      rows = SQL("select * from \"random_game_queue\" where player <> {user}").on("user" -> user).apply.map(row => 
         row[String]("player")
	  ).toList
      
    )
    
    if(rows.length > 0){
      
      otherPlayer = rows.head
      
      DB.withConnection(implicit conn =>
      
        SQL("""
            insert into "pending_game_requests"(requester, requestee) values({player}, {otherplayer});
            delete from "random_game_queue" where player = {otherplayer};
            """).on(
            
                "player" -> user,
                "otherplayer" -> otherPlayer
                
            ).executeUpdate
      
      )
      
    } else {
      DB.withConnection( implicit conn =>
         SQL("insert into \"random_game_queue\"(player) values({player})").on("player" -> user).executeInsert()
      )
      otherPlayer = user
    }
    
    return otherPlayer
    
  }
  
  def gameRequestCreate(user : String, opponent : String) {
    
    DB.withConnection(implicit conn =>
    
      SQL("insert into \"pending_game_requests\"(requester, requestee) values({player}, {otherplayer})").on(
      
          "player" -> user,
          "otherplayer" -> opponent
          
      ).executeInsert()
      
    )
    
  }
  
}