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
  
  def convertGamesToCLient(){
    
    var newGames = List[Game]()
    
    for(g <- games){
      
      var whiteH = DatabaseAccessor.getUser(g.white, DatabaseAccessor.AUTHKEY, true).handle
      var blackH = DatabaseAccessor.getUser(g.black, DatabaseAccessor.AUTHKEY, true).handle
      
      newGames ::= new Game(g.id, whiteH, blackH)
      
    }
    
    games = newGames
    
  }
  
}