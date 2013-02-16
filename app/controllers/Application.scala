package controllers

import play.api._
import play.api.mvc._
import xmodels._
import play.api.libs.json._
import com.codahale.jerkson.Json._
import play.api.libs.oauth.ConsumerKey
import play.api.libs.oauth.ConsumerKey
import play.api.libs.oauth.OAuth
import play.api.libs.oauth.ServiceInfo


object Application extends Controller {
  val key = ConsumerKey("www.xchess.co.uk", "V68qmc6za5w4PhVl9P5ZpN1d")
  val google = OAuth(ServiceInfo(
	    "https://www.google.com/accounts/OAuthGetRequestToken",
	    "https://www.google.com/accounts/OAuthGetAccessToken",
	    "https://www.google.com/accounts/OAuthAuthorizeToken", key),
	    false)
    
  def index = Action {
    Ok("")
  }
  
  def getUser(username : String, appID : String) = Action { request =>
    
    if(!DatabaseAccessor.authCheck(appID)){
        
        Ok("Application not authorised")
        
    } else {
      
	    var user = DatabaseAccessor.getUser(username)
	    
	    if(user == null){
	      
	      Ok("Account does not exist")
	      
	    } else {
	      
	      val jsonResult = generate(user)
	      
	      Ok(jsonResult)
	     
	    }
    
    }
    
  }
  
  def generateRequestToken(appID : String) = Action{ request =>
      val callback = "http://www.xchess.com/application/callbacks/oauth" //PRODUCTION
    //  val callback = "http://localhost:9000/application/callbacks/oauth" //TEST
          
      if(!DatabaseAccessor.authCheck(appID)){
        
          Ok("Application not authorised")
        
      } else {
      
		  google.retrieveRequestToken(callback) match {
	          case Right(t) => {
	              Ok(google.redirectUrl(t.token))
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
  
  def exchangeRequestForAccess = Action{ request=>
      
      Ok("")
      
  }
  
  def addFriend(user: String, friend: String, appID : String) = Action{ request=>
     if(!DatabaseAccessor.authCheck(appID)){
        
          Ok("Application not authorised")
        
      } else {
          
		    val userProfile = DatabaseAccessor.getUser(user)      
		    var resultStr = userProfile.addFriend(friend)
		    val friendProfile = DatabaseAccessor.getUser(friend)    
		    
		    if(resultStr == "friend added" && friendProfile != null){
		      
		      val requestActor = new FriendshipRequestActor(user, friendProfile)
		      
		      requestActor.start
		      
		    } else if(friendProfile == null){
		      
		      resultStr = friend + " does not exist"
		      
		    }
		    
		    Ok(resultStr)
		    
      }
  }
  
}