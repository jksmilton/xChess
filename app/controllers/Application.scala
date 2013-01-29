package controllers

import play.api._
import play.api.mvc._
import xmodels.DatabaseAccessor
import xmodels.ChessUser
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
    
    
    Ok("")
  }
  
}