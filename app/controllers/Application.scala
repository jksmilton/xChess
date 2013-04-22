package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws._
import xmodels._
import play.api.libs.json._
import com.codahale.jerkson.Json._
import play.api.libs.oauth.RequestToken
import play.api.libs.oauth.ConsumerKey
import play.api.libs.oauth.OAuth
import play.api.libs.oauth.ServiceInfo
import play.api.cache.Cache
import java.security.MessageDigest
import play.api.libs.oauth.OAuthCalculator
import javachess._
import scala.collection.mutable.ArraySeq
object Application extends Controller {
  val googlekey = ConsumerKey("www.xchess.co.uk", "V68qmc6za5w4PhVl9P5ZpN1d")
  val google = OAuth(ServiceInfo(
	    "https://www.google.com/accounts/OAuthGetRequestToken",
	    "https://www.google.com/accounts/OAuthGetAccessToken",
	    "https://www.google.com/accounts/OAuthAuthorizeToken", googlekey),
	    true)
  val twitterkey = ConsumerKey("7HmXfn2q6TfTRI5KwIXfQ","GCllCxvxennAlZLFRglgBUq0ZUO3A30QPN6T0e9jE")
  
  val twitter= OAuth(ServiceInfo(
	    "https://api.twitter.com/oauth/request_token",
	    "https://api.twitter.com/oauth/access_token",
	    "https://api.twitter.com/oauth/authorize", twitterkey),
	    true)
  
  def index = Action {
    TODO
  }
  
  def getUser(username : String, appID : String) = Action { request =>
    
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
      
	    var user = DatabaseAccessor.getUser(username, DatabaseAccessor.AUTHKEY, false)
	    
	    if(user == null){
	      
	      Ok("Account does not exist")
	      
	    } else {
	      
	      user.convertGamesToCLient
	      
	      val jsonResult = generate(user)
	      
	      Ok(jsonResult)
	     
	    }
    
    }
    
  }
  
  def generateRequestToken(appID : String) = Action{ request =>
      val callback = "http://www.xchess.co.uk/application/callbacks/oauth" //PRODUCTION
      //val callback = "http://localhost:9000/application/callbacks/oauth" //TEST
          
      if(!DatabaseAccessor.authCheck(appID)){
        
          Ok("Application not authorised")
        
      } else {
    	  
		  twitter.retrieveRequestToken(callback) match {
	          case Right(t) => {
	              Cache.set(t.token, t.secret)
	              println("secret: " + t.secret)
	              Ok(twitter.redirectUrl(t.token))
	          }
	          case Left(e) => throw e
	      }
	  
	  }
	  
  }
  
  def getGame(gameID : Long, appID : String) = Action{ request=>
      
         
     if(!DatabaseAccessor.authCheck(appID)){
        
          Ok("Application not authorised")
        
      } else {
          
	    val transcript = new Transcript(DatabaseAccessor.getTranscript(gameID))
	        
	    Ok(generate(transcript))
    }
  }
  
  def exchangeRequestForAccess(verifier: String, token: String) = Action{ request=>
      
	  val secret = Cache.getAs[String](token).get
	  println("secret: " + secret )
	  val requestToken= RequestToken(token, secret)
	  println("verifier: " + verifier + "; token: " + token);
	  twitter.retrieveAccessToken(requestToken, verifier) match {
	  	case Right(t) => {
	  	    
	  	    val response = WS.url("https://api.twitter.com/1.1/account/settings.json").sign(OAuthCalculator(twitterkey, RequestToken(t.token, t.secret))).get()
	  	    var user = ""
  	        var tokenHash = md5Hasher(t.token)
  	        var dbUser = DatabaseAccessor.getUser(tokenHash, DatabaseAccessor.AUTHKEY, true)
  	        
  	        if(dbUser ==null){
  	        	Async{
			  	    response.map( response =>
			  	        			  	        
			  	        Redirect("xchess://" + generate(setUp((response.json \ "screen_name").as[String], t.token, tokenHash, t.secret)))
			  	        
			  	        
			  	    )
  	        	}
  	        } else {
  	          Redirect("xchess://" + generate(setUp(dbUser)))
  	        }
	  	    
	  	}
	  	case Left(e) => throw e
	  }
  }
  
  def setUp(screenname : String, token : String, xauth : String, secret : String) : ChessUser = {
     	        
	   var dbUser = new ChessUser(token, xauth, "user@example.com", screenname, secret)
       
	   DatabaseAccessor.createUser(dbUser)
	          
	   return ChessUser("xxxx", dbUser.xauth, dbUser.email, dbUser.handle, "xxxx")
  }
  
  def setUp(user : ChessUser) : ChessUser = {
    
    return ChessUser("xxxx", user.xauth, user.email, user.handle, "xxxx")
    
  }
  
  def md5Hasher(input : String) : String = {
      
      var digester = MessageDigest.getInstance("MD5")
      var outputbuffer = new StringBuffer()
      var byteDigest = digester.digest(input.getBytes("UTF-8")).iterator
      
      while(byteDigest.hasNext){
			outputbuffer.append(Integer.toHexString(byteDigest.next & 0xff))
	  }
      
      return outputbuffer.toString()
      
  }
  
  def addFriend(user: String, friend: String, appID : String) = Action{ request=>
     if(!DatabaseAccessor.authCheck(appID)){
        
          Ok("Application not authorised")
        
      } else {
          
		    val userProfile = DatabaseAccessor.getUser(user, DatabaseAccessor.AUTHKEY ,true)      
		    var resultStr = userProfile.addFriend(friend)
		    val friendProfile = DatabaseAccessor.getUser(friend, DatabaseAccessor.HANDLE ,true)    
		    
		    if(resultStr == "friend added" && friendProfile != null){
		      
		      val requestActor = new FriendshipRequestActor(userProfile, friendProfile)
		      
		      requestActor.start
		      
		    } else if(friendProfile == null){
		      
		      resultStr = friend + " does not exist"
		      
		    }
		    
		    Ok(resultStr)
		    
      }
  }
  
  def editEmail(user:String, appID : String) = Action(parse.text){ request =>
  
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
      
	    var userProfile = DatabaseAccessor.getUser(user, DatabaseAccessor.AUTHKEY, false)
	    
	    if(userProfile == null){
	      
	      Ok("Account does not exist")
	      
	    } else {
	      
	      val email = request.body
	      
	      DatabaseAccessor.updateEmail(user, email)
	     
	      Ok("Updated Successfully")
	      
	    }
    
    }
  
  }
  
  def addMove(user:String, gameID:Long, start:String, end : String, promotion : String, appID:String) = Action { request=>
    
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
    
	    val game = DatabaseAccessor.getGame(gameID)
	    
	    if(!(user.equals(game.white) || user.equals(game.black) )){
	      
	      Ok("Player not a member of this game")
	      
	    } else {
	    
		    val board = buildBoard(DatabaseAccessor.getTranscript(gameID))
		    
		    if((board.GetCurrentPlayer() == 0 && user.equals(game.black)) || (board.GetCurrentPlayer() == 1 && user.equals(game.white))){
		      Ok("Not your turn")
		    }
		    
		    val xmove = new Move(start + " " + end + " " + promotion, user)
		    var newMove = xmove.convertToEngine
		    
		    val player = new jcPlayerHuman(board.GetCurrentPlayer())
		    
		    newMove = player.GetMove(board, newMove, newMove.MoveType)
		    
		    DatabaseAccessor.addMove(gameID, user, xmove.move)
		    
		    Ok("Success")
	    }
    }
  }
  
  def buildBoard(transcript: List[String] ) : jcBoard = {
    
    val board = new jcBoard
    val players = new ArraySeq[jcPlayerHuman](2)
    var crntMove = new jcMove
    players(0) = new jcPlayerHuman(0)
    players(1) = new jcPlayerHuman(1)
    
    for(move <- transcript){
      
      val xmove = new Move(move, "")
      crntMove = xmove.convertToEngine
      crntMove = players(board.GetCurrentPlayer()).GetMove(board, crntMove, crntMove.MoveType)
      board.ApplyMove(crntMove)
      
    }
    
    return board
    
  }
  
  def requestRandomGame(user : String, appID : String) = Action { request=>
  
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("@Application not authorised")
        
    } else {
    
	    var otherPlayer = DatabaseAccessor.randomGameCreate(user)
	    
	    otherPlayer = DatabaseAccessor.getUser(otherPlayer, DatabaseAccessor.AUTHKEY, true).handle
	    
	    Ok(otherPlayer)
    
    }
  
  }
  
  def requestGame(user : String, otherPlayer : String, appID : String) = Action {request =>
  	
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
    
    	val opponent = DatabaseAccessor.getUser(otherPlayer, DatabaseAccessor.HANDLE, true)
    
	    DatabaseAccessor.gameRequestCreate(user, opponent.xauth)
	    
	    Ok("Success")
	    
    }
    
    
    
  }
  
  def requestPendingGames(user:String, appID:String) = Action{ request =>
  
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
    
    	var games = DatabaseAccessor.getPendingGames(user)
  
    	var newGames = List[Game]()
    
	    for(g <- games){
	      
	      var whiteH = DatabaseAccessor.getUser(g.white, DatabaseAccessor.AUTHKEY, true).handle
	      var blackH = DatabaseAccessor.getUser(g.black, DatabaseAccessor.AUTHKEY, true).handle
	      
	      newGames ::= new Game(g.id, whiteH, blackH)
	      
	    }
    
    	
    	Ok(generate(newGames))
    }
  }
  
  def requestPendingFriends(user:String, appID:String) = Action { request=>
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
    
    	var friends = DatabaseAccessor.getPendingFriends(user)
  
    	Ok(generate(friends))
    }
  }
  
  def acceptFriendRequest(user:String, friend:String, accepted:String, appID:String) = Action{req =>
  
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
      
      var friendUser = DatabaseAccessor.getUser(friend, DatabaseAccessor.HANDLE, true)
      
      val pendings = DatabaseAccessor.getPendingFriends(user)
      
      if(pendings.contains(friend) && accepted.equals("true")){
        
        DatabaseAccessor.createFriendship(user, friendUser.xauth)
        Ok("Success")
        
      } else if(pendings.contains(friend)) {
        
        DatabaseAccessor.rejectFriendRequest(user, friendUser.xauth)
        Ok("Success")
        
      }else {
        
        Ok("Request not found")
        
      }
      
    }
    
  }
  
  def acceptPendingGameRequest(user:String, gameID:Long, accepted:String, appID:String) = Action {
    
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
      
      var game = DatabaseAccessor.getPendingGame(gameID)
           
      if((user.equals(game.black) || user.equals(game.white) ) && accepted.equals("true")){
        
        DatabaseAccessor.createGame(game.white, game.black, game.id)
        
        Ok("Success")
        
      } else if(user.equals(game.black) || user.equals(game.white)) {
        
        DatabaseAccessor.rejectGame(game.id)
        Ok("Success")
        
      } else {
        
        Ok("Request not found")
        
      }
      
    }
    
  }
  
  
}