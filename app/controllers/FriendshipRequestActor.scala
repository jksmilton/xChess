package controllers
import scala.actors.Actor
import com.typesafe.plugin._
import play.api.Play.current
import xmodels.DatabaseAccessor
import xmodels.ChessUser
case class FriendshipRequestActor(user: ChessUser, friend: ChessUser) extends Actor {

  def act(){
        
    val mail = use[MailerPlugin].email
    mail.setSubject(user.handle + " has made a friend request.")
    mail.addRecipient(friend.handle + " <" + friend.email + ">")
    mail.addFrom("xChess <noreply@xchess.co.uk>")
    
    mail.send("This is a friend request")
    
    DatabaseAccessor.createPendingFriendship(user.xauth, friend.xauth)
    
  }
  
}