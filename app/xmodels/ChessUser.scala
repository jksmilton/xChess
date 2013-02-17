package xmodels
import scala.collection.immutable.List

case class ChessUser (authString: String, xauth: String, email: String, handle: String, authSecret: String){

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
  
  override def equals(obj : Any) : Boolean = authString.equals(obj.toString())
  
  override def toString () : String = handle
  
}