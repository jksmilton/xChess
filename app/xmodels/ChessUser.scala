package xmodels
import scala.collection.immutable.List

case class ChessUser (name: String, email: String){

  var friends = List[ChessUser]()
  
  var games = List[Game]()
  
  def addFriend(username: ChessUser) : String = {
    
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