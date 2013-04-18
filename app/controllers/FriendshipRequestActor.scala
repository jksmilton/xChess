package controllers
import scala.actors.Actor
import com.typesafe.plugin._
import play.api.Play.current
import xmodels.DatabaseAccessor
import xmodels.ChessUser
case class FriendshipRequestActor(user: ChessUser, friend: ChessUser) extends Actor {

  def act(){
        
    DatabaseAccessor.createPendingFriendship(user.xauth, friend.xauth)
    
  }
  
}