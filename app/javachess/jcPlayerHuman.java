/**************************************************************************
 * jcPlayerHuman.java - Interface to a human player
 * by François Dominic Laramée
 *
 * Purpose: This object allows a human player to play JavaChess.  Its only
 * real job is to query the human player for his move.
 *
 * Note that this is not the cleanest, most user-friendly piece of code around;
 * it is only intended as a test harness for the AI player, not as a full-
 * fledged application (which would be graphical, for one thing!)
 *
 * History:
 * 11.06.00 Creation
 **************************************************************************/
package javachess;
import javachess.jcMove;
import javachess.jcBoard;
import javachess.jcMoveListGenerator;
import java.io.*;

public class jcPlayerHuman extends jcPlayer
{
  // The keyboard
  InputStreamReader kbd;
  char linebuf[];

  // Validation help
  jcMoveListGenerator Pseudos;
  jcBoard Successor;

  // Constructor
  public jcPlayerHuman( int which, InputStreamReader syskbd )
  {
    this.SetSide( which );
    linebuf = new char[ 10 ];
    kbd = syskbd;
    Pseudos = new jcMoveListGenerator();
    Successor = new jcBoard();
  }

  // public jcMove GetMove( theBoard )
  // Getting a move from the human player.  Sorry, but this is very, very
  // primitive: you need to enter square numbers instead of piece ID's, and
  // both square numbers must be entered with two digits.  Ex.: 04 00
  public jcMove GetMove( jcBoard theBoard )
  {
    // Read the move from the command line
    boolean ok = false;
    jcMove Mov = new jcMove();
    do
    {
      System.out.println( "Your move, " + PlayerStrings[ this.GetSide() ] + "?" );

      // Get data from the command line
      int len = 0;
      do {
        try{
          len = kbd.read( linebuf, 0, 5 );
        } catch( IOException e ) {}
      } while ( len < 3 );

      String line = new String( linebuf, 0, 5 );

      if ( line.equalsIgnoreCase( "RESIG" ) )
      {
        Mov.MoveType = jcMove.MOVE_RESIGN;
        return( Mov );
      }

      // Extract the source and destination squares from the line buffer
      Mov.SourceSquare = Integer.parseInt( line.substring( 0, 2 ) );
      Mov.DestinationSquare = Integer.parseInt( line.substring( 3, 5 ) );
      if ( ( Mov.SourceSquare < 0 ) || ( Mov.SourceSquare > 63 ) )
      {
        System.out.println( "Sorry, illegal source square " + Mov.SourceSquare );
        continue;
      }
      if ( ( Mov.DestinationSquare < 0 ) || ( Mov.DestinationSquare > 63 ) )
      {
        System.out.println( "Sorry, illegal destination square " + Mov.DestinationSquare );
        continue;
      }

      // Time to try to figure out what the move means!
      if ( theBoard.GetCurrentPlayer() == jcPlayer.SIDE_WHITE )
      {
        // Is there a piece (of the moving player) on SourceSquare?
        // If not, abort
        Mov.MovingPiece = theBoard.FindWhitePiece( Mov.SourceSquare );
        if ( Mov.MovingPiece == jcBoard.EMPTY_SQUARE )
        {
          System.out.println( "Sorry, You don't have a piece at square " + Mov.SourceSquare );
          continue;
        }

        // Three cases: there is a piece on the destination square (a capture),
        // the destination square allows an en passant capture, or it is a
        // simple non-capture move.  If the destination contains a piece of the
        // moving side, abort
        if ( theBoard.FindWhitePiece( Mov.DestinationSquare ) != jcBoard.EMPTY_SQUARE )
        {
          System.out.println( "Sorry, can't capture your own piece!" );
          continue;
        }
        Mov.CapturedPiece = theBoard.FindBlackPiece( Mov.DestinationSquare );
        if ( Mov.CapturedPiece != jcBoard.EMPTY_SQUARE )
          Mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
        else if ( ( theBoard.GetEnPassantPawn() == ( 1 << Mov.DestinationSquare ) ) &&
                  ( Mov.MovingPiece == jcBoard.WHITE_PAWN ) )
        {
          Mov.CapturedPiece = jcBoard.BLACK_PAWN;
          Mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
        }

        // If the move isn't a capture, it may be a castling attempt
        else if ( ( Mov.MovingPiece == jcBoard.WHITE_KING ) &&
                  ( ( Mov.SourceSquare - Mov.DestinationSquare ) == 2 ) )
          Mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
        else if ( ( Mov.MovingPiece == jcBoard.WHITE_KING ) &&
                  ( ( Mov.SourceSquare - Mov.DestinationSquare ) == -2 ) )
          Mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
        else
          Mov.MoveType = jcMove.MOVE_NORMAL;
      }
      else
      {
        Mov.MovingPiece = theBoard.FindBlackPiece( Mov.SourceSquare );
        if ( Mov.MovingPiece == jcBoard.EMPTY_SQUARE )
        {
          System.out.println( "Sorry, you don't have a piece in square " + Mov.SourceSquare );
          continue;
        }

        if ( theBoard.FindBlackPiece( Mov.DestinationSquare ) != jcBoard.EMPTY_SQUARE )
        {
          System.out.println( "Sorry, you can't capture your own piece in square " + Mov.DestinationSquare );
          continue;
        }
        Mov.CapturedPiece = theBoard.FindWhitePiece( Mov.DestinationSquare );
        if ( Mov.CapturedPiece != jcBoard.EMPTY_SQUARE )
          Mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
        else if ( ( theBoard.GetEnPassantPawn() == ( 1 << Mov.DestinationSquare ) ) &&
                  ( Mov.MovingPiece == jcBoard.BLACK_PAWN ) )
        {
          Mov.CapturedPiece = jcBoard.WHITE_PAWN;
          Mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
        }
        else if ( ( Mov.MovingPiece == jcBoard.BLACK_KING ) &&
                  ( ( Mov.SourceSquare - Mov.DestinationSquare ) == 2 ) )
          Mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
        else if ( ( Mov.MovingPiece == jcBoard.BLACK_KING ) &&
                  ( ( Mov.SourceSquare - Mov.DestinationSquare ) == -2 ) )
          Mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
        else
          Mov.MoveType = jcMove.MOVE_NORMAL;
      }

      // Now, if the move results in a pawn promotion, we must ask the user
      // for the type of promotion!
      if ( ( ( Mov.MovingPiece == jcBoard.WHITE_PAWN ) && ( Mov.DestinationSquare < 8 ) ) ||
           ( ( Mov.MovingPiece == jcBoard.BLACK_PAWN ) && ( Mov.DestinationSquare > 55 ) ) )
      {
        int car = -1;
        System.out.println( "Promote the pawn to [K]night, [R]ook, [B]ishop, [Q]ueen?" );
        do
        {
          try { car = kbd.read(); } catch( IOException e ) {}
        } while ( ( car != 'K' ) && ( car != 'k' ) && ( car != 'b' ) && ( car != 'B' )
               && ( car != 'R' ) && ( car != 'r' ) && ( car != 'Q' ) && ( car != 'q' ) );
        if ( ( car == 'K' ) || ( car == 'k' ) )
          Mov.MoveType += jcMove.MOVE_PROMOTION_KNIGHT;
        else if ( ( car == 'B' ) || ( car == 'b' ) )
          Mov.MoveType += jcMove.MOVE_PROMOTION_BISHOP;
        else if ( ( car == 'R' ) || ( car == 'r' ) )
          Mov.MoveType += jcMove.MOVE_PROMOTION_ROOK;
        else
          Mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
      }

      // OK, now let's see if the move is actually legal!  First step: a check
      // for pseudo-legality, i.e., is it a valid successor to the current
      // board?
      Pseudos.ComputeLegalMoves( theBoard );
      if ( !Pseudos.Find( Mov ) )
      {
        System.out.print( "Sorry, this move is not in the pseudo-legal list: " );
        Mov.Print();
        Pseudos.Print();
        continue;
      }

      // If pseudo-legal, then verify whether it leaves the king in check
      Successor.Clone( theBoard );
      Successor.ApplyMove( Mov );
      if ( !Pseudos.ComputeLegalMoves( Successor ) )
      {
        System.out.print( "Sorry, this move leaves your king in check: " );
        Mov.Print();
        continue;
      }

      // If we have made it here, we have a valid move to play!
      System.out.println( "Move is accepted..." );
      ok = true;

    } while ( !ok );

    return( Mov );
  }
}