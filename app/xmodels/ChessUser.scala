package xmodels
import scala.collection.immutable.List

case class ChessUser (name: String, email: String){

  var friends = List[String]()
  
  var games = List[Game]()
  
  def addFriend(username: String) : String = {
    
    if(!(friends contains username)){
    	friends ::= username
    	return "friend added"
    } else {
    	return username + " is already a friend"
    }
    
  }
  
  override def equals(obj : Any) : Boolean = name.equals(obj.toString())
  
  override def toString () : String = name
  
}