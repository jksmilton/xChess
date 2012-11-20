package xmodels
import play.api.Play.current
import play.api.db._
import anorm._
object DatabaseAccessor {

  
  def all = DB.withTransaction{ implicit conn =>

  	
  }
  
  def createUser(user : ChessUser) = {
    
   DB.withTransaction { implicit conn =>
     
     val id = SQL("insert into users(username, email) values({username},{email})").on(
         "username" -> user.name, 
         "email" ->user.email).executeUpdate()
     
     conn.commit()
         
   }
    
  } 
  
}