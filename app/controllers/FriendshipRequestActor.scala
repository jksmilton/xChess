package controllers
import scala.actors.Actor
import com.typesafe.plugin._
import play.api.Play.current
import xmodels.DatabaseAccessor
import xmodels.ChessUser
case class FriendshipRequestActor(user: String, friend: ChessUser) extends Actor {

  def act(){
  /*      
    val mail = use[MailerPlugin].email
    mail.setSubject(user + " has made a friend request.")
    mail.addRecipient(friend.name + " <" + friend.email + ">")
    mail.addFrom("xChess <madsquirreldisease@gmail.com>")
    mail.send("This is a friend request")
    
    DatabaseAccessor.createPendingFriendship(user, friend.name)
    */
  }
  
}