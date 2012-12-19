package controllers

import play.api._
import play.api.mvc._
import xmodels.DatabaseAccessor

object Application extends Controller {
  
  def index = Action {
    Ok("Hello World")
  }
  
  def getUser(username : String, email : String) = Action { request =>
    
    var user = DatabaseAccessor.getUser(username)
    
    if(user == null){
      
      Ok("fail!")
      
    } else {
      
      var resultString = ""
      
      user.friends.foreach(friend =>
        
        resultString += friend.name + ";"
        
      )
      
      Ok(resultString)
      
    }
    
    
    
  }
  
}