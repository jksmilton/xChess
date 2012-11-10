package models
import scala.collection.immutable.List

class User (name: String, email: String){

  private var freinds = List()
  
  def addFreind(username: User) : String = {
    
    if(!(freinds contains username)){
    	username :: freinds
    	return "freind added"
    } else {
    	return username + " is already a freind"
    }
    
  }
  
  override def toString () : String = name
  
}