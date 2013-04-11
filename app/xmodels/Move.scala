package xmodels
import javachess.jcMove
case class Move (move : String, player : String) {

  def convertToEngine() : jcMove = {
    
    var mov = new jcMove
    
    if(move.equals("RESIG")){
      
      mov.MoveType = jcMove.MOVE_RESIGN
      
    }
    
    return mov
    
  }
  
}