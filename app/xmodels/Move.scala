package xmodels
import javachess.jcMove
import controllers.MoveException
case class Move (move : String, player : String) {

  val A : Int = 55
  val B : Int = 47
  val C : Int = 39
  val D : Int = 31
  val E : Int = 23
  val F : Int = 15
  val G : Int = 7
  val H : Int = -1
  
  def convertToEngine() : jcMove = {
    
    var mov = new jcMove
    mov.MoveType == jcMove.MOVE_NORMAL
    
    if(move.equals("RESIG")){
      
      mov.MoveType = jcMove.MOVE_RESIGN
      
    } else {
      
      var positions = move.split(" ")
      mov.SourceSquare = parseSquare(positions(0))
      mov.DestinationSquare = parseSquare(positions(1))
      mov.MoveType == jcMove.MOVE_NORMAL
      if (positions.length > 2 && positions(2).equals("Q")){
        mov.MoveType = jcMove.MOVE_PROMOTION_QUEEN
      } else if (positions.length > 2 && positions(2).equals("K")){
        mov.MoveType = jcMove.MOVE_PROMOTION_KNIGHT
      } else if (positions.length > 2 && positions(2).equals("R")){
        mov.MoveType = jcMove.MOVE_PROMOTION_ROOK
      } else if (positions.length > 2 && positions(2).equals("B")){
        mov.MoveType = jcMove.MOVE_PROMOTION_BISHOP
      } 
      
    }
    
    return mov
    
  }
  
  def parseSquare(pos : String) : Int = {
    
    val row = pos.charAt(0)
    var result : Int = pos.charAt(1).toString.toInt
    
    if(row.equals('A')){
      result += A
    } else if(row.equals('B')){
      result += B
    } else if(row.equals('C')){
      result += C
    } else if(row.equals('D')){
      result += D
    } else if(row.equals('E')){
      result += E
    } else if(row.equals('F')){
      result += F
    } else if(row.equals('G')){
      result += G
    } else if(row.equals('H')){
      result += H
    } else {
      throw new MoveException(MoveException.failedToParse)
    }
    
    return result
  }
  
}