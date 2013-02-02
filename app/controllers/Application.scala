package controllers

import play.api._
import play.api.mvc._
import xmodels._
import play.api.libs.json._
import com.codahale.jerkson.Json._


object Application extends Controller {
  
  def index = Action {
    Ok("")
  }
  
  def getUser(username : String, email : String) = Action { request =>
    
    var user = DatabaseAccessor.getUser(username)
    
    if(user == null){
      
      Ok("fail!")
      
    } else {
      
      val jsonResult = generate(user)
      
      Ok(jsonResult)
     
    }
    
    
    
  }
  
  def createUser = Action(parse.json){ request=>
  
    var username : String = null
    var email : String = null
    println("Incoming json: " + request.body)
    
      
    username = (request.body \ "user").asOpt[String].map { name =>
      name
    }.get
  
    email = (request.body \ "email").asOpt[String].map { uEmail =>
      uEmail                
    }.get
      
    var user = new ChessUser(username, email)
    println("User: " + user)
    DatabaseAccessor.createUser(user)
    
    Ok("SUCCESS")
    
  }
  
  def getGame(gameID : Long) = Action{ request=>
    
    val transcript = new Transcript(DatabaseAccessor.getTranscript(gameID))
        
    Ok(generate(transcript))
    
  }
  
  def addFriend(user: String, friend: String) = Action{ request=>
    
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