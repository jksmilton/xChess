package xmodels
import scala.collection.immutable.List

case class ChessUser (name: String, email: String){

  var freinds = List[ChessUser]()
  
  def addFreind(username: ChessUser) : String = {
    
    if(!(freinds contains username)){
    	freinds ::= username
    	return "freind added"
    } else {
    	return username + " is already a freind"
    }
    
  }
  
  
  
  override def toString () : String = name
  
}