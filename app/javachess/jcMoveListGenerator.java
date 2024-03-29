/******************************************************************************
 * jcMoveListGenerator.java - Find all pseudo-legal moves given a board state
 * by F.D. Laramée
 *
 * Purpose: Identify a list of possible moves
 *
 * History:
 * 27.07.00 Creation
 *****************************************************************************/

package javachess;
import javachess.jcBoard;
import javachess.jcMove;
import java.util.*;

public class jcMoveListGenerator
{

  /**************************************************************************
   * INSTANCE VARIABLES
   *************************************************************************/

  // The list of moves, implemented as a java collection class, namely the
  // ArrayList (dynamic array)
  ArrayList Moves;
  Iterator MovesIt;

  /**************************************************************************
   * PUBLIC METHODS
   *************************************************************************/

  // Construction
  public jcMoveListGenerator()
  {
    Moves = new ArrayList( 10 );
    MovesIt = null;
    ResetIterator();
  }


  // public void ResetIterator
  // Prepare an iterator for scanning through the list of moves
  public void ResetIterator()
  {
    // Mark the old iterator, if any, for garbage collection
    if ( MovesIt != null )
      MovesIt = null;

    // Make a new iterator ready for scanning
    MovesIt = Moves.iterator();
  }

  // Accessors
  public ArrayList GetMoveList() { return Moves; }
  public int Size() { return Moves.size(); }

  // public boolean Find( jcMove mov )
  // Look for a specific move in the list; if it is there, return true
  // This is used by the jcPlayerHuman object, to verify whether a move entered
  // by the player is actually valid
  public boolean Find( jcMove mov )
  {
    ResetIterator();
    jcMove testMove;
    while( ( testMove = Next() ) != null )
    {
      if ( mov.Equals( testMove ) )
        return true;
    }
    return false;
  }

  // public jcMove FindMoveForSquares( int source, int dest )
  // look for a move from "source" to "dest" in the list
  public jcMove FindMoveForSquares( int source, int dest )
  {
    ResetIterator();
    jcMove testMove;
    while( ( testMove = Next() ) != null )
    {
      if ( ( testMove.SourceSquare == source ) && ( testMove.DestinationSquare == dest ) )
        return testMove;
    }
    return null;
  }

  // public jcMove Next()
  // Find the next move in the list, if any
  public jcMove Next()
  {
    if ( MovesIt.hasNext() )
      return (jcMove) MovesIt.next();
    else
      return null;
  }

  // public boolean ComputeLegalMoves
  // Look at the board received as a parameter, and build a list of legal
  // moves which can be derived from it.  If there are no legal moves, or if
  // one of the moves is a king capture (which means that the opponent's
  // previous move left the king in check, which is illegal), return false.
  public boolean ComputeLegalMoves( jcBoard theBoard )
  {
    // First, clean up the old list of moves, if any
    Moves.clear();

    // Now, compute the moves, one piece type at a time
    if ( theBoard.GetCurrentPlayer() == jcPlayer.SIDE_WHITE )
    {
      // Clean up the data structures indicating that the last white move
      // was a castling, if any
      if ( theBoard.GetExtraKings( jcPlayer.SIDE_WHITE ) != 0 )
      {
        theBoard.ClearExtraKings( jcPlayer.SIDE_WHITE );
      }
      // Check for white moves, one piece type at a time
      // if any one type can capture the king, stop the work immediately
      // because the board position is illegal
      if ( !ComputeWhiteQueenMoves( theBoard ) ) return false;
      if ( !ComputeWhiteKingMoves( theBoard ) ) return false;
      if ( !ComputeWhiteRookMoves( theBoard, jcBoard.WHITE_ROOK ) ) return false;
      if ( !ComputeWhiteBishopMoves( theBoard, jcBoard.WHITE_BISHOP ) ) return false;
      if ( !ComputeWhiteKnightMoves( theBoard ) ) return false;
      if ( !ComputeWhitePawnMoves( theBoard ) ) return false;
    }
    else  // Compute Black's moves
    {
      if ( theBoard.GetExtraKings( jcPlayer.SIDE_BLACK ) != 0 )
      {
        theBoard.ClearExtraKings( jcPlayer.SIDE_BLACK );
      }
      if ( !ComputeBlackQueenMoves( theBoard ) ) return false;
      if ( !ComputeBlackKingMoves( theBoard ) ) return false;
      if ( !ComputeBlackRookMoves( theBoard, jcBoard.BLACK_ROOK ) ) return false;
      if ( !ComputeBlackBishopMoves( theBoard, jcBoard.BLACK_BISHOP ) ) return false;
      if ( !ComputeBlackKnightMoves( theBoard ) ) return false;
      if ( !ComputeBlackPawnMoves( theBoard ) ) return false;
    }

    // And finally, if there are no pseudo-legal moves at all, we have an
    // obvious error (there are no pieces on the board!); flag the condition
    if ( Moves.size() == 0 )
      return false;
    else
    {
      ResetIterator();
      return true;
    }
  }


  // public boolean ComputeQuiescenceMoves
  // Find only the moves which are relevant to quiescence search; i.e., captures
  public boolean ComputeQuiescenceMoves( jcBoard theBoard )
  {
    ComputeLegalMoves( theBoard );
    for( int i = Moves.size() - 1; i >= 0; i-- )
    {
      jcMove mov = (jcMove) Moves.get( i );
      if ( ( mov.MoveType != jcMove.MOVE_CAPTURE_ORDINARY ) &&
           ( mov.MoveType != jcMove.MOVE_CAPTURE_EN_PASSANT ) )
        Moves.remove( i );
    }
    ResetIterator();
    return( Moves.size() > 0 );
  }

  // public void Print()
  // Dump the move list to standard output, for debugging purposes
  public void Print()
  {
    // Do not use the iterator, to avoid messing up a regular operation!
    for( int it = 0; it < Moves.size(); it++ )
    {
      jcMove mov = (jcMove) Moves.get( it );
      mov.Print();
    }
  }


  /*************************************************************************
   * PRIVATE METHODS
   * For move generation
   *************************************************************************/

   private boolean ComputeWhiteQueenMoves( jcBoard theBoard )
   {
     if ( !ComputeWhiteBishopMoves( theBoard, jcBoard.WHITE_QUEEN ) ) return false;
     if ( !ComputeWhiteRookMoves( theBoard, jcBoard.WHITE_QUEEN ) ) return false;
     return true;
   }

   private boolean ComputeWhiteKingMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing position of the king
     long pieces = theBoard.GetBitBoard( jcBoard.WHITE_KING );

     // Find it!  There is only one king, so look for it and stop
     int square;
     for( square = 0; square < 64; square++ )
     {
       if ( ( jcBoard.SquareBits[ square ] & pieces ) != 0 )
         break;
     }

     // Find its moves
     for( int i = 0; i < KingMoves[ square ].length; i++ )
     {
       // Get the destination square
       int dest = KingMoves[ square ][ i ];

       // Is it occupied by a friendly piece?  If so, can't move there
       if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
             jcBoard.SquareBits[ dest ] ) != 0 )
          continue;

       // Otherwise, the move is legal, so we must prepare to add it
       jcMove mov = new jcMove();
       mov.SourceSquare = square;
       mov.DestinationSquare = dest;
       mov.MovingPiece = jcBoard.WHITE_KING;

       // Is the destination occupied by an enemy?  If so, we have a capture
       if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
            jcBoard.SquareBits[ dest ] ) != 0 )
       {
         mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
         mov.CapturedPiece = theBoard.FindBlackPiece( dest );

         // If the piece we find is a king, abort because the board
         // position is illegal!
         if ( mov.CapturedPiece == jcBoard.BLACK_KING )
         {
            return false;
         }
       }

       // otherwise, it is a simple move
       else
       {
         mov.MoveType = jcMove.MOVE_NORMAL;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
       }

       // And we add the move to the list
       Moves.add( mov );
     }

     // Now, let's consider castling...
     // Kingside first
     if ( theBoard.GetCastlingStatus( jcBoard.CASTLE_KINGSIDE + jcPlayer.SIDE_WHITE ) )
     {
       // First, check whether there are empty squares between king and rook
       if ( ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.EMPTYSQUARES_WHITE_KINGSIDE ) == 0 ) &&
            ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.EMPTYSQUARES_WHITE_KINGSIDE ) == 0 ) )
       {
         jcMove mov = new jcMove();
         mov.MovingPiece = jcBoard.WHITE_KING;
         mov.SourceSquare = 60;
         mov.DestinationSquare = 62;
         mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
         Moves.add( mov );
       }
     }
     if ( theBoard.GetCastlingStatus( jcBoard.CASTLE_QUEENSIDE + jcPlayer.SIDE_WHITE ) )
     {
       if ( ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.EMPTYSQUARES_WHITE_QUEENSIDE ) == 0 ) &&
            ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.EMPTYSQUARES_WHITE_QUEENSIDE ) == 0 ) )
       {
         jcMove mov = new jcMove();
         mov.MovingPiece = jcBoard.WHITE_KING;
         mov.SourceSquare = 60;
         mov.DestinationSquare = 58;
         mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
         Moves.add( mov );
       }
     }
     return true;
   }

   // private boolean ComputeWhiteRookMoves
   // Receives an extra "pieceType" parameter, because the queen AND the rook
   // need to use this function
   private boolean ComputeWhiteRookMoves( jcBoard theBoard, int pieceType )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( pieceType );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a white piece, so let's start looking at the bottom
     // of the board
     for( int square = 63; square >= 0; square-- )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int ray = 0; ray < RookMoves[ square ].length; ray++ )
         {
           for( int i = 0; i < RookMoves[ square ][ ray ].length; i++ )
           {
             // Get the destination square
             int dest = RookMoves[ square ][ ray ][ i ];

             // Is it occupied by a friendly piece?  If so, can't move there
             // AND we must discontinue the current ray
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
               break;

             // Otherwise, the move is legal, so we must prepare to add it
             jcMove mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = pieceType;

             // Is the destination occupied by an enemy?  If so, we have a capture
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
             {
               mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
               mov.CapturedPiece = theBoard.FindBlackPiece( dest );

               // If the piece we find is a king, abort because the board
               // position is illegal!
               if ( mov.CapturedPiece == jcBoard.BLACK_KING )
               {
                 return false;
               }

               Moves.add( mov );
               break;
             }
             // otherwise, it is a simple move
             else
             {
               mov.MoveType = jcMove.MOVE_NORMAL;
               mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
               Moves.add( mov );
             }
           }
         }
         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
      return true;
   }

   private boolean ComputeWhiteBishopMoves( jcBoard theBoard, int pieceType )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( pieceType );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a white piece, so let's start looking at the bottom
     // of the board
     for( int square = 63; square >= 0; square-- )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int ray = 0; ray < BishopMoves[ square ].length; ray++ )
         {
           for( int i = 0; i < BishopMoves[ square ][ ray ].length; i++ )
           {
             // Get the destination square
             int dest = BishopMoves[ square ][ ray ][ i ];

             // Is it occupied by a friendly piece?  If so, can't move there
             // AND we must discontinue the current ray
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
               break;

             // Otherwise, the move is legal, so we must prepare to add it
             jcMove mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = pieceType;

             // Is the destination occupied by an enemy?  If so, we have a capture
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
             {
               mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
               mov.CapturedPiece = theBoard.FindBlackPiece( dest );

               // If the piece we find is a king, abort because the board
               // position is illegal!
               if ( mov.CapturedPiece == jcBoard.BLACK_KING )
               {
                 return false;
               }

               Moves.add( mov );
               break;
             }
             // otherwise, it is a simple move
             else
             {
               mov.MoveType = jcMove.MOVE_NORMAL;
               mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
               Moves.add( mov );
             }
           }
         }
         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
     return true;
   }

   private boolean ComputeWhiteKnightMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( jcBoard.WHITE_KNIGHT );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a white piece, so let's start looking at the bottom
     // of the board
     for( int square = 63; square >= 0; square-- )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int i = 0; i < KnightMoves[ square ].length; i++ )
         {
           // Get the destination square
           int dest = KnightMoves[ square ][ i ];

           // Is it occupied by a friendly piece?  If so, can't move there
           if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                jcBoard.SquareBits[ dest ] ) != 0 )
             continue;

           // Otherwise, the move is legal, so we must prepare to add it
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_KNIGHT;

           // Is the destination occupied by an enemy?  If so, we have a capture
           if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                jcBoard.SquareBits[ dest ] ) != 0 )
           {
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );

             // If the piece we find is a king, abort because the board
             // position is illegal!
             if ( mov.CapturedPiece == jcBoard.BLACK_KING )
             {
               return false;
             }
           }
           // otherwise, it is a simple move
           else
           {
             mov.MoveType = jcMove.MOVE_NORMAL;
             mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
           }

           // And we add the move to the list
           Moves.add( mov );
         }

         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
     return true;
   }

   private boolean ComputeWhitePawnMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( jcBoard.WHITE_PAWN );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // a small optimization
     long allPieces = theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) |
                      theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES );

     // This is a white piece, so let's start looking at the bottom
     // of the board... But only consider positions where a pawn can
     // actually dwell!
     int dest;
     for( int square = 55; square >= 8; square-- )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) == 0 )
         continue;

       // First, try a normal pawn pushing
       dest = square - 8;
       if ( ( allPieces & jcBoard.SquareBits[ dest ] ) == 0 )
       {
         // Unless this push results in a promotion...
         if ( square > 15 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_NORMAL;
           Moves.add( mov );

           // Is there a chance to perform a double push? Only if the piece
           // is in its original square
           if ( square >= 48 )
           {
             dest -= 8;
             if ( ( allPieces & jcBoard.SquareBits[ dest ] ) == 0 )
             {
               mov = new jcMove();
               mov.SourceSquare = square;
               mov.DestinationSquare = dest;
               mov.MovingPiece = jcBoard.WHITE_PAWN;
               mov.MoveType = jcMove.MOVE_NORMAL;
               Moves.add( mov );
             }
           }
         }
         else  // if square < 16
         {
           // We are now looking at pawn promotion!
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_QUEEN + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_KNIGHT + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_ROOK + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_BISHOP + jcMove.MOVE_NORMAL;
           Moves.add( mov );

         }
       }

       // Now, let's try a capture
       // Three cases: the pawn is on the 1st file, the 8th file, or elsewhere
       if ( ( square % 8 ) == 0 )
       {
         dest = square - 7;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest < 8 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.CapturedPiece = theBoard.FindBlackPiece( dest );
           Moves.add( mov );

           // Other promotion captures
           if ( dest < 8 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.CapturedPiece = jcBoard.BLACK_PAWN;
           Moves.add( mov );
         }
       }
       else if ( ( square % 8 ) == 7 )
       {
         dest = square - 9;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest < 8 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.CapturedPiece = theBoard.FindBlackPiece( dest );
           Moves.add( mov );
           // Other promotion captures
           if ( dest < 8 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.CapturedPiece = jcBoard.BLACK_PAWN;
           Moves.add( mov );
         }
       }
       else
       {
         dest = square - 7;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest < 8 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.CapturedPiece = theBoard.FindBlackPiece( dest );
           Moves.add( mov );

           // Other promotion captures
           if ( dest < 8 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.CapturedPiece = jcBoard.BLACK_PAWN;
           Moves.add( mov );
         }
         dest = square - 9;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest < 8 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.CapturedPiece = theBoard.FindBlackPiece( dest );
           Moves.add( mov );
           // Other promotion captures
           if ( dest < 8 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.WHITE_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindBlackPiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.MovingPiece = jcBoard.WHITE_PAWN;
           mov.CapturedPiece = jcBoard.BLACK_PAWN;
           Moves.add( mov );
         }
       }

       // And perform the usual trick to abort the loop when we no longer
       // have any pieces to look for
       pieces ^= jcBoard.SquareBits[ square ];
       if ( pieces == 0 )
         return true;

     }
     return true;
   }


   private boolean ComputeBlackQueenMoves( jcBoard theBoard )
   {
     if ( !ComputeBlackRookMoves( theBoard, jcBoard.BLACK_QUEEN ) ) return false;
     if ( !ComputeBlackBishopMoves( theBoard, jcBoard.BLACK_QUEEN ) ) return false;
     return true;
   }

   private boolean ComputeBlackKingMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing position of the king
     long pieces = theBoard.GetBitBoard( jcBoard.BLACK_KING );

     // Find it!  There is only one king, so look for it and stop
     int square;
     for( square = 0; square < 64; square++ )
     {
       if ( ( jcBoard.SquareBits[ square ] & pieces ) != 0 )
         break;
     }

     // Find its moves
     for( int i = 0; i < KingMoves[ square ].length; i++ )
     {
       // Get the destination square
       int dest = KingMoves[ square ][ i ];

       // Is it occupied by a friendly piece?  If so, can't move there
       if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
             jcBoard.SquareBits[ dest ] ) != 0 )
          continue;

       // Otherwise, the move is legal, so we must prepare to add it
       jcMove mov = new jcMove();
       mov.SourceSquare = square;
       mov.DestinationSquare = dest;
       mov.MovingPiece = jcBoard.BLACK_KING;

       // Is the destination occupied by an enemy?  If so, we have a capture
       if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
            jcBoard.SquareBits[ dest ] ) != 0 )
       {
         mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
         mov.CapturedPiece = theBoard.FindWhitePiece( dest );

         // If the piece we find is a king, abort because the board
         // position is illegal!
         if ( mov.CapturedPiece == jcBoard.WHITE_KING )
         {
            return false;
         }
       }

       // otherwise, it is a simple move
       else
       {
         mov.MoveType = jcMove.MOVE_NORMAL;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
       }

       // And we add the move to the list
       Moves.add( mov );
     }

     // Now, let's consider castling...
     // Kingside first
     if ( theBoard.GetCastlingStatus( jcBoard.CASTLE_KINGSIDE + jcPlayer.SIDE_BLACK ) )
     {
       // First, check whether there are empty squares between king and rook
       if ( ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.EMPTYSQUARES_BLACK_KINGSIDE ) == 0 ) &&
            ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.EMPTYSQUARES_BLACK_KINGSIDE ) == 0 ) )
       {
         jcMove mov = new jcMove();
         mov.MovingPiece = jcBoard.BLACK_KING;
         mov.SourceSquare = 4;
         mov.DestinationSquare = 6;
         mov.MoveType = jcMove.MOVE_CASTLING_KINGSIDE;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
         Moves.add( mov );
       }
     }
     if ( theBoard.GetCastlingStatus( jcBoard.CASTLE_QUEENSIDE + jcPlayer.SIDE_BLACK ) )
     {
       if ( ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) & jcBoard.EMPTYSQUARES_BLACK_QUEENSIDE ) == 0 ) &&
            ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.EMPTYSQUARES_BLACK_QUEENSIDE ) == 0 ) )
       {
         jcMove mov = new jcMove();
         mov.MovingPiece = jcBoard.BLACK_KING;
         mov.SourceSquare = 4;
         mov.DestinationSquare = 2;
         mov.MoveType = jcMove.MOVE_CASTLING_QUEENSIDE;
         mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
         Moves.add( mov );
       }
     }
     return true;
   }

   private boolean ComputeBlackRookMoves( jcBoard theBoard, int pieceType )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( pieceType );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a black piece, so let's start looking at the top
     // of the board
     for( int square = 0; square < 64; square++ )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int ray = 0; ray < RookMoves[ square ].length; ray++ )
         {
           for( int i = 0; i < RookMoves[ square ][ ray ].length; i++ )
           {
             // Get the destination square
             int dest = RookMoves[ square ][ ray ][ i ];

             // Is it occupied by a friendly piece?  If so, can't move there
             // AND we must discontinue the current ray
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
               break;

             // Otherwise, the move is legal, so we must prepare to add it
             jcMove mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = pieceType;

             // Is the destination occupied by an enemy?  If so, we have a capture
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
             {
               mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
               mov.CapturedPiece = theBoard.FindWhitePiece( dest );

               // If the piece we find is a king, abort because the board
               // position is illegal!
               if ( mov.CapturedPiece == jcBoard.WHITE_KING )
               {
                 return false;
               }

               Moves.add( mov );
               break;
             }
             // otherwise, it is a simple move
             else
             {
               mov.MoveType = jcMove.MOVE_NORMAL;
               mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
               Moves.add( mov );
             }
           }
         }
         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
     return true;
   }

   private boolean ComputeBlackBishopMoves( jcBoard theBoard, int pieceType )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( pieceType );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a black piece, so let's start looking at the top
     // of the board
     for( int square = 0; square < 64; square++ )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int ray = 0; ray < BishopMoves[ square ].length; ray++ )
         {
           for( int i = 0; i < BishopMoves[ square ][ ray ].length; i++ )
           {
             // Get the destination square
             int dest = BishopMoves[ square ][ ray ][ i ];

             // Is it occupied by a friendly piece?  If so, can't move there
             // AND we must discontinue the current ray
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
               break;

             // Otherwise, the move is legal, so we must prepare to add it
             jcMove mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = pieceType;

             // Is the destination occupied by an enemy?  If so, we have a capture
             if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                  jcBoard.SquareBits[ dest ] ) != 0 )
             {
               mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
               mov.CapturedPiece = theBoard.FindWhitePiece( dest );

               // If the piece we find is a king, abort because the board
               // position is illegal!
               if ( mov.CapturedPiece == jcBoard.WHITE_KING )
               {
                 return false;
               }

               // Otherwise, add the move to the list and interrupt the ray
               Moves.add( mov );
               break;
             }
             // otherwise, it is a simple move
             else
             {
               mov.MoveType = jcMove.MOVE_NORMAL;
               mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
               Moves.add( mov );
             }
           }
         }
         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
     return true;
   }

   private boolean ComputeBlackKnightMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( jcBoard.BLACK_KNIGHT );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // This is a black piece, so let's start looking at the top
     // of the board
     for( int square = 0; square < 64; square++ )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) != 0 )
       {
         // There is a piece here; find its moves
         for( int i = 0; i < KnightMoves[ square ].length; i++ )
         {
           // Get the destination square
           int dest = KnightMoves[ square ][ i ];

           // Is it occupied by a friendly piece?  If so, can't move there
           if ( ( theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) &
                jcBoard.SquareBits[ dest ] ) != 0 )
             continue;

           // Otherwise, the move is legal, so we must prepare to add it
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_KNIGHT;

           // Is the destination occupied by an enemy?  If so, we have a capture
           if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) &
                jcBoard.SquareBits[ dest ] ) != 0 )
           {
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );

             // If the piece we find is a king, abort because the board
             // position is illegal!
             if ( mov.CapturedPiece == jcBoard.WHITE_KING )
             {
               return false;
             }
           }
           // otherwise, it is a simple move
           else
           {
             mov.MoveType = jcMove.MOVE_NORMAL;
             mov.CapturedPiece = jcBoard.EMPTY_SQUARE;
           }

           // And we add the move to the list
           Moves.add( mov );
         }

         // Turn off the bit in the temporary bitboard; this way, we can
         // detect whether we have found the last of this type of piece
         // and short-circuit the loop
         pieces ^= jcBoard.SquareBits[ square ];
         if ( pieces == 0 )
           return true;
       }
     }

     // We should never get here, but the return statement is added to prevent
     // obnoxious compiler warnings
     return true;
   }

   private boolean ComputeBlackPawnMoves( jcBoard theBoard )
   {
     // Fetch the bitboard containing positions of these pieces
     long pieces = theBoard.GetBitBoard( jcBoard.BLACK_PAWN );

     // If there are no pieces of this type, no need to work very hard!
     if ( pieces == 0 )
     {
       return true;
     }

     // a small optimization
     long allPieces = theBoard.GetBitBoard( jcBoard.ALL_BLACK_PIECES ) |
                      theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES );

     // This is a black piece, so let's start looking at the top
     // of the board... But only consider positions where a pawn can
     // actually dwell!
     int dest;
     for( int square = 8; square < 56; square++ )
     {
       if ( ( pieces & jcBoard.SquareBits[ square ] ) == 0 )
         continue;

       // First, try a normal pawn pushing
       dest = square + 8;
       if ( ( allPieces & jcBoard.SquareBits[ dest ] ) == 0 )
       {
         // Unless this push results in a promotion...
         if ( square < 48 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_NORMAL;
           Moves.add( mov );

           // Is there a chance to perform a double push? Only if the piece
           // is in its original square
           if ( square < 16 )
           {
             dest += 8;
             if ( ( allPieces & jcBoard.SquareBits[ dest ] ) == 0 )
             {
               mov = new jcMove();
               mov.SourceSquare = square;
               mov.DestinationSquare = dest;
               mov.MovingPiece = jcBoard.BLACK_PAWN;
               mov.MoveType = jcMove.MOVE_NORMAL;
               Moves.add( mov );
             }
           }
         }
         else  // if square >= 48
         {
           // We are now looking at pawn promotion!
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_QUEEN + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_KNIGHT + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_ROOK + jcMove.MOVE_NORMAL;
           Moves.add( mov );
           mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_PROMOTION_BISHOP + jcMove.MOVE_NORMAL;
           Moves.add( mov );

         }
       }

       // Now, let's try a capture
       // Three cases: the pawn is on the 1st file, the 8th file, or elsewhere
       if ( ( square % 8 ) == 0 )
       {
         dest = square + 9;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest >= 56 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.CapturedPiece = theBoard.FindWhitePiece( dest );
           Moves.add( mov );

           // Other promotion captures
           if ( dest >= 56 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.CapturedPiece = jcBoard.WHITE_PAWN;
           Moves.add( mov );
         }
       }
       else if ( ( square % 8 ) == 7 )
       {
         dest = square + 7;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest >= 56 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.CapturedPiece = theBoard.FindWhitePiece( dest );
           Moves.add( mov );
           // Other promotion captures
           if ( dest >= 56 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.CapturedPiece = jcBoard.WHITE_PAWN;
           Moves.add( mov );
         }
       }
       else
       {
         dest = square + 9;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest >= 56 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.CapturedPiece = theBoard.FindWhitePiece( dest );
           Moves.add( mov );
           // Other promotion captures
           if ( dest >= 56 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.CapturedPiece = jcBoard.WHITE_PAWN;
           Moves.add( mov );
         }
         dest = square + 7;
         // Try an ordinary capture first
         if ( ( theBoard.GetBitBoard( jcBoard.ALL_WHITE_PIECES ) & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY;
           if ( dest >= 56 )
             mov.MoveType += jcMove.MOVE_PROMOTION_QUEEN;
           mov.CapturedPiece = theBoard.FindWhitePiece( dest );
           Moves.add( mov );
           // Other promotion captures
           if ( dest >= 56 )
           {
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_KNIGHT;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_BISHOP;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
             mov = new jcMove();
             mov.SourceSquare = square;
             mov.DestinationSquare = dest;
             mov.MovingPiece = jcBoard.BLACK_PAWN;
             mov.MoveType = jcMove.MOVE_CAPTURE_ORDINARY + jcMove.MOVE_PROMOTION_ROOK;
             mov.CapturedPiece = theBoard.FindWhitePiece( dest );
             Moves.add( mov );
           }
         }
         // Now, try an en passant capture
         else if ( ( theBoard.GetEnPassantPawn() & jcBoard.SquareBits[ dest ] ) != 0 )
         {
           jcMove mov = new jcMove();
           mov.SourceSquare = square;
           mov.DestinationSquare = dest;
           mov.MoveType = jcMove.MOVE_CAPTURE_EN_PASSANT;
           mov.MovingPiece = jcBoard.BLACK_PAWN;
           mov.CapturedPiece = jcBoard.WHITE_PAWN;
           Moves.add( mov );
         }
       }

       // And perform the usual trick to abort the loop when we no longer
       // have any pieces to look for
       pieces ^= jcBoard.SquareBits[ square ];
       if ( pieces == 0 )
         return true;

     }
     return true;
   }

   /**************************************************************************
   * STATIC BLOCK
   *************************************************************************/

  // Pre-processed data structures containing all possible moves from all
  // possible squares, by piece type
  private static int KnightMoves[][];
  private static int KingMoves[][];
  private static int BishopMoves[][][];
  private static int RookMoves[][][];

  static
  {
    // Define the KnightMoves data structure;
    KnightMoves = new int[ 64 ][];
    KnightMoves[ 0 ] = new int[ 2 ];
    KnightMoves[ 0 ][ 0 ] = 10;
    KnightMoves[ 0 ][ 1 ] = 17;
    KnightMoves[ 1 ] = new int[ 3 ];
    KnightMoves[ 1 ][ 0 ] = 16;
    KnightMoves[ 1 ][ 1 ] = 18;
    KnightMoves[ 1 ][ 2 ] = 11;
    KnightMoves[ 2 ] = new int[ 4 ];
    KnightMoves[ 2 ][ 0 ] = 8;
    KnightMoves[ 2 ][ 1 ] = 12;
    KnightMoves[ 2 ][ 2 ] = 17;
    KnightMoves[ 2 ][ 3 ] = 19;
    KnightMoves[ 3 ] = new int[ 4 ];
    KnightMoves[ 3 ][ 0 ] = 9;
    KnightMoves[ 3 ][ 1 ] = 13;
    KnightMoves[ 3 ][ 2 ] = 18;
    KnightMoves[ 3 ][ 3 ] = 20;
    KnightMoves[ 4 ] = new int[ 4 ];
    KnightMoves[ 4 ][ 0 ] = 10;
    KnightMoves[ 4 ][ 1 ] = 14;
    KnightMoves[ 4 ][ 2 ] = 21;
    KnightMoves[ 4 ][ 3 ] = 19;
    KnightMoves[ 5 ] = new int[ 4 ];
    KnightMoves[ 5 ][ 0 ] = 11;
    KnightMoves[ 5 ][ 1 ] = 15;
    KnightMoves[ 5 ][ 2 ] = 22;
    KnightMoves[ 5 ][ 3 ] = 20;
    KnightMoves[ 6 ] = new int[ 3 ];
    KnightMoves[ 6 ][ 0 ] = 12;
    KnightMoves[ 6 ][ 1 ] = 21;
    KnightMoves[ 6 ][ 2 ] = 23;
    KnightMoves[ 7 ] = new int[ 2 ];
    KnightMoves[ 7 ][ 0 ] = 13;
    KnightMoves[ 7 ][ 1 ] = 22;

    KnightMoves[ 8 ] = new int[ 3 ];
    KnightMoves[ 8 ][ 0 ] = 2;
    KnightMoves[ 8 ][ 1 ] = 18;
    KnightMoves[ 8 ][ 2 ] = 25;
    KnightMoves[ 9 ] = new int[ 4 ];
    KnightMoves[ 9 ][ 0 ] = 3;
    KnightMoves[ 9 ][ 1 ] = 19;
    KnightMoves[ 9 ][ 2 ] = 24;
    KnightMoves[ 9 ][ 3 ] = 26;
    KnightMoves[ 10 ] = new int[ 6 ];
    KnightMoves[ 10 ][ 0 ] = 0;
    KnightMoves[ 10 ][ 1 ] = 4;
    KnightMoves[ 10 ][ 2 ] = 20;
    KnightMoves[ 10 ][ 3 ] = 27;
    KnightMoves[ 10 ][ 4 ] = 25;
    KnightMoves[ 10 ][ 5 ] = 16;
    KnightMoves[ 11 ] = new int[ 6 ];
    KnightMoves[ 11 ][ 0 ] = 1;
    KnightMoves[ 11 ][ 1 ] = 5;
    KnightMoves[ 11 ][ 2 ] = 21;
    KnightMoves[ 11 ][ 3 ] = 28;
    KnightMoves[ 11 ][ 4 ] = 26;
    KnightMoves[ 11 ][ 5 ] = 17;
    KnightMoves[ 12 ] = new int[ 6 ];
    KnightMoves[ 12 ][ 0 ] = 2;
    KnightMoves[ 12 ][ 1 ] = 6;
    KnightMoves[ 12 ][ 2 ] = 22;
    KnightMoves[ 12 ][ 3 ] = 29;
    KnightMoves[ 12 ][ 4 ] = 27;
    KnightMoves[ 12 ][ 5 ] = 18;
    KnightMoves[ 13 ] = new int[ 6 ];
    KnightMoves[ 13 ][ 0 ] = 3;
    KnightMoves[ 13 ][ 1 ] = 7;
    KnightMoves[ 13 ][ 2 ] = 23;
    KnightMoves[ 13 ][ 3 ] = 30;
    KnightMoves[ 13 ][ 4 ] = 28;
    KnightMoves[ 13 ][ 5 ] = 19;
    KnightMoves[ 14 ] = new int[ 4 ];
    KnightMoves[ 14 ][ 0 ] = 31;
    KnightMoves[ 14 ][ 1 ] = 29;
    KnightMoves[ 14 ][ 2 ] = 20;
    KnightMoves[ 14 ][ 3 ] = 4;
    KnightMoves[ 15 ] = new int[ 3 ];
    KnightMoves[ 15 ][ 0 ] = 5;
    KnightMoves[ 15 ][ 1 ] = 21;
    KnightMoves[ 15 ][ 2 ] = 30;

    KnightMoves[ 16 ] = new int[ 4 ];
    KnightMoves[ 16 ][ 0 ] = 1;
    KnightMoves[ 16 ][ 1 ] = 10;
    KnightMoves[ 16 ][ 2 ] = 26;
    KnightMoves[ 16 ][ 3 ] = 33;
    KnightMoves[ 17 ] = new int[ 6 ];
    KnightMoves[ 17 ][ 0 ] = 0;
    KnightMoves[ 17 ][ 1 ] = 2;
    KnightMoves[ 17 ][ 2 ] = 11;
    KnightMoves[ 17 ][ 3 ] = 27;
    KnightMoves[ 17 ][ 4 ] = 34;
    KnightMoves[ 17 ][ 5 ] = 32;
    KnightMoves[ 18 ] = new int[ 8 ];
    KnightMoves[ 18 ][ 0 ] = 1;
    KnightMoves[ 18 ][ 1 ] = 3;
    KnightMoves[ 18 ][ 2 ] = 12;
    KnightMoves[ 18 ][ 3 ] = 28;
    KnightMoves[ 18 ][ 4 ] = 35;
    KnightMoves[ 18 ][ 5 ] = 33;
    KnightMoves[ 18 ][ 6 ] = 24;
    KnightMoves[ 18 ][ 7 ] = 8;
    KnightMoves[ 19 ] = new int[ 8 ];
    KnightMoves[ 19 ][ 0 ] = 2;
    KnightMoves[ 19 ][ 1 ] = 4;
    KnightMoves[ 19 ][ 2 ] = 13;
    KnightMoves[ 19 ][ 3 ] = 29;
    KnightMoves[ 19 ][ 4 ] = 36;
    KnightMoves[ 19 ][ 5 ] = 34;
    KnightMoves[ 19 ][ 6 ] = 25;
    KnightMoves[ 19 ][ 7 ] = 9;
    KnightMoves[ 20 ] = new int[ 8 ];
    KnightMoves[ 20 ][ 0 ] = 3;
    KnightMoves[ 20 ][ 1 ] = 5;
    KnightMoves[ 20 ][ 2 ] = 14;
    KnightMoves[ 20 ][ 3 ] = 30;
    KnightMoves[ 20 ][ 4 ] = 37;
    KnightMoves[ 20 ][ 5 ] = 35;
    KnightMoves[ 20 ][ 6 ] = 26;
    KnightMoves[ 20 ][ 7 ] = 10;
    KnightMoves[ 21 ] = new int[ 8 ];
    KnightMoves[ 21 ][ 0 ] = 4;
    KnightMoves[ 21 ][ 1 ] = 6;
    KnightMoves[ 21 ][ 2 ] = 15;
    KnightMoves[ 21 ][ 3 ] = 31;
    KnightMoves[ 21 ][ 4 ] = 38;
    KnightMoves[ 21 ][ 5 ] = 36;
    KnightMoves[ 21 ][ 6 ] = 27;
    KnightMoves[ 21 ][ 7 ] = 11;
    KnightMoves[ 22 ] = new int[ 6 ];
    KnightMoves[ 22 ][ 0 ] = 5;
    KnightMoves[ 22 ][ 1 ] = 7;
    KnightMoves[ 22 ][ 2 ] = 39;
    KnightMoves[ 22 ][ 3 ] = 37;
    KnightMoves[ 22 ][ 4 ] = 28;
    KnightMoves[ 22 ][ 5 ] = 12;
    KnightMoves[ 23 ] = new int[ 4 ];
    KnightMoves[ 23 ][ 0 ] = 6;
    KnightMoves[ 23 ][ 1 ] = 38;
    KnightMoves[ 23 ][ 2 ] = 29;
    KnightMoves[ 23 ][ 3 ] = 13;

    KnightMoves[ 24 ] = new int[ 4 ];
    KnightMoves[ 24 ][ 0 ] = 9;
    KnightMoves[ 24 ][ 1 ] = 18;
    KnightMoves[ 24 ][ 2 ] = 34;
    KnightMoves[ 24 ][ 3 ] = 41;
    KnightMoves[ 25 ] = new int[ 6 ];
    KnightMoves[ 25 ][ 0 ] = 8;
    KnightMoves[ 25 ][ 1 ] = 10;
    KnightMoves[ 25 ][ 2 ] = 19;
    KnightMoves[ 25 ][ 3 ] = 35;
    KnightMoves[ 25 ][ 4 ] = 42;
    KnightMoves[ 25 ][ 5 ] = 40;
    KnightMoves[ 26 ] = new int[ 8 ];
    KnightMoves[ 26 ][ 0 ] = 9;
    KnightMoves[ 26 ][ 1 ] = 11;
    KnightMoves[ 26 ][ 2 ] = 20;
    KnightMoves[ 26 ][ 3 ] = 36;
    KnightMoves[ 26 ][ 4 ] = 43;
    KnightMoves[ 26 ][ 5 ] = 41;
    KnightMoves[ 26 ][ 6 ] = 32;
    KnightMoves[ 26 ][ 7 ] = 16;
    KnightMoves[ 27 ] = new int[ 8 ];
    KnightMoves[ 27 ][ 0 ] = 10;
    KnightMoves[ 27 ][ 1 ] = 12;
    KnightMoves[ 27 ][ 2 ] = 21;
    KnightMoves[ 27 ][ 3 ] = 37;
    KnightMoves[ 27 ][ 4 ] = 44;
    KnightMoves[ 27 ][ 5 ] = 42;
    KnightMoves[ 27 ][ 6 ] = 33;
    KnightMoves[ 27 ][ 7 ] = 17;
    KnightMoves[ 28 ] = new int[ 8 ];
    KnightMoves[ 28 ][ 0 ] = 11;
    KnightMoves[ 28 ][ 1 ] = 13;
    KnightMoves[ 28 ][ 2 ] = 22;
    KnightMoves[ 28 ][ 3 ] = 38;
    KnightMoves[ 28 ][ 4 ] = 45;
    KnightMoves[ 28 ][ 5 ] = 43;
    KnightMoves[ 28 ][ 6 ] = 34;
    KnightMoves[ 28 ][ 7 ] = 18;
    KnightMoves[ 29 ] = new int[ 8 ];
    KnightMoves[ 29 ][ 0 ] = 12;
    KnightMoves[ 29 ][ 1 ] = 14;
    KnightMoves[ 29 ][ 2 ] = 23;
    KnightMoves[ 29 ][ 3 ] = 39;
    KnightMoves[ 29 ][ 4 ] = 46;
    KnightMoves[ 29 ][ 5 ] = 44;
    KnightMoves[ 29 ][ 6 ] = 35;
    KnightMoves[ 29 ][ 7 ] = 19;
    KnightMoves[ 30 ] = new int[ 6 ];
    KnightMoves[ 30 ][ 0 ] = 13;
    KnightMoves[ 30 ][ 1 ] = 15;
    KnightMoves[ 30 ][ 2 ] = 47;
    KnightMoves[ 30 ][ 3 ] = 45;
    KnightMoves[ 30 ][ 4 ] = 36;
    KnightMoves[ 30 ][ 5 ] = 20;
    KnightMoves[ 31 ] = new int[ 4 ];
    KnightMoves[ 31 ][ 0 ] = 14;
    KnightMoves[ 31 ][ 1 ] = 46;
    KnightMoves[ 31 ][ 2 ] = 37;
    KnightMoves[ 31 ][ 3 ] = 21;

    KnightMoves[ 32 ] = new int[ 4 ];
    KnightMoves[ 32 ][ 0 ] = 17;
    KnightMoves[ 32 ][ 1 ] = 26;
    KnightMoves[ 32 ][ 2 ] = 42;
    KnightMoves[ 32 ][ 3 ] = 49;
    KnightMoves[ 33 ] = new int[ 6 ];
    KnightMoves[ 33 ][ 0 ] = 16;
    KnightMoves[ 33 ][ 1 ] = 18;
    KnightMoves[ 33 ][ 2 ] = 27;
    KnightMoves[ 33 ][ 3 ] = 43;
    KnightMoves[ 33 ][ 4 ] = 50;
    KnightMoves[ 33 ][ 5 ] = 48;
    KnightMoves[ 34 ] = new int[ 8 ];
    KnightMoves[ 34 ][ 0 ] = 17;
    KnightMoves[ 34 ][ 1 ] = 19;
    KnightMoves[ 34 ][ 2 ] = 28;
    KnightMoves[ 34 ][ 3 ] = 44;
    KnightMoves[ 34 ][ 4 ] = 51;
    KnightMoves[ 34 ][ 5 ] = 49;
    KnightMoves[ 34 ][ 6 ] = 40;
    KnightMoves[ 34 ][ 7 ] = 24;
    KnightMoves[ 35 ] = new int[ 8 ];
    KnightMoves[ 35 ][ 0 ] = 18;
    KnightMoves[ 35 ][ 1 ] = 20;
    KnightMoves[ 35 ][ 2 ] = 29;
    KnightMoves[ 35 ][ 3 ] = 45;
    KnightMoves[ 35 ][ 4 ] = 52;
    KnightMoves[ 35 ][ 5 ] = 50;
    KnightMoves[ 35 ][ 6 ] = 41;
    KnightMoves[ 35 ][ 7 ] = 25;
    KnightMoves[ 36 ] = new int[ 8 ];
    KnightMoves[ 36 ][ 0 ] = 19;
    KnightMoves[ 36 ][ 1 ] = 21;
    KnightMoves[ 36 ][ 2 ] = 30;
    KnightMoves[ 36 ][ 3 ] = 46;
    KnightMoves[ 36 ][ 4 ] = 53;
    KnightMoves[ 36 ][ 5 ] = 51;
    KnightMoves[ 36 ][ 6 ] = 42;
    KnightMoves[ 36 ][ 7 ] = 26;
    KnightMoves[ 37 ] = new int[ 8 ];
    KnightMoves[ 37 ][ 0 ] = 20;
    KnightMoves[ 37 ][ 1 ] = 22;
    KnightMoves[ 37 ][ 2 ] = 31;
    KnightMoves[ 37 ][ 3 ] = 47;
    KnightMoves[ 37 ][ 4 ] = 54;
    KnightMoves[ 37 ][ 5 ] = 52;
    KnightMoves[ 37 ][ 6 ] = 43;
    KnightMoves[ 37 ][ 7 ] = 27;
    KnightMoves[ 38 ] = new int[ 6 ];
    KnightMoves[ 38 ][ 0 ] = 21;
    KnightMoves[ 38 ][ 1 ] = 23;
    KnightMoves[ 38 ][ 2 ] = 55;
    KnightMoves[ 38 ][ 3 ] = 53;
    KnightMoves[ 38 ][ 4 ] = 44;
    KnightMoves[ 38 ][ 5 ] = 28;
    KnightMoves[ 39 ] = new int[ 4 ];
    KnightMoves[ 39 ][ 0 ] = 22;
    KnightMoves[ 39 ][ 1 ] = 54;
    KnightMoves[ 39 ][ 2 ] = 45;
    KnightMoves[ 39 ][ 3 ] = 29;

    KnightMoves[ 40 ] = new int[ 4 ];
    KnightMoves[ 40 ][ 0 ] = 25;
    KnightMoves[ 40 ][ 1 ] = 34;
    KnightMoves[ 40 ][ 2 ] = 50;
    KnightMoves[ 40 ][ 3 ] = 57;
    KnightMoves[ 41 ] = new int[ 6 ];
    KnightMoves[ 41 ][ 0 ] = 26;
    KnightMoves[ 41 ][ 1 ] = 24;
    KnightMoves[ 41 ][ 2 ] = 35;
    KnightMoves[ 41 ][ 3 ] = 51;
    KnightMoves[ 41 ][ 4 ] = 58;
    KnightMoves[ 41 ][ 5 ] = 56;
    KnightMoves[ 42 ] = new int[ 8 ];
    KnightMoves[ 42 ][ 0 ] = 25;
    KnightMoves[ 42 ][ 1 ] = 27;
    KnightMoves[ 42 ][ 2 ] = 36;
    KnightMoves[ 42 ][ 3 ] = 52;
    KnightMoves[ 42 ][ 4 ] = 59;
    KnightMoves[ 42 ][ 5 ] = 57;
    KnightMoves[ 42 ][ 6 ] = 48;
    KnightMoves[ 42 ][ 7 ] = 32;
    KnightMoves[ 43 ] = new int[ 8 ];
    KnightMoves[ 43 ][ 0 ] = 26;
    KnightMoves[ 43 ][ 1 ] = 28;
    KnightMoves[ 43 ][ 2 ] = 37;
    KnightMoves[ 43 ][ 3 ] = 53;
    KnightMoves[ 43 ][ 4 ] = 60;
    KnightMoves[ 43 ][ 5 ] = 58;
    KnightMoves[ 43 ][ 6 ] = 49;
    KnightMoves[ 43 ][ 7 ] = 33;
    KnightMoves[ 44 ] = new int[ 8 ];
    KnightMoves[ 44 ][ 0 ] = 27;
    KnightMoves[ 44 ][ 1 ] = 29;
    KnightMoves[ 44 ][ 2 ] = 38;
    KnightMoves[ 44 ][ 3 ] = 54;
    KnightMoves[ 44 ][ 4 ] = 61;
    KnightMoves[ 44 ][ 5 ] = 59;
    KnightMoves[ 44 ][ 6 ] = 50;
    KnightMoves[ 44 ][ 7 ] = 34;
    KnightMoves[ 45 ] = new int[ 8 ];
    KnightMoves[ 45 ][ 0 ] = 28;
    KnightMoves[ 45 ][ 1 ] = 30;
    KnightMoves[ 45 ][ 2 ] = 39;
    KnightMoves[ 45 ][ 3 ] = 55;
    KnightMoves[ 45 ][ 4 ] = 62;
    KnightMoves[ 45 ][ 5 ] = 60;
    KnightMoves[ 45 ][ 6 ] = 51;
    KnightMoves[ 45 ][ 7 ] = 35;
    KnightMoves[ 46 ] = new int[ 6 ];
    KnightMoves[ 46 ][ 0 ] = 29;
    KnightMoves[ 46 ][ 1 ] = 31;
    KnightMoves[ 46 ][ 2 ] = 63;
    KnightMoves[ 46 ][ 3 ] = 61;
    KnightMoves[ 46 ][ 4 ] = 52;
    KnightMoves[ 46 ][ 5 ] = 36;
    KnightMoves[ 47 ] = new int[ 4 ];
    KnightMoves[ 47 ][ 0 ] = 30;
    KnightMoves[ 47 ][ 1 ] = 62;
    KnightMoves[ 47 ][ 2 ] = 53;
    KnightMoves[ 47 ][ 3 ] = 37;

    KnightMoves[ 48 ] = new int[ 3 ];
    KnightMoves[ 48 ][ 0 ] = 33;
    KnightMoves[ 48 ][ 1 ] = 42;
    KnightMoves[ 48 ][ 2 ] = 58;
    KnightMoves[ 49 ] = new int[ 4 ];
    KnightMoves[ 49 ][ 0 ] = 32;
    KnightMoves[ 49 ][ 1 ] = 34;
    KnightMoves[ 49 ][ 2 ] = 43;
    KnightMoves[ 49 ][ 3 ] = 59;
    KnightMoves[ 50 ] = new int[ 6 ];
    KnightMoves[ 50 ][ 0 ] = 40;
    KnightMoves[ 50 ][ 1 ] = 33;
    KnightMoves[ 50 ][ 2 ] = 35;
    KnightMoves[ 50 ][ 3 ] = 44;
    KnightMoves[ 50 ][ 4 ] = 60;
    KnightMoves[ 50 ][ 5 ] = 56;
    KnightMoves[ 51 ] = new int[ 6 ];
    KnightMoves[ 51 ][ 0 ] = 41;
    KnightMoves[ 51 ][ 1 ] = 34;
    KnightMoves[ 51 ][ 2 ] = 36;
    KnightMoves[ 51 ][ 3 ] = 45;
    KnightMoves[ 51 ][ 4 ] = 61;
    KnightMoves[ 51 ][ 5 ] = 57;
    KnightMoves[ 52 ] = new int[ 6 ];
    KnightMoves[ 52 ][ 0 ] = 42;
    KnightMoves[ 52 ][ 1 ] = 35;
    KnightMoves[ 52 ][ 2 ] = 37;
    KnightMoves[ 52 ][ 3 ] = 46;
    KnightMoves[ 52 ][ 4 ] = 62;
    KnightMoves[ 52 ][ 5 ] = 58;
    KnightMoves[ 53 ] = new int[ 6 ];
    KnightMoves[ 53 ][ 0 ] = 43;
    KnightMoves[ 53 ][ 1 ] = 36;
    KnightMoves[ 53 ][ 2 ] = 38;
    KnightMoves[ 53 ][ 3 ] = 47;
    KnightMoves[ 53 ][ 4 ] = 63;
    KnightMoves[ 53 ][ 5 ] = 59;
    KnightMoves[ 54 ] = new int[ 4 ];
    KnightMoves[ 54 ][ 0 ] = 39;
    KnightMoves[ 54 ][ 1 ] = 60;
    KnightMoves[ 54 ][ 2 ] = 44;
    KnightMoves[ 54 ][ 3 ] = 37;
    KnightMoves[ 55 ] = new int[ 3 ];
    KnightMoves[ 55 ][ 0 ] = 38;
    KnightMoves[ 55 ][ 1 ] = 45;
    KnightMoves[ 55 ][ 2 ] = 61;

    KnightMoves[ 56 ] = new int[ 2 ];
    KnightMoves[ 56 ][ 0 ] = 41;
    KnightMoves[ 56 ][ 1 ] = 50;
    KnightMoves[ 57 ] = new int[ 3 ];
    KnightMoves[ 57 ][ 0 ] = 40;
    KnightMoves[ 57 ][ 1 ] = 42;
    KnightMoves[ 57 ][ 2 ] = 51;
    KnightMoves[ 58 ] = new int[ 4 ];
    KnightMoves[ 58 ][ 0 ] = 48;
    KnightMoves[ 58 ][ 1 ] = 41;
    KnightMoves[ 58 ][ 2 ] = 43;
    KnightMoves[ 58 ][ 3 ] = 52;
    KnightMoves[ 59 ] = new int[ 4 ];
    KnightMoves[ 59 ][ 0 ] = 49;
    KnightMoves[ 59 ][ 1 ] = 42;
    KnightMoves[ 59 ][ 2 ] = 44;
    KnightMoves[ 59 ][ 3 ] = 53;
    KnightMoves[ 60 ] = new int[ 4 ];
    KnightMoves[ 60 ][ 0 ] = 50;
    KnightMoves[ 60 ][ 1 ] = 43;
    KnightMoves[ 60 ][ 2 ] = 45;
    KnightMoves[ 60 ][ 3 ] = 54;
    KnightMoves[ 61 ] = new int[ 4 ];
    KnightMoves[ 61 ][ 0 ] = 51;
    KnightMoves[ 61 ][ 1 ] = 44;
    KnightMoves[ 61 ][ 2 ] = 46;
    KnightMoves[ 61 ][ 3 ] = 55;
    KnightMoves[ 62 ] = new int[ 3 ];
    KnightMoves[ 62 ][ 0 ] = 52;
    KnightMoves[ 62 ][ 1 ] = 45;
    KnightMoves[ 62 ][ 2 ] = 47;
    KnightMoves[ 63 ] = new int[ 2 ];
    KnightMoves[ 63 ][ 0 ] = 53;
    KnightMoves[ 63 ][ 1 ] = 46;

    KingMoves = new int[ 64 ][];
    KingMoves[ 0 ] = new int[ 3 ];
    KingMoves[ 0 ][ 0 ] = 1;
    KingMoves[ 0 ][ 1 ] = 8;
    KingMoves[ 0 ][ 2 ] = 9;
    KingMoves[ 1 ] = new int[ 5 ];
    KingMoves[ 1 ][ 0 ] = 0;
    KingMoves[ 1 ][ 1 ] = 2;
    KingMoves[ 1 ][ 2 ] = 8;
    KingMoves[ 1 ][ 3 ] = 9;
    KingMoves[ 1 ][ 4 ] = 10;
    KingMoves[ 2 ] = new int[ 5 ];
    KingMoves[ 2 ][ 0 ] = 1;
    KingMoves[ 2 ][ 1 ] = 3;
    KingMoves[ 2 ][ 2 ] = 9;
    KingMoves[ 2 ][ 3 ] = 10;
    KingMoves[ 2 ][ 4 ] = 11;
    KingMoves[ 3 ] = new int[ 5 ];
    KingMoves[ 3 ][ 0 ] = 2;
    KingMoves[ 3 ][ 1 ] = 4;
    KingMoves[ 3 ][ 2 ] = 10;
    KingMoves[ 3 ][ 3 ] = 11;
    KingMoves[ 3 ][ 4 ] = 12;
    KingMoves[ 4 ] = new int[ 5 ];
    KingMoves[ 4 ][ 0 ] = 3;
    KingMoves[ 4 ][ 1 ] = 5;
    KingMoves[ 4 ][ 2 ] = 11;
    KingMoves[ 4 ][ 3 ] = 12;
    KingMoves[ 4 ][ 4 ] = 13;
    KingMoves[ 5 ] = new int[ 5 ];
    KingMoves[ 5 ][ 0 ] = 4;
    KingMoves[ 5 ][ 1 ] = 6;
    KingMoves[ 5 ][ 2 ] = 12;
    KingMoves[ 5 ][ 3 ] = 13;
    KingMoves[ 5 ][ 4 ] = 14;
    KingMoves[ 6 ] = new int[ 5 ];
    KingMoves[ 6 ][ 0 ] = 5;
    KingMoves[ 6 ][ 1 ] = 7;
    KingMoves[ 6 ][ 2 ] = 13;
    KingMoves[ 6 ][ 3 ] = 14;
    KingMoves[ 6 ][ 4 ] = 15;
    KingMoves[ 7 ] = new int[ 3 ];
    KingMoves[ 7 ][ 0 ] = 6;
    KingMoves[ 7 ][ 1 ] = 14;
    KingMoves[ 7 ][ 2 ] = 15;

    KingMoves[ 8 ] = new int[ 5 ];
    KingMoves[ 8 ][ 0 ] = 0;
    KingMoves[ 8 ][ 1 ] = 1;
    KingMoves[ 8 ][ 2 ] = 9;
    KingMoves[ 8 ][ 3 ] = 17;
    KingMoves[ 8 ][ 4 ] = 16;
    KingMoves[ 9 ] = new int[ 8 ];
    KingMoves[ 9 ][ 0 ] = 0;
    KingMoves[ 9 ][ 1 ] = 1;
    KingMoves[ 9 ][ 2 ] = 2;
    KingMoves[ 9 ][ 3 ] = 8;
    KingMoves[ 9 ][ 4 ] = 10;
    KingMoves[ 9 ][ 5 ] = 16;
    KingMoves[ 9 ][ 6 ] = 17;
    KingMoves[ 9 ][ 7 ] = 18;
    KingMoves[ 10 ] = new int[ 8 ];
    KingMoves[ 10 ][ 0 ] = 1;
    KingMoves[ 10 ][ 1 ] = 2;
    KingMoves[ 10 ][ 2 ] = 3;
    KingMoves[ 10 ][ 3 ] = 9;
    KingMoves[ 10 ][ 4 ] = 11;
    KingMoves[ 10 ][ 5 ] = 17;
    KingMoves[ 10 ][ 6 ] = 18;
    KingMoves[ 10 ][ 7 ] = 19;
    KingMoves[ 11 ] = new int[ 8 ];
    KingMoves[ 11 ][ 0 ] = 2;
    KingMoves[ 11 ][ 1 ] = 3;
    KingMoves[ 11 ][ 2 ] = 4;
    KingMoves[ 11 ][ 3 ] = 10;
    KingMoves[ 11 ][ 4 ] = 12;
    KingMoves[ 11 ][ 5 ] = 18;
    KingMoves[ 11 ][ 6 ] = 19;
    KingMoves[ 11 ][ 7 ] = 20;
    KingMoves[ 12 ] = new int[ 8 ];
    KingMoves[ 12 ][ 0 ] = 3;
    KingMoves[ 12 ][ 1 ] = 4;
    KingMoves[ 12 ][ 2 ] = 5;
    KingMoves[ 12 ][ 3 ] = 11;
    KingMoves[ 12 ][ 4 ] = 13;
    KingMoves[ 12 ][ 5 ] = 19;
    KingMoves[ 12 ][ 6 ] = 20;
    KingMoves[ 12 ][ 7 ] = 21;
    KingMoves[ 13 ] = new int[ 8 ];
    KingMoves[ 13 ][ 0 ] = 4;
    KingMoves[ 13 ][ 1 ] = 5;
    KingMoves[ 13 ][ 2 ] = 6;
    KingMoves[ 13 ][ 3 ] = 12;
    KingMoves[ 13 ][ 4 ] = 14;
    KingMoves[ 13 ][ 5 ] = 20;
    KingMoves[ 13 ][ 6 ] = 21;
    KingMoves[ 13 ][ 7 ] = 22;
    KingMoves[ 14 ] = new int[ 8 ];
    KingMoves[ 14 ][ 0 ] = 5;
    KingMoves[ 14 ][ 1 ] = 6;
    KingMoves[ 14 ][ 2 ] = 7;
    KingMoves[ 14 ][ 3 ] = 13;
    KingMoves[ 14 ][ 4 ] = 15;
    KingMoves[ 14 ][ 5 ] = 21;
    KingMoves[ 14 ][ 6 ] = 22;
    KingMoves[ 14 ][ 7 ] = 23;
    KingMoves[ 15 ] = new int[ 5 ];
    KingMoves[ 15 ][ 0 ] = 6;
    KingMoves[ 15 ][ 1 ] = 7;
    KingMoves[ 15 ][ 2 ] = 14;
    KingMoves[ 15 ][ 3 ] = 22;
    KingMoves[ 15 ][ 4 ] = 23;

    KingMoves[ 16 ] = new int[ 5 ];
    KingMoves[ 16 ][ 0 ] = 9;
    KingMoves[ 16 ][ 1 ] = 8;
    KingMoves[ 16 ][ 2 ] = 17;
    KingMoves[ 16 ][ 3 ] = 24;
    KingMoves[ 16 ][ 4 ] = 25;
    KingMoves[ 17 ] = new int[ 8 ];
    KingMoves[ 17 ][ 0 ] = 8;
    KingMoves[ 17 ][ 1 ] = 9;
    KingMoves[ 17 ][ 2 ] = 10;
    KingMoves[ 17 ][ 3 ] = 16;
    KingMoves[ 17 ][ 4 ] = 18;
    KingMoves[ 17 ][ 5 ] = 24;
    KingMoves[ 17 ][ 6 ] = 25;
    KingMoves[ 17 ][ 7 ] = 26;
    KingMoves[ 18 ] = new int[ 8 ];
    KingMoves[ 18 ][ 0 ] = 9;
    KingMoves[ 18 ][ 1 ] = 10;
    KingMoves[ 18 ][ 2 ] = 11;
    KingMoves[ 18 ][ 3 ] = 17;
    KingMoves[ 18 ][ 4 ] = 19;
    KingMoves[ 18 ][ 5 ] = 25;
    KingMoves[ 18 ][ 6 ] = 26;
    KingMoves[ 18 ][ 7 ] = 27;
    KingMoves[ 19 ] = new int[ 8 ];
    KingMoves[ 19 ][ 0 ] = 10;
    KingMoves[ 19 ][ 1 ] = 11;
    KingMoves[ 19 ][ 2 ] = 12;
    KingMoves[ 19 ][ 3 ] = 18;
    KingMoves[ 19 ][ 4 ] = 20;
    KingMoves[ 19 ][ 5 ] = 26;
    KingMoves[ 19 ][ 6 ] = 27;
    KingMoves[ 19 ][ 7 ] = 28;
    KingMoves[ 20 ] = new int[ 8 ];
    KingMoves[ 20 ][ 0 ] = 11;
    KingMoves[ 20 ][ 1 ] = 12;
    KingMoves[ 20 ][ 2 ] = 13;
    KingMoves[ 20 ][ 3 ] = 19;
    KingMoves[ 20 ][ 4 ] = 21;
    KingMoves[ 20 ][ 5 ] = 27;
    KingMoves[ 20 ][ 6 ] = 28;
    KingMoves[ 20 ][ 7 ] = 29;
    KingMoves[ 21 ] = new int[ 8 ];
    KingMoves[ 21 ][ 0 ] = 12;
    KingMoves[ 21 ][ 1 ] = 13;
    KingMoves[ 21 ][ 2 ] = 14;
    KingMoves[ 21 ][ 3 ] = 20;
    KingMoves[ 21 ][ 4 ] = 22;
    KingMoves[ 21 ][ 5 ] = 28;
    KingMoves[ 21 ][ 6 ] = 29;
    KingMoves[ 21 ][ 7 ] = 30;
    KingMoves[ 22 ] = new int[ 8 ];
    KingMoves[ 22 ][ 0 ] = 13;
    KingMoves[ 22 ][ 1 ] = 14;
    KingMoves[ 22 ][ 2 ] = 15;
    KingMoves[ 22 ][ 3 ] = 21;
    KingMoves[ 22 ][ 4 ] = 23;
    KingMoves[ 22 ][ 5 ] = 29;
    KingMoves[ 22 ][ 6 ] = 30;
    KingMoves[ 22 ][ 7 ] = 31;
    KingMoves[ 23 ] = new int[ 5 ];
    KingMoves[ 23 ][ 0 ] = 14;
    KingMoves[ 23 ][ 1 ] = 15;
    KingMoves[ 23 ][ 2 ] = 22;
    KingMoves[ 23 ][ 3 ] = 30;
    KingMoves[ 23 ][ 4 ] = 31;

    KingMoves[ 24 ] = new int[ 5 ];
    KingMoves[ 24 ][ 0 ] = 16;
    KingMoves[ 24 ][ 1 ] = 17;
    KingMoves[ 24 ][ 2 ] = 25;
    KingMoves[ 24 ][ 3 ] = 32;
    KingMoves[ 24 ][ 4 ] = 33;
    KingMoves[ 25 ] = new int[ 8 ];
    KingMoves[ 25 ][ 0 ] = 16;
    KingMoves[ 25 ][ 1 ] = 17;
    KingMoves[ 25 ][ 2 ] = 18;
    KingMoves[ 25 ][ 3 ] = 24;
    KingMoves[ 25 ][ 4 ] = 26;
    KingMoves[ 25 ][ 5 ] = 32;
    KingMoves[ 25 ][ 6 ] = 33;
    KingMoves[ 25 ][ 7 ] = 34;
    KingMoves[ 26 ] = new int[ 8 ];
    KingMoves[ 26 ][ 0 ] = 17;
    KingMoves[ 26 ][ 1 ] = 18;
    KingMoves[ 26 ][ 2 ] = 19;
    KingMoves[ 26 ][ 3 ] = 25;
    KingMoves[ 26 ][ 4 ] = 27;
    KingMoves[ 26 ][ 5 ] = 33;
    KingMoves[ 26 ][ 6 ] = 34;
    KingMoves[ 26 ][ 7 ] = 35;
    KingMoves[ 27 ] = new int[ 8 ];
    KingMoves[ 27 ][ 0 ] = 18;
    KingMoves[ 27 ][ 1 ] = 19;
    KingMoves[ 27 ][ 2 ] = 20;
    KingMoves[ 27 ][ 3 ] = 26;
    KingMoves[ 27 ][ 4 ] = 28;
    KingMoves[ 27 ][ 5 ] = 34;
    KingMoves[ 27 ][ 6 ] = 35;
    KingMoves[ 27 ][ 7 ] = 36;
    KingMoves[ 28 ] = new int[ 8 ];
    KingMoves[ 28 ][ 0 ] = 19;
    KingMoves[ 28 ][ 1 ] = 20;
    KingMoves[ 28 ][ 2 ] = 21;
    KingMoves[ 28 ][ 3 ] = 27;
    KingMoves[ 28 ][ 4 ] = 29;
    KingMoves[ 28 ][ 5 ] = 35;
    KingMoves[ 28 ][ 6 ] = 36;
    KingMoves[ 28 ][ 7 ] = 37;
    KingMoves[ 29 ] = new int[ 8 ];
    KingMoves[ 29 ][ 0 ] = 20;
    KingMoves[ 29 ][ 1 ] = 21;
    KingMoves[ 29 ][ 2 ] = 22;
    KingMoves[ 29 ][ 3 ] = 28;
    KingMoves[ 29 ][ 4 ] = 30;
    KingMoves[ 29 ][ 5 ] = 36;
    KingMoves[ 29 ][ 6 ] = 37;
    KingMoves[ 29 ][ 7 ] = 38;
    KingMoves[ 30 ] = new int[ 8 ];
    KingMoves[ 30 ][ 0 ] = 21;
    KingMoves[ 30 ][ 1 ] = 22;
    KingMoves[ 30 ][ 2 ] = 23;
    KingMoves[ 30 ][ 3 ] = 29;
    KingMoves[ 30 ][ 4 ] = 31;
    KingMoves[ 30 ][ 5 ] = 37;
    KingMoves[ 30 ][ 6 ] = 38;
    KingMoves[ 30 ][ 7 ] = 39;
    KingMoves[ 31 ] = new int[ 5 ];
    KingMoves[ 31 ][ 0 ] = 22;
    KingMoves[ 31 ][ 1 ] = 23;
    KingMoves[ 31 ][ 2 ] = 30;
    KingMoves[ 31 ][ 3 ] = 38;
    KingMoves[ 31 ][ 4 ] = 39;

    KingMoves[ 32 ] = new int[ 5 ];
    KingMoves[ 32 ][ 0 ] = 24;
    KingMoves[ 32 ][ 1 ] = 25;
    KingMoves[ 32 ][ 2 ] = 33;
    KingMoves[ 32 ][ 3 ] = 41;
    KingMoves[ 32 ][ 4 ] = 40;
    KingMoves[ 33 ] = new int[ 8 ];
    KingMoves[ 33 ][ 0 ] = 24;
    KingMoves[ 33 ][ 1 ] = 25;
    KingMoves[ 33 ][ 2 ] = 26;
    KingMoves[ 33 ][ 3 ] = 32;
    KingMoves[ 33 ][ 4 ] = 34;
    KingMoves[ 33 ][ 5 ] = 40;
    KingMoves[ 33 ][ 6 ] = 41;
    KingMoves[ 33 ][ 7 ] = 42;
    KingMoves[ 34 ] = new int[ 8 ];
    KingMoves[ 34 ][ 0 ] = 25;
    KingMoves[ 34 ][ 1 ] = 26;
    KingMoves[ 34 ][ 2 ] = 27;
    KingMoves[ 34 ][ 3 ] = 33;
    KingMoves[ 34 ][ 4 ] = 35;
    KingMoves[ 34 ][ 5 ] = 41;
    KingMoves[ 34 ][ 6 ] = 42;
    KingMoves[ 34 ][ 7 ] = 43;
    KingMoves[ 35 ] = new int[ 8 ];
    KingMoves[ 35 ][ 0 ] = 26;
    KingMoves[ 35 ][ 1 ] = 27;
    KingMoves[ 35 ][ 2 ] = 28;
    KingMoves[ 35 ][ 3 ] = 34;
    KingMoves[ 35 ][ 4 ] = 36;
    KingMoves[ 35 ][ 5 ] = 42;
    KingMoves[ 35 ][ 6 ] = 43;
    KingMoves[ 35 ][ 7 ] = 44;
    KingMoves[ 36 ] = new int[ 8 ];
    KingMoves[ 36 ][ 0 ] = 27;
    KingMoves[ 36 ][ 1 ] = 28;
    KingMoves[ 36 ][ 2 ] = 29;
    KingMoves[ 36 ][ 3 ] = 35;
    KingMoves[ 36 ][ 4 ] = 37;
    KingMoves[ 36 ][ 5 ] = 43;
    KingMoves[ 36 ][ 6 ] = 44;
    KingMoves[ 36 ][ 7 ] = 45;
    KingMoves[ 37 ] = new int[ 8 ];
    KingMoves[ 37 ][ 0 ] = 28;
    KingMoves[ 37 ][ 1 ] = 29;
    KingMoves[ 37 ][ 2 ] = 30;
    KingMoves[ 37 ][ 3 ] = 36;
    KingMoves[ 37 ][ 4 ] = 38;
    KingMoves[ 37 ][ 5 ] = 44;
    KingMoves[ 37 ][ 6 ] = 45;
    KingMoves[ 37 ][ 7 ] = 46;
    KingMoves[ 38 ] = new int[ 8 ];
    KingMoves[ 38 ][ 0 ] = 29;
    KingMoves[ 38 ][ 1 ] = 30;
    KingMoves[ 38 ][ 2 ] = 31;
    KingMoves[ 38 ][ 3 ] = 37;
    KingMoves[ 38 ][ 4 ] = 39;
    KingMoves[ 38 ][ 5 ] = 45;
    KingMoves[ 38 ][ 6 ] = 46;
    KingMoves[ 38 ][ 7 ] = 47;
    KingMoves[ 39 ] = new int[ 5 ];
    KingMoves[ 39 ][ 0 ] = 30;
    KingMoves[ 39 ][ 1 ] = 31;
    KingMoves[ 39 ][ 2 ] = 38;
    KingMoves[ 39 ][ 3 ] = 46;
    KingMoves[ 39 ][ 4 ] = 47;

    KingMoves[ 40 ] = new int[ 5 ];
    KingMoves[ 40 ][ 0 ] = 32;
    KingMoves[ 40 ][ 1 ] = 33;
    KingMoves[ 40 ][ 2 ] = 41;
    KingMoves[ 40 ][ 3 ] = 48;
    KingMoves[ 40 ][ 4 ] = 49;
    KingMoves[ 41 ] = new int[ 8 ];
    KingMoves[ 41 ][ 0 ] = 32;
    KingMoves[ 41 ][ 1 ] = 33;
    KingMoves[ 41 ][ 2 ] = 34;
    KingMoves[ 41 ][ 3 ] = 40;
    KingMoves[ 41 ][ 4 ] = 42;
    KingMoves[ 41 ][ 5 ] = 48;
    KingMoves[ 41 ][ 6 ] = 49;
    KingMoves[ 41 ][ 7 ] = 50;
    KingMoves[ 42 ] = new int[ 8 ];
    KingMoves[ 42 ][ 0 ] = 33;
    KingMoves[ 42 ][ 1 ] = 34;
    KingMoves[ 42 ][ 2 ] = 35;
    KingMoves[ 42 ][ 3 ] = 41;
    KingMoves[ 42 ][ 4 ] = 43;
    KingMoves[ 42 ][ 5 ] = 49;
    KingMoves[ 42 ][ 6 ] = 50;
    KingMoves[ 42 ][ 7 ] = 51;
    KingMoves[ 43 ] = new int[ 8 ];
    KingMoves[ 43 ][ 0 ] = 34;
    KingMoves[ 43 ][ 1 ] = 35;
    KingMoves[ 43 ][ 2 ] = 36;
    KingMoves[ 43 ][ 3 ] = 42;
    KingMoves[ 43 ][ 4 ] = 44;
    KingMoves[ 43 ][ 5 ] = 50;
    KingMoves[ 43 ][ 6 ] = 51;
    KingMoves[ 43 ][ 7 ] = 52;
    KingMoves[ 44 ] = new int[ 8 ];
    KingMoves[ 44 ][ 0 ] = 35;
    KingMoves[ 44 ][ 1 ] = 36;
    KingMoves[ 44 ][ 2 ] = 37;
    KingMoves[ 44 ][ 3 ] = 43;
    KingMoves[ 44 ][ 4 ] = 45;
    KingMoves[ 44 ][ 5 ] = 51;
    KingMoves[ 44 ][ 6 ] = 52;
    KingMoves[ 44 ][ 7 ] = 53;
    KingMoves[ 45 ] = new int[ 8 ];
    KingMoves[ 45 ][ 0 ] = 36;
    KingMoves[ 45 ][ 1 ] = 37;
    KingMoves[ 45 ][ 2 ] = 38;
    KingMoves[ 45 ][ 3 ] = 44;
    KingMoves[ 45 ][ 4 ] = 46;
    KingMoves[ 45 ][ 5 ] = 52;
    KingMoves[ 45 ][ 6 ] = 53;
    KingMoves[ 45 ][ 7 ] = 54;
    KingMoves[ 46 ] = new int[ 8 ];
    KingMoves[ 46 ][ 0 ] = 37;
    KingMoves[ 46 ][ 1 ] = 38;
    KingMoves[ 46 ][ 2 ] = 39;
    KingMoves[ 46 ][ 3 ] = 45;
    KingMoves[ 46 ][ 4 ] = 47;
    KingMoves[ 46 ][ 5 ] = 53;
    KingMoves[ 46 ][ 6 ] = 54;
    KingMoves[ 46 ][ 7 ] = 55;
    KingMoves[ 47 ] = new int[ 5 ];
    KingMoves[ 47 ][ 0 ] = 38;
    KingMoves[ 47 ][ 1 ] = 39;
    KingMoves[ 47 ][ 2 ] = 46;
    KingMoves[ 47 ][ 3 ] = 54;
    KingMoves[ 47 ][ 4 ] = 55;

    KingMoves[ 48 ] = new int[ 5 ];
    KingMoves[ 48 ][ 0 ] = 40;
    KingMoves[ 48 ][ 1 ] = 41;
    KingMoves[ 48 ][ 2 ] = 49;
    KingMoves[ 48 ][ 3 ] = 56;
    KingMoves[ 48 ][ 4 ] = 57;
    KingMoves[ 49 ] = new int[ 8 ];
    KingMoves[ 49 ][ 0 ] = 40;
    KingMoves[ 49 ][ 1 ] = 41;
    KingMoves[ 49 ][ 2 ] = 42;
    KingMoves[ 49 ][ 3 ] = 48;
    KingMoves[ 49 ][ 4 ] = 50;
    KingMoves[ 49 ][ 5 ] = 56;
    KingMoves[ 49 ][ 6 ] = 57;
    KingMoves[ 49 ][ 7 ] = 58;
    KingMoves[ 50 ] = new int[ 8 ];
    KingMoves[ 50 ][ 0 ] = 41;
    KingMoves[ 50 ][ 1 ] = 42;
    KingMoves[ 50 ][ 2 ] = 43;
    KingMoves[ 50 ][ 3 ] = 49;
    KingMoves[ 50 ][ 4 ] = 51;
    KingMoves[ 50 ][ 5 ] = 57;
    KingMoves[ 50 ][ 6 ] = 58;
    KingMoves[ 50 ][ 7 ] = 59;
    KingMoves[ 51 ] = new int[ 8 ];
    KingMoves[ 51 ][ 0 ] = 42;
    KingMoves[ 51 ][ 1 ] = 43;
    KingMoves[ 51 ][ 2 ] = 44;
    KingMoves[ 51 ][ 3 ] = 50;
    KingMoves[ 51 ][ 4 ] = 52;
    KingMoves[ 51 ][ 5 ] = 58;
    KingMoves[ 51 ][ 6 ] = 59;
    KingMoves[ 51 ][ 7 ] = 60;
    KingMoves[ 52 ] = new int[ 8 ];
    KingMoves[ 52 ][ 0 ] = 43;
    KingMoves[ 52 ][ 1 ] = 44;
    KingMoves[ 52 ][ 2 ] = 45;
    KingMoves[ 52 ][ 3 ] = 51;
    KingMoves[ 52 ][ 4 ] = 53;
    KingMoves[ 52 ][ 5 ] = 59;
    KingMoves[ 52 ][ 6 ] = 60;
    KingMoves[ 52 ][ 7 ] = 61;
    KingMoves[ 53 ] = new int[ 8 ];
    KingMoves[ 53 ][ 0 ] = 44;
    KingMoves[ 53 ][ 1 ] = 45;
    KingMoves[ 53 ][ 2 ] = 46;
    KingMoves[ 53 ][ 3 ] = 52;
    KingMoves[ 53 ][ 4 ] = 54;
    KingMoves[ 53 ][ 5 ] = 60;
    KingMoves[ 53 ][ 6 ] = 61;
    KingMoves[ 53 ][ 7 ] = 62;
    KingMoves[ 54 ] = new int[ 8 ];
    KingMoves[ 54 ][ 0 ] = 45;
    KingMoves[ 54 ][ 1 ] = 46;
    KingMoves[ 54 ][ 2 ] = 47;
    KingMoves[ 54 ][ 3 ] = 53;
    KingMoves[ 54 ][ 4 ] = 55;
    KingMoves[ 54 ][ 5 ] = 61;
    KingMoves[ 54 ][ 6 ] = 62;
    KingMoves[ 54 ][ 7 ] = 63;
    KingMoves[ 55 ] = new int[ 5 ];
    KingMoves[ 55 ][ 0 ] = 46;
    KingMoves[ 55 ][ 1 ] = 47;
    KingMoves[ 55 ][ 2 ] = 54;
    KingMoves[ 55 ][ 3 ] = 62;
    KingMoves[ 55 ][ 4 ] = 63;

    KingMoves[ 56 ] = new int[ 3 ];
    KingMoves[ 56 ][ 0 ] = 48;
    KingMoves[ 56 ][ 1 ] = 49;
    KingMoves[ 56 ][ 2 ] = 57;
    KingMoves[ 57 ] = new int[ 5 ];
    KingMoves[ 57 ][ 0 ] = 48;
    KingMoves[ 57 ][ 1 ] = 49;
    KingMoves[ 57 ][ 2 ] = 50;
    KingMoves[ 57 ][ 3 ] = 56;
    KingMoves[ 57 ][ 4 ] = 58;
    KingMoves[ 58 ] = new int[ 5 ];
    KingMoves[ 58 ][ 0 ] = 49;
    KingMoves[ 58 ][ 1 ] = 50;
    KingMoves[ 58 ][ 2 ] = 51;
    KingMoves[ 58 ][ 3 ] = 57;
    KingMoves[ 58 ][ 4 ] = 59;
    KingMoves[ 59 ] = new int[ 5 ];
    KingMoves[ 59 ][ 0 ] = 50;
    KingMoves[ 59 ][ 1 ] = 51;
    KingMoves[ 59 ][ 2 ] = 52;
    KingMoves[ 59 ][ 3 ] = 58;
    KingMoves[ 59 ][ 4 ] = 60;
    KingMoves[ 60 ] = new int[ 5 ];
    KingMoves[ 60 ][ 0 ] = 51;
    KingMoves[ 60 ][ 1 ] = 52;
    KingMoves[ 60 ][ 2 ] = 53;
    KingMoves[ 60 ][ 3 ] = 59;
    KingMoves[ 60 ][ 4 ] = 61;
    KingMoves[ 61 ] = new int[ 5 ];
    KingMoves[ 61 ][ 0 ] = 52;
    KingMoves[ 61 ][ 1 ] = 53;
    KingMoves[ 61 ][ 2 ] = 54;
    KingMoves[ 61 ][ 3 ] = 60;
    KingMoves[ 61 ][ 4 ] = 62;
    KingMoves[ 62 ] = new int[ 5 ];
    KingMoves[ 62 ][ 0 ] = 53;
    KingMoves[ 62 ][ 1 ] = 54;
    KingMoves[ 62 ][ 2 ] = 55;
    KingMoves[ 62 ][ 3 ] = 61;
    KingMoves[ 62 ][ 4 ] = 63;
    KingMoves[ 63 ] = new int[ 3 ];
    KingMoves[ 63 ][ 0 ] = 54;
    KingMoves[ 63 ][ 1 ] = 55;
    KingMoves[ 63 ][ 2 ] = 62;

    BishopMoves = new int[ 64 ][][];
    BishopMoves[ 0 ] = new int [ 1 ][ 7 ];
    BishopMoves[ 0 ][ 0 ][ 0 ] = 9;
    BishopMoves[ 0 ][ 0 ][ 1 ] = 18;
    BishopMoves[ 0 ][ 0 ][ 2 ] = 27;
    BishopMoves[ 0 ][ 0 ][ 3 ] = 36;
    BishopMoves[ 0 ][ 0 ][ 4 ] = 45;
    BishopMoves[ 0 ][ 0 ][ 5 ] = 54;
    BishopMoves[ 0 ][ 0 ][ 6 ] = 63;

    BishopMoves[ 1 ] = new int[ 2 ][];
    BishopMoves[ 1 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 1 ][ 0 ][ 0 ] = 8;
    BishopMoves[ 1 ][ 1 ] = new int[ 6 ];
    BishopMoves[ 1 ][ 1 ][ 0 ] = 10;
    BishopMoves[ 1 ][ 1 ][ 1 ] = 19;
    BishopMoves[ 1 ][ 1 ][ 2 ] = 28;
    BishopMoves[ 1 ][ 1 ][ 3 ] = 37;
    BishopMoves[ 1 ][ 1 ][ 4 ] = 46;
    BishopMoves[ 1 ][ 1 ][ 5 ] = 55;

    BishopMoves[ 2 ] = new int[ 2 ][];
    BishopMoves[ 2 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 2 ][ 0 ][ 0 ] = 9;
    BishopMoves[ 2 ][ 0 ][ 1 ] = 16;
    BishopMoves[ 2 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 2 ][ 1 ][ 0 ] = 11;
    BishopMoves[ 2 ][ 1 ][ 1 ] = 20;
    BishopMoves[ 2 ][ 1 ][ 2 ] = 29;
    BishopMoves[ 2 ][ 1 ][ 3 ] = 38;
    BishopMoves[ 2 ][ 1 ][ 4 ] = 47;

    BishopMoves[ 3 ] = new int[ 2 ][];
    BishopMoves[ 3 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 3 ][ 0 ][ 0 ] = 10;
    BishopMoves[ 3 ][ 0 ][ 1 ] = 17;
    BishopMoves[ 3 ][ 0 ][ 2 ] = 24;
    BishopMoves[ 3 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 3 ][ 1 ][ 0 ] = 12;
    BishopMoves[ 3 ][ 1 ][ 1 ] = 21;
    BishopMoves[ 3 ][ 1 ][ 2 ] = 30;
    BishopMoves[ 3 ][ 1 ][ 3 ] = 39;

    BishopMoves[ 4 ] = new int[ 2 ][];
    BishopMoves[ 4 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 4 ][ 0 ][ 0 ] = 11;
    BishopMoves[ 4 ][ 0 ][ 1 ] = 18;
    BishopMoves[ 4 ][ 0 ][ 2 ] = 25;
    BishopMoves[ 4 ][ 0 ][ 3 ] = 32;
    BishopMoves[ 4 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 4 ][ 1 ][ 0 ] = 13;
    BishopMoves[ 4 ][ 1 ][ 1 ] = 22;
    BishopMoves[ 4 ][ 1 ][ 2 ] = 31;

    BishopMoves[ 5 ] = new int[ 2 ][];
    BishopMoves[ 5 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 5 ][ 0 ][ 0 ] = 12;
    BishopMoves[ 5 ][ 0 ][ 1 ] = 19;
    BishopMoves[ 5 ][ 0 ][ 2 ] = 26;
    BishopMoves[ 5 ][ 0 ][ 3 ] = 33;
    BishopMoves[ 5 ][ 0 ][ 4 ] = 40;
    BishopMoves[ 5 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 5 ][ 1 ][ 0 ] = 14;
    BishopMoves[ 5 ][ 1 ][ 1 ] = 23;

    BishopMoves[ 6 ] = new int[ 2 ][];
    BishopMoves[ 6 ][ 0 ] = new int[ 6 ];
    BishopMoves[ 6 ][ 0 ][ 0 ] = 13;
    BishopMoves[ 6 ][ 0 ][ 1 ] = 20;
    BishopMoves[ 6 ][ 0 ][ 2 ] = 27;
    BishopMoves[ 6 ][ 0 ][ 3 ] = 34;
    BishopMoves[ 6 ][ 0 ][ 4 ] = 41;
    BishopMoves[ 6 ][ 0 ][ 5 ] = 48;
    BishopMoves[ 6 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 6 ][ 1 ][ 0 ] = 15;

    BishopMoves[ 7 ] = new int [ 1 ][ 7 ];
    BishopMoves[ 7 ][ 0 ][ 0 ] = 14;
    BishopMoves[ 7 ][ 0 ][ 1 ] = 21;
    BishopMoves[ 7 ][ 0 ][ 2 ] = 28;
    BishopMoves[ 7 ][ 0 ][ 3 ] = 35;
    BishopMoves[ 7 ][ 0 ][ 4 ] = 42;
    BishopMoves[ 7 ][ 0 ][ 5 ] = 49;
    BishopMoves[ 7 ][ 0 ][ 6 ] = 56;

    BishopMoves[ 8 ] = new int[ 2 ][];
    BishopMoves[ 8 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 8 ][ 0 ][ 0 ] = 1;
    BishopMoves[ 8 ][ 1 ] = new int[ 6 ];
    BishopMoves[ 8 ][ 1 ][ 0 ] = 17;
    BishopMoves[ 8 ][ 1 ][ 1 ] = 26;
    BishopMoves[ 8 ][ 1 ][ 2 ] = 35;
    BishopMoves[ 8 ][ 1 ][ 3 ] = 44;
    BishopMoves[ 8 ][ 1 ][ 4 ] = 53;
    BishopMoves[ 8 ][ 1 ][ 5 ] = 62;

    BishopMoves[ 9 ] = new int[ 4 ][];
    BishopMoves[ 9 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 9 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 9 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 9 ][ 3 ] = new int[ 6 ];
    BishopMoves[ 9 ][ 0 ][ 0 ] = 0;
    BishopMoves[ 9 ][ 1 ][ 0 ] = 2;
    BishopMoves[ 9 ][ 2 ][ 0 ] = 16;
    BishopMoves[ 9 ][ 3 ][ 0 ] = 18;
    BishopMoves[ 9 ][ 3 ][ 1 ] = 27;
    BishopMoves[ 9 ][ 3 ][ 2 ] = 36;
    BishopMoves[ 9 ][ 3 ][ 3 ] = 45;
    BishopMoves[ 9 ][ 3 ][ 4 ] = 54;
    BishopMoves[ 9 ][ 3 ][ 5 ] = 63;

    BishopMoves[ 10 ] = new int[ 4 ][];
    BishopMoves[ 10 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 10 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 10 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 10 ][ 3 ] = new int[ 5 ];
    BishopMoves[ 10 ][ 0 ][ 0 ] = 1;
    BishopMoves[ 10 ][ 1 ][ 0 ] = 3;
    BishopMoves[ 10 ][ 2 ][ 0 ] = 17;
    BishopMoves[ 10 ][ 2 ][ 1 ] = 24;
    BishopMoves[ 10 ][ 3 ][ 0 ] = 19;
    BishopMoves[ 10 ][ 3 ][ 1 ] = 28;
    BishopMoves[ 10 ][ 3 ][ 2 ] = 37;
    BishopMoves[ 10 ][ 3 ][ 3 ] = 46;
    BishopMoves[ 10 ][ 3 ][ 4 ] = 55;

    BishopMoves[ 11 ] = new int[ 4 ][];
    BishopMoves[ 11 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 11 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 11 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 11 ][ 3 ] = new int[ 4 ];
    BishopMoves[ 11 ][ 0 ][ 0 ] = 2;
    BishopMoves[ 11 ][ 1 ][ 0 ] = 4;
    BishopMoves[ 11 ][ 2 ][ 0 ] = 18;
    BishopMoves[ 11 ][ 2 ][ 1 ] = 25;
    BishopMoves[ 11 ][ 2 ][ 2 ] = 32;
    BishopMoves[ 11 ][ 3 ][ 0 ] = 20;
    BishopMoves[ 11 ][ 3 ][ 1 ] = 29;
    BishopMoves[ 11 ][ 3 ][ 2 ] = 38;
    BishopMoves[ 11 ][ 3 ][ 3 ] = 47;

    BishopMoves[ 12 ] = new int[ 4 ][];
    BishopMoves[ 12 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 12 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 12 ][ 2 ] = new int[ 4 ];
    BishopMoves[ 12 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 12 ][ 0 ][ 0 ] = 3;
    BishopMoves[ 12 ][ 1 ][ 0 ] = 5;
    BishopMoves[ 12 ][ 2 ][ 0 ] = 19;
    BishopMoves[ 12 ][ 2 ][ 1 ] = 26;
    BishopMoves[ 12 ][ 2 ][ 2 ] = 33;
    BishopMoves[ 12 ][ 2 ][ 3 ] = 40;
    BishopMoves[ 12 ][ 3 ][ 0 ] = 21;
    BishopMoves[ 12 ][ 3 ][ 1 ] = 30;
    BishopMoves[ 12 ][ 3 ][ 2 ] = 39;

    BishopMoves[ 13 ] = new int[ 4 ][];
    BishopMoves[ 13 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 13 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 13 ][ 2 ] = new int[ 5 ];
    BishopMoves[ 13 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 13 ][ 0 ][ 0 ] = 4;
    BishopMoves[ 13 ][ 1 ][ 0 ] = 6;
    BishopMoves[ 13 ][ 2 ][ 0 ] = 20;
    BishopMoves[ 13 ][ 2 ][ 1 ] = 27;
    BishopMoves[ 13 ][ 2 ][ 2 ] = 34;
    BishopMoves[ 13 ][ 2 ][ 3 ] = 41;
    BishopMoves[ 13 ][ 2 ][ 4 ] = 48;
    BishopMoves[ 13 ][ 3 ][ 0 ] = 22;
    BishopMoves[ 13 ][ 3 ][ 1 ] = 31;

    BishopMoves[ 14 ] = new int[ 4 ][];
    BishopMoves[ 14 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 14 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 14 ][ 2 ] = new int[ 6 ];
    BishopMoves[ 14 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 14 ][ 0 ][ 0 ] = 5;
    BishopMoves[ 14 ][ 1 ][ 0 ] = 7;
    BishopMoves[ 14 ][ 2 ][ 0 ] = 21;
    BishopMoves[ 14 ][ 2 ][ 1 ] = 28;
    BishopMoves[ 14 ][ 2 ][ 2 ] = 35;
    BishopMoves[ 14 ][ 2 ][ 3 ] = 42;
    BishopMoves[ 14 ][ 2 ][ 4 ] = 49;
    BishopMoves[ 14 ][ 2 ][ 5 ] = 56;
    BishopMoves[ 14 ][ 3 ][ 0 ] = 23;

    BishopMoves[ 15 ] = new int[ 2 ][];
    BishopMoves[ 15 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 15 ][ 1 ] = new int[ 6 ];
    BishopMoves[ 15 ][ 0 ][ 0 ] = 6;
    BishopMoves[ 15 ][ 1 ][ 0 ] = 22;
    BishopMoves[ 15 ][ 1 ][ 1 ] = 29;
    BishopMoves[ 15 ][ 1 ][ 2 ] = 36;
    BishopMoves[ 15 ][ 1 ][ 3 ] = 43;
    BishopMoves[ 15 ][ 1 ][ 4 ] = 50;
    BishopMoves[ 15 ][ 1 ][ 5 ] = 57;

    BishopMoves[ 16 ] = new int[ 2 ][];
    BishopMoves[ 16 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 16 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 16 ][ 0 ][ 0 ] = 9;
    BishopMoves[ 16 ][ 0 ][ 1 ] = 2;
    BishopMoves[ 16 ][ 1 ][ 0 ] = 25;
    BishopMoves[ 16 ][ 1 ][ 1 ] = 34;
    BishopMoves[ 16 ][ 1 ][ 2 ] = 43;
    BishopMoves[ 16 ][ 1 ][ 3 ] = 52;
    BishopMoves[ 16 ][ 1 ][ 4 ] = 61;

    BishopMoves[ 17 ] = new int[ 4 ][];
    BishopMoves[ 17 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 17 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 17 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 17 ][ 3 ] = new int[ 5 ];
    BishopMoves[ 17 ][ 0 ][ 0 ] = 8;
    BishopMoves[ 17 ][ 1 ][ 0 ] = 10;
    BishopMoves[ 17 ][ 1 ][ 1 ] = 3;
    BishopMoves[ 17 ][ 2 ][ 0 ] = 24;
    BishopMoves[ 17 ][ 3 ][ 0 ] = 26;
    BishopMoves[ 17 ][ 3 ][ 1 ] = 35;
    BishopMoves[ 17 ][ 3 ][ 2 ] = 44;
    BishopMoves[ 17 ][ 3 ][ 3 ] = 53;
    BishopMoves[ 17 ][ 3 ][ 4 ] = 62;

    BishopMoves[ 18 ] = new int[ 4 ][];
    BishopMoves[ 18 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 18 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 18 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 18 ][ 3 ] = new int[ 5 ];
    BishopMoves[ 18 ][ 0 ][ 0 ] = 9;
    BishopMoves[ 18 ][ 0 ][ 1 ] = 0;
    BishopMoves[ 18 ][ 1 ][ 0 ] = 11;
    BishopMoves[ 18 ][ 1 ][ 1 ] = 4;
    BishopMoves[ 18 ][ 2 ][ 0 ] = 25;
    BishopMoves[ 18 ][ 2 ][ 1 ] = 32;
    BishopMoves[ 18 ][ 3 ][ 0 ] = 27;
    BishopMoves[ 18 ][ 3 ][ 1 ] = 36;
    BishopMoves[ 18 ][ 3 ][ 2 ] = 45;
    BishopMoves[ 18 ][ 3 ][ 3 ] = 54;
    BishopMoves[ 18 ][ 3 ][ 4 ] = 63;

    BishopMoves[ 19 ] = new int[ 4 ][];
    BishopMoves[ 19 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 19 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 19 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 19 ][ 3 ] = new int[ 4 ];
    BishopMoves[ 19 ][ 0 ][ 0 ] = 10;
    BishopMoves[ 19 ][ 0 ][ 1 ] = 1;
    BishopMoves[ 19 ][ 1 ][ 0 ] = 12;
    BishopMoves[ 19 ][ 1 ][ 1 ] = 5;
    BishopMoves[ 19 ][ 2 ][ 0 ] = 26;
    BishopMoves[ 19 ][ 2 ][ 1 ] = 33;
    BishopMoves[ 19 ][ 2 ][ 2 ] = 40;
    BishopMoves[ 19 ][ 3 ][ 0 ] = 28;
    BishopMoves[ 19 ][ 3 ][ 1 ] = 37;
    BishopMoves[ 19 ][ 3 ][ 2 ] = 46;
    BishopMoves[ 19 ][ 3 ][ 3 ] = 55;

    BishopMoves[ 20 ] = new int[ 4 ][];
    BishopMoves[ 20 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 20 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 20 ][ 2 ] = new int[ 4 ];
    BishopMoves[ 20 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 20 ][ 0 ][ 0 ] = 11;
    BishopMoves[ 20 ][ 0 ][ 1 ] = 2;
    BishopMoves[ 20 ][ 1 ][ 0 ] = 13;
    BishopMoves[ 20 ][ 1 ][ 1 ] = 6;
    BishopMoves[ 20 ][ 2 ][ 0 ] = 27;
    BishopMoves[ 20 ][ 2 ][ 1 ] = 34;
    BishopMoves[ 20 ][ 2 ][ 2 ] = 41;
    BishopMoves[ 20 ][ 2 ][ 3 ] = 48;
    BishopMoves[ 20 ][ 3 ][ 0 ] = 29;
    BishopMoves[ 20 ][ 3 ][ 1 ] = 38;
    BishopMoves[ 20 ][ 3 ][ 2 ] = 47;

    BishopMoves[ 21 ] = new int[ 4 ][];
    BishopMoves[ 21 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 21 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 21 ][ 2 ] = new int[ 5 ];
    BishopMoves[ 21 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 21 ][ 0 ][ 0 ] = 12;
    BishopMoves[ 21 ][ 0 ][ 1 ] = 3;
    BishopMoves[ 21 ][ 1 ][ 0 ] = 14;
    BishopMoves[ 21 ][ 1 ][ 1 ] = 7;
    BishopMoves[ 21 ][ 2 ][ 0 ] = 28;
    BishopMoves[ 21 ][ 2 ][ 1 ] = 35;
    BishopMoves[ 21 ][ 2 ][ 2 ] = 42;
    BishopMoves[ 21 ][ 2 ][ 3 ] = 49;
    BishopMoves[ 21 ][ 2 ][ 4 ] = 56;
    BishopMoves[ 21 ][ 3 ][ 0 ] = 30;
    BishopMoves[ 21 ][ 3 ][ 1 ] = 39;

    BishopMoves[ 22 ] = new int[ 4 ][];
    BishopMoves[ 22 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 22 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 22 ][ 2 ] = new int[ 5 ];
    BishopMoves[ 22 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 22 ][ 0 ][ 0 ] = 13;
    BishopMoves[ 22 ][ 0 ][ 1 ] = 4;
    BishopMoves[ 22 ][ 1 ][ 0 ] = 15;
    BishopMoves[ 22 ][ 2 ][ 0 ] = 29;
    BishopMoves[ 22 ][ 2 ][ 1 ] = 36;
    BishopMoves[ 22 ][ 2 ][ 2 ] = 43;
    BishopMoves[ 22 ][ 2 ][ 3 ] = 50;
    BishopMoves[ 22 ][ 2 ][ 4 ] = 57;
    BishopMoves[ 22 ][ 3 ][ 0 ] = 31;

    BishopMoves[ 23 ] = new int[ 2 ][];
    BishopMoves[ 23 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 23 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 23 ][ 0 ][ 0 ] = 14;
    BishopMoves[ 23 ][ 0 ][ 1 ] = 5;
    BishopMoves[ 23 ][ 1 ][ 0 ] = 30;
    BishopMoves[ 23 ][ 1 ][ 1 ] = 37;
    BishopMoves[ 23 ][ 1 ][ 2 ] = 44;
    BishopMoves[ 23 ][ 1 ][ 3 ] = 51;
    BishopMoves[ 23 ][ 1 ][ 4 ] = 58;

    BishopMoves[ 24 ] = new int[ 2 ][];
    BishopMoves[ 24 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 24 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 24 ][ 0 ][ 0 ] = 17;
    BishopMoves[ 24 ][ 0 ][ 1 ] = 10;
    BishopMoves[ 24 ][ 0 ][ 2 ] = 3;
    BishopMoves[ 24 ][ 1 ][ 0 ] = 33;
    BishopMoves[ 24 ][ 1 ][ 1 ] = 42;
    BishopMoves[ 24 ][ 1 ][ 2 ] = 51;
    BishopMoves[ 24 ][ 1 ][ 3 ] = 60;

    BishopMoves[ 25 ] = new int[ 4 ][];
    BishopMoves[ 25 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 25 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 25 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 25 ][ 3 ] = new int[ 4 ];
    BishopMoves[ 25 ][ 0 ][ 0 ] = 16;
    BishopMoves[ 25 ][ 1 ][ 0 ] = 18;
    BishopMoves[ 25 ][ 1 ][ 1 ] = 11;
    BishopMoves[ 25 ][ 1 ][ 2 ] = 4;
    BishopMoves[ 25 ][ 2 ][ 0 ] = 32;
    BishopMoves[ 25 ][ 3 ][ 0 ] = 34;
    BishopMoves[ 25 ][ 3 ][ 1 ] = 43;
    BishopMoves[ 25 ][ 3 ][ 2 ] = 52;
    BishopMoves[ 25 ][ 3 ][ 3 ] = 61;

    BishopMoves[ 26 ] = new int[ 4 ][];
    BishopMoves[ 26 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 26 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 26 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 26 ][ 3 ] = new int[ 4 ];
    BishopMoves[ 26 ][ 0 ][ 0 ] = 17;
    BishopMoves[ 26 ][ 0 ][ 1 ] = 8;
    BishopMoves[ 26 ][ 1 ][ 0 ] = 19;
    BishopMoves[ 26 ][ 1 ][ 1 ] = 12;
    BishopMoves[ 26 ][ 1 ][ 2 ] = 5;
    BishopMoves[ 26 ][ 2 ][ 0 ] = 33;
    BishopMoves[ 26 ][ 2 ][ 1 ] = 40;
    BishopMoves[ 26 ][ 3 ][ 0 ] = 35;
    BishopMoves[ 26 ][ 3 ][ 1 ] = 44;
    BishopMoves[ 26 ][ 3 ][ 2 ] = 53;
    BishopMoves[ 26 ][ 3 ][ 3 ] = 62;

    BishopMoves[ 27 ] = new int[ 4 ][];
    BishopMoves[ 27 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 27 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 27 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 27 ][ 3 ] = new int[ 4 ];
    BishopMoves[ 27 ][ 0 ][ 0 ] = 18;
    BishopMoves[ 27 ][ 0 ][ 1 ] = 9;
    BishopMoves[ 27 ][ 0 ][ 2 ] = 0;
    BishopMoves[ 27 ][ 1 ][ 0 ] = 20;
    BishopMoves[ 27 ][ 1 ][ 1 ] = 13;
    BishopMoves[ 27 ][ 1 ][ 2 ] = 6;
    BishopMoves[ 27 ][ 2 ][ 0 ] = 34;
    BishopMoves[ 27 ][ 2 ][ 1 ] = 41;
    BishopMoves[ 27 ][ 2 ][ 2 ] = 48;
    BishopMoves[ 27 ][ 3 ][ 0 ] = 36;
    BishopMoves[ 27 ][ 3 ][ 1 ] = 45;
    BishopMoves[ 27 ][ 3 ][ 2 ] = 54;
    BishopMoves[ 27 ][ 3 ][ 3 ] = 63;

    BishopMoves[ 28 ] = new int[ 4 ][];
    BishopMoves[ 28 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 28 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 28 ][ 2 ] = new int[ 4 ];
    BishopMoves[ 28 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 28 ][ 0 ][ 0 ] = 19;
    BishopMoves[ 28 ][ 0 ][ 1 ] = 10;
    BishopMoves[ 28 ][ 0 ][ 2 ] = 1;
    BishopMoves[ 28 ][ 1 ][ 0 ] = 21;
    BishopMoves[ 28 ][ 1 ][ 1 ] = 14;
    BishopMoves[ 28 ][ 1 ][ 2 ] = 7;
    BishopMoves[ 28 ][ 2 ][ 0 ] = 35;
    BishopMoves[ 28 ][ 2 ][ 1 ] = 42;
    BishopMoves[ 28 ][ 2 ][ 2 ] = 49;
    BishopMoves[ 28 ][ 2 ][ 3 ] = 56;
    BishopMoves[ 28 ][ 3 ][ 0 ] = 37;
    BishopMoves[ 28 ][ 3 ][ 1 ] = 46;
    BishopMoves[ 28 ][ 3 ][ 2 ] = 55;

    BishopMoves[ 29 ] = new int[ 4 ][];
    BishopMoves[ 29 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 29 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 29 ][ 2 ] = new int[ 4 ];
    BishopMoves[ 29 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 29 ][ 0 ][ 0 ] = 20;
    BishopMoves[ 29 ][ 0 ][ 1 ] = 11;
    BishopMoves[ 29 ][ 0 ][ 2 ] = 2;
    BishopMoves[ 29 ][ 1 ][ 0 ] = 22;
    BishopMoves[ 29 ][ 1 ][ 1 ] = 15;
    BishopMoves[ 29 ][ 2 ][ 0 ] = 36;
    BishopMoves[ 29 ][ 2 ][ 1 ] = 43;
    BishopMoves[ 29 ][ 2 ][ 2 ] = 50;
    BishopMoves[ 29 ][ 2 ][ 3 ] = 57;
    BishopMoves[ 29 ][ 3 ][ 0 ] = 38;
    BishopMoves[ 29 ][ 3 ][ 1 ] = 47;

    BishopMoves[ 30 ] = new int[ 4 ][];
    BishopMoves[ 30 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 30 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 30 ][ 2 ] = new int[ 4 ];
    BishopMoves[ 30 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 30 ][ 0 ][ 0 ] = 21;
    BishopMoves[ 30 ][ 0 ][ 1 ] = 12;
    BishopMoves[ 30 ][ 0 ][ 2 ] = 3;
    BishopMoves[ 30 ][ 1 ][ 0 ] = 23;
    BishopMoves[ 30 ][ 2 ][ 0 ] = 37;
    BishopMoves[ 30 ][ 2 ][ 1 ] = 44;
    BishopMoves[ 30 ][ 2 ][ 2 ] = 51;
    BishopMoves[ 30 ][ 2 ][ 3 ] = 58;
    BishopMoves[ 30 ][ 3 ][ 0 ] = 39;

    BishopMoves[ 31 ] = new int[ 2 ][];
    BishopMoves[ 31 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 31 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 31 ][ 0 ][ 0 ] = 22;
    BishopMoves[ 31 ][ 0 ][ 1 ] = 13;
    BishopMoves[ 31 ][ 0 ][ 2 ] = 4;
    BishopMoves[ 31 ][ 1 ][ 0 ] = 38;
    BishopMoves[ 31 ][ 1 ][ 1 ] = 45;
    BishopMoves[ 31 ][ 1 ][ 2 ] = 52;
    BishopMoves[ 31 ][ 1 ][ 3 ] = 59;

    BishopMoves[ 32 ] = new int[ 2 ][];
    BishopMoves[ 32 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 32 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 32 ][ 0 ][ 0 ] = 25;
    BishopMoves[ 32 ][ 0 ][ 1 ] = 18;
    BishopMoves[ 32 ][ 0 ][ 2 ] = 11;
    BishopMoves[ 32 ][ 0 ][ 3 ] = 4;
    BishopMoves[ 32 ][ 1 ][ 0 ] = 41;
    BishopMoves[ 32 ][ 1 ][ 1 ] = 50;
    BishopMoves[ 32 ][ 1 ][ 2 ] = 59;

    BishopMoves[ 33 ] = new int[ 4 ][];
    BishopMoves[ 33 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 33 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 33 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 33 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 33 ][ 0 ][ 0 ] = 24;
    BishopMoves[ 33 ][ 1 ][ 0 ] = 26;
    BishopMoves[ 33 ][ 1 ][ 1 ] = 19;
    BishopMoves[ 33 ][ 1 ][ 2 ] = 12;
    BishopMoves[ 33 ][ 1 ][ 3 ] = 5;
    BishopMoves[ 33 ][ 2 ][ 0 ] = 40;
    BishopMoves[ 33 ][ 3 ][ 0 ] = 42;
    BishopMoves[ 33 ][ 3 ][ 1 ] = 51;
    BishopMoves[ 33 ][ 3 ][ 2 ] = 60;

    BishopMoves[ 34 ] = new int[ 4 ][];
    BishopMoves[ 34 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 34 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 34 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 34 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 34 ][ 0 ][ 0 ] = 25;
    BishopMoves[ 34 ][ 0 ][ 1 ] = 16;
    BishopMoves[ 34 ][ 1 ][ 0 ] = 27;
    BishopMoves[ 34 ][ 1 ][ 1 ] = 20;
    BishopMoves[ 34 ][ 1 ][ 2 ] = 13;
    BishopMoves[ 34 ][ 1 ][ 3 ] = 6;
    BishopMoves[ 34 ][ 2 ][ 0 ] = 41;
    BishopMoves[ 34 ][ 2 ][ 1 ] = 48;
    BishopMoves[ 34 ][ 3 ][ 0 ] = 43;
    BishopMoves[ 34 ][ 3 ][ 1 ] = 52;
    BishopMoves[ 34 ][ 3 ][ 2 ] = 61;

    BishopMoves[ 35 ] = new int[ 4 ][];
    BishopMoves[ 35 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 35 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 35 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 35 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 35 ][ 0 ][ 0 ] = 26;
    BishopMoves[ 35 ][ 0 ][ 1 ] = 17;
    BishopMoves[ 35 ][ 0 ][ 2 ] = 8;
    BishopMoves[ 35 ][ 1 ][ 0 ] = 28;
    BishopMoves[ 35 ][ 1 ][ 1 ] = 21;
    BishopMoves[ 35 ][ 1 ][ 2 ] = 14;
    BishopMoves[ 35 ][ 1 ][ 3 ] = 7;
    BishopMoves[ 35 ][ 2 ][ 0 ] = 42;
    BishopMoves[ 35 ][ 2 ][ 1 ] = 49;
    BishopMoves[ 35 ][ 2 ][ 2 ] = 56;
    BishopMoves[ 35 ][ 3 ][ 0 ] = 44;
    BishopMoves[ 35 ][ 3 ][ 1 ] = 53;
    BishopMoves[ 35 ][ 3 ][ 2 ] = 62;

    BishopMoves[ 36 ] = new int[ 4 ][];
    BishopMoves[ 36 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 36 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 36 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 36 ][ 3 ] = new int[ 3 ];
    BishopMoves[ 36 ][ 0 ][ 0 ] = 27;
    BishopMoves[ 36 ][ 0 ][ 1 ] = 18;
    BishopMoves[ 36 ][ 0 ][ 2 ] = 9;
    BishopMoves[ 36 ][ 0 ][ 3 ] = 0;
    BishopMoves[ 36 ][ 1 ][ 0 ] = 29;
    BishopMoves[ 36 ][ 1 ][ 1 ] = 22;
    BishopMoves[ 36 ][ 1 ][ 2 ] = 15;
    BishopMoves[ 36 ][ 2 ][ 0 ] = 43;
    BishopMoves[ 36 ][ 2 ][ 1 ] = 50;
    BishopMoves[ 36 ][ 2 ][ 2 ] = 57;
    BishopMoves[ 36 ][ 3 ][ 0 ] = 45;
    BishopMoves[ 36 ][ 3 ][ 1 ] = 54;
    BishopMoves[ 36 ][ 3 ][ 2 ] = 63;

    BishopMoves[ 37 ] = new int[ 4 ][];
    BishopMoves[ 37 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 37 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 37 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 37 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 37 ][ 0 ][ 0 ] = 28;
    BishopMoves[ 37 ][ 0 ][ 1 ] = 19;
    BishopMoves[ 37 ][ 0 ][ 2 ] = 10;
    BishopMoves[ 37 ][ 0 ][ 3 ] = 1;
    BishopMoves[ 37 ][ 1 ][ 0 ] = 30;
    BishopMoves[ 37 ][ 1 ][ 1 ] = 23;
    BishopMoves[ 37 ][ 2 ][ 0 ] = 44;
    BishopMoves[ 37 ][ 2 ][ 1 ] = 51;
    BishopMoves[ 37 ][ 2 ][ 2 ] = 58;
    BishopMoves[ 37 ][ 3 ][ 0 ] = 46;
    BishopMoves[ 37 ][ 3 ][ 1 ] = 55;

    BishopMoves[ 38 ] = new int[ 4 ][];
    BishopMoves[ 38 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 38 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 38 ][ 2 ] = new int[ 3 ];
    BishopMoves[ 38 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 38 ][ 0 ][ 0 ] = 29;
    BishopMoves[ 38 ][ 0 ][ 1 ] = 20;
    BishopMoves[ 38 ][ 0 ][ 2 ] = 11;
    BishopMoves[ 38 ][ 0 ][ 3 ] = 2;
    BishopMoves[ 38 ][ 1 ][ 0 ] = 31;
    BishopMoves[ 38 ][ 2 ][ 0 ] = 45;
    BishopMoves[ 38 ][ 2 ][ 1 ] = 52;
    BishopMoves[ 38 ][ 2 ][ 2 ] = 59;
    BishopMoves[ 38 ][ 3 ][ 0 ] = 47;

    BishopMoves[ 39 ] = new int[ 2 ][];
    BishopMoves[ 39 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 39 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 39 ][ 0 ][ 0 ] = 30;
    BishopMoves[ 39 ][ 0 ][ 1 ] = 21;
    BishopMoves[ 39 ][ 0 ][ 2 ] = 12;
    BishopMoves[ 39 ][ 0 ][ 3 ] = 3;
    BishopMoves[ 39 ][ 1 ][ 0 ] = 46;
    BishopMoves[ 39 ][ 1 ][ 1 ] = 53;
    BishopMoves[ 39 ][ 1 ][ 2 ] = 60;

    BishopMoves[ 40 ] = new int[ 2 ][];
    BishopMoves[ 40 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 40 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 40 ][ 0 ][ 0 ] = 33;
    BishopMoves[ 40 ][ 0 ][ 1 ] = 26;
    BishopMoves[ 40 ][ 0 ][ 2 ] = 19;
    BishopMoves[ 40 ][ 0 ][ 3 ] = 12;
    BishopMoves[ 40 ][ 0 ][ 4 ] = 5;
    BishopMoves[ 40 ][ 1 ][ 0 ] = 49;
    BishopMoves[ 40 ][ 1 ][ 1 ] = 58;

    BishopMoves[ 41 ] = new int[ 4 ][];
    BishopMoves[ 41 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 41 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 41 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 41 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 41 ][ 0 ][ 0 ] = 32;
    BishopMoves[ 41 ][ 1 ][ 0 ] = 34;
    BishopMoves[ 41 ][ 1 ][ 1 ] = 27;
    BishopMoves[ 41 ][ 1 ][ 2 ] = 20;
    BishopMoves[ 41 ][ 1 ][ 3 ] = 13;
    BishopMoves[ 41 ][ 1 ][ 4 ] = 6;
    BishopMoves[ 41 ][ 2 ][ 0 ] = 48;
    BishopMoves[ 41 ][ 3 ][ 0 ] = 50;
    BishopMoves[ 41 ][ 3 ][ 1 ] = 59;

    BishopMoves[ 42 ] = new int[ 4 ][];
    BishopMoves[ 42 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 42 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 42 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 42 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 42 ][ 0 ][ 0 ] = 33;
    BishopMoves[ 42 ][ 0 ][ 1 ] = 24;
    BishopMoves[ 42 ][ 1 ][ 0 ] = 35;
    BishopMoves[ 42 ][ 1 ][ 1 ] = 28;
    BishopMoves[ 42 ][ 1 ][ 2 ] = 21;
    BishopMoves[ 42 ][ 1 ][ 3 ] = 14;
    BishopMoves[ 42 ][ 1 ][ 4 ] = 7;
    BishopMoves[ 42 ][ 2 ][ 0 ] = 49;
    BishopMoves[ 42 ][ 2 ][ 1 ] = 56;
    BishopMoves[ 42 ][ 3 ][ 0 ] = 51;
    BishopMoves[ 42 ][ 3 ][ 1 ] = 60;

    BishopMoves[ 43 ] = new int[ 4 ][];
    BishopMoves[ 43 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 43 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 43 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 43 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 43 ][ 0 ][ 0 ] = 34;
    BishopMoves[ 43 ][ 0 ][ 1 ] = 25;
    BishopMoves[ 43 ][ 0 ][ 2 ] = 16;
    BishopMoves[ 43 ][ 1 ][ 0 ] = 36;
    BishopMoves[ 43 ][ 1 ][ 1 ] = 29;
    BishopMoves[ 43 ][ 1 ][ 2 ] = 22;
    BishopMoves[ 43 ][ 1 ][ 3 ] = 15;
    BishopMoves[ 43 ][ 2 ][ 0 ] = 50;
    BishopMoves[ 43 ][ 2 ][ 1 ] = 57;
    BishopMoves[ 43 ][ 3 ][ 0 ] = 52;
    BishopMoves[ 43 ][ 3 ][ 1 ] = 61;

    BishopMoves[ 44 ] = new int[ 4 ][];
    BishopMoves[ 44 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 44 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 44 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 44 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 44 ][ 0 ][ 0 ] = 35;
    BishopMoves[ 44 ][ 0 ][ 1 ] = 26;
    BishopMoves[ 44 ][ 0 ][ 2 ] = 17;
    BishopMoves[ 44 ][ 0 ][ 3 ] = 8;
    BishopMoves[ 44 ][ 1 ][ 0 ] = 37;
    BishopMoves[ 44 ][ 1 ][ 1 ] = 30;
    BishopMoves[ 44 ][ 1 ][ 2 ] = 23;
    BishopMoves[ 44 ][ 2 ][ 0 ] = 51;
    BishopMoves[ 44 ][ 2 ][ 1 ] = 58;
    BishopMoves[ 44 ][ 3 ][ 0 ] = 53;
    BishopMoves[ 44 ][ 3 ][ 1 ] = 62;

    BishopMoves[ 45 ] = new int[ 4 ][];
    BishopMoves[ 45 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 45 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 45 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 45 ][ 3 ] = new int[ 2 ];
    BishopMoves[ 45 ][ 0 ][ 0 ] = 36;
    BishopMoves[ 45 ][ 0 ][ 1 ] = 27;
    BishopMoves[ 45 ][ 0 ][ 2 ] = 18;
    BishopMoves[ 45 ][ 0 ][ 3 ] = 9;
    BishopMoves[ 45 ][ 0 ][ 4 ] = 0;
    BishopMoves[ 45 ][ 1 ][ 0 ] = 38;
    BishopMoves[ 45 ][ 1 ][ 1 ] = 31;
    BishopMoves[ 45 ][ 2 ][ 0 ] = 52;
    BishopMoves[ 45 ][ 2 ][ 1 ] = 59;
    BishopMoves[ 45 ][ 3 ][ 0 ] = 54;
    BishopMoves[ 45 ][ 3 ][ 1 ] = 63;

    BishopMoves[ 46 ] = new int[ 4 ][];
    BishopMoves[ 46 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 46 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 46 ][ 2 ] = new int[ 2 ];
    BishopMoves[ 46 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 46 ][ 0 ][ 0 ] = 37;
    BishopMoves[ 46 ][ 0 ][ 1 ] = 28;
    BishopMoves[ 46 ][ 0 ][ 2 ] = 19;
    BishopMoves[ 46 ][ 0 ][ 3 ] = 10;
    BishopMoves[ 46 ][ 0 ][ 4 ] = 1;
    BishopMoves[ 46 ][ 1 ][ 0 ] = 39;
    BishopMoves[ 46 ][ 2 ][ 0 ] = 53;
    BishopMoves[ 46 ][ 2 ][ 1 ] = 60;
    BishopMoves[ 46 ][ 3 ][ 0 ] = 55;

    BishopMoves[ 47 ] = new int[ 2 ][];
    BishopMoves[ 47 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 47 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 47 ][ 0 ][ 0 ] = 38;
    BishopMoves[ 47 ][ 0 ][ 1 ] = 29;
    BishopMoves[ 47 ][ 0 ][ 2 ] = 20;
    BishopMoves[ 47 ][ 0 ][ 3 ] = 11;
    BishopMoves[ 47 ][ 0 ][ 4 ] = 2;
    BishopMoves[ 47 ][ 1 ][ 0 ] = 54;
    BishopMoves[ 47 ][ 1 ][ 1 ] = 61;

    BishopMoves[ 48 ] = new int[ 2 ][];
    BishopMoves[ 48 ][ 0 ] = new int[ 6 ];
    BishopMoves[ 48 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 48 ][ 0 ][ 0 ] = 41;
    BishopMoves[ 48 ][ 0 ][ 1 ] = 34;
    BishopMoves[ 48 ][ 0 ][ 2 ] = 27;
    BishopMoves[ 48 ][ 0 ][ 3 ] = 20;
    BishopMoves[ 48 ][ 0 ][ 4 ] = 13;
    BishopMoves[ 48 ][ 0 ][ 5 ] = 6;
    BishopMoves[ 48 ][ 1 ][ 0 ] = 57;

    BishopMoves[ 49 ] = new int[ 4 ][];
    BishopMoves[ 49 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 49 ][ 1 ] = new int[ 6 ];
    BishopMoves[ 49 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 49 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 49 ][ 0 ][ 0 ] = 40;
    BishopMoves[ 49 ][ 1 ][ 0 ] = 42;
    BishopMoves[ 49 ][ 1 ][ 1 ] = 35;
    BishopMoves[ 49 ][ 1 ][ 2 ] = 28;
    BishopMoves[ 49 ][ 1 ][ 3 ] = 21;
    BishopMoves[ 49 ][ 1 ][ 4 ] = 14;
    BishopMoves[ 49 ][ 1 ][ 5 ] = 7;
    BishopMoves[ 49 ][ 2 ][ 0 ] = 56;
    BishopMoves[ 49 ][ 3 ][ 0 ] = 58;

    BishopMoves[ 50 ] = new int[ 4 ][];
    BishopMoves[ 50 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 50 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 50 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 50 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 50 ][ 0 ][ 0 ] = 41;
    BishopMoves[ 50 ][ 0 ][ 1 ] = 32;
    BishopMoves[ 50 ][ 1 ][ 0 ] = 43;
    BishopMoves[ 50 ][ 1 ][ 1 ] = 36;
    BishopMoves[ 50 ][ 1 ][ 2 ] = 29;
    BishopMoves[ 50 ][ 1 ][ 3 ] = 22;
    BishopMoves[ 50 ][ 1 ][ 4 ] = 15;
    BishopMoves[ 50 ][ 2 ][ 0 ] = 57;
    BishopMoves[ 50 ][ 3 ][ 0 ] = 59;

    BishopMoves[ 51 ] = new int[ 4 ][];
    BishopMoves[ 51 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 51 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 51 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 51 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 51 ][ 0 ][ 0 ] = 42;
    BishopMoves[ 51 ][ 0 ][ 1 ] = 33;
    BishopMoves[ 51 ][ 0 ][ 2 ] = 24;
    BishopMoves[ 51 ][ 1 ][ 0 ] = 44;
    BishopMoves[ 51 ][ 1 ][ 1 ] = 37;
    BishopMoves[ 51 ][ 1 ][ 2 ] = 30;
    BishopMoves[ 51 ][ 1 ][ 3 ] = 23;
    BishopMoves[ 51 ][ 2 ][ 0 ] = 58;
    BishopMoves[ 51 ][ 3 ][ 0 ] = 60;

    BishopMoves[ 52 ] = new int[ 4 ][];
    BishopMoves[ 52 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 52 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 52 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 52 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 52 ][ 0 ][ 0 ] = 43;
    BishopMoves[ 52 ][ 0 ][ 1 ] = 34;
    BishopMoves[ 52 ][ 0 ][ 2 ] = 25;
    BishopMoves[ 52 ][ 0 ][ 3 ] = 16;
    BishopMoves[ 52 ][ 1 ][ 0 ] = 45;
    BishopMoves[ 52 ][ 1 ][ 1 ] = 38;
    BishopMoves[ 52 ][ 1 ][ 2 ] = 31;
    BishopMoves[ 52 ][ 2 ][ 0 ] = 59;
    BishopMoves[ 52 ][ 3 ][ 0 ] = 61;

    BishopMoves[ 53 ] = new int[ 4 ][];
    BishopMoves[ 53 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 53 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 53 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 53 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 53 ][ 0 ][ 0 ] = 44;
    BishopMoves[ 53 ][ 0 ][ 1 ] = 35;
    BishopMoves[ 53 ][ 0 ][ 2 ] = 26;
    BishopMoves[ 53 ][ 0 ][ 3 ] = 17;
    BishopMoves[ 53 ][ 0 ][ 4 ] = 8;
    BishopMoves[ 53 ][ 1 ][ 0 ] = 46;
    BishopMoves[ 53 ][ 1 ][ 1 ] = 39;
    BishopMoves[ 53 ][ 2 ][ 0 ] = 60;
    BishopMoves[ 53 ][ 3 ][ 0 ] = 62;

    BishopMoves[ 54 ] = new int[ 4 ][];
    BishopMoves[ 54 ][ 0 ] = new int[ 6 ];
    BishopMoves[ 54 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 54 ][ 2 ] = new int[ 1 ];
    BishopMoves[ 54 ][ 3 ] = new int[ 1 ];
    BishopMoves[ 54 ][ 0 ][ 0 ] = 45;
    BishopMoves[ 54 ][ 0 ][ 1 ] = 36;
    BishopMoves[ 54 ][ 0 ][ 2 ] = 27;
    BishopMoves[ 54 ][ 0 ][ 3 ] = 18;
    BishopMoves[ 54 ][ 0 ][ 4 ] = 9;
    BishopMoves[ 54 ][ 0 ][ 5 ] = 0;
    BishopMoves[ 54 ][ 1 ][ 0 ] = 47;
    BishopMoves[ 54 ][ 2 ][ 0 ] = 61;
    BishopMoves[ 54 ][ 3 ][ 0 ] = 63;

    BishopMoves[ 55 ] = new int[ 2 ][];
    BishopMoves[ 55 ][ 0 ] = new int[ 6 ];
    BishopMoves[ 55 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 55 ][ 0 ][ 0 ] = 46;
    BishopMoves[ 55 ][ 0 ][ 1 ] = 37;
    BishopMoves[ 55 ][ 0 ][ 2 ] = 28;
    BishopMoves[ 55 ][ 0 ][ 3 ] = 19;
    BishopMoves[ 55 ][ 0 ][ 4 ] = 10;
    BishopMoves[ 55 ][ 0 ][ 5 ] = 1;
    BishopMoves[ 55 ][ 1 ][ 0 ] = 62;

    BishopMoves[ 56 ] = new int[ 1 ][ 7 ];
    BishopMoves[ 56 ][ 0 ][ 0 ] = 49;
    BishopMoves[ 56 ][ 0 ][ 1 ] = 42;
    BishopMoves[ 56 ][ 0 ][ 2 ] = 35;
    BishopMoves[ 56 ][ 0 ][ 3 ] = 28;
    BishopMoves[ 56 ][ 0 ][ 4 ] = 21;
    BishopMoves[ 56 ][ 0 ][ 5 ] = 14;
    BishopMoves[ 56 ][ 0 ][ 6 ] = 7;

    BishopMoves[ 57 ] = new int[ 2 ][];
    BishopMoves[ 57 ][ 0 ] = new int[ 1 ];
    BishopMoves[ 57 ][ 1 ] = new int[ 6 ];
    BishopMoves[ 57 ][ 0 ][ 0 ] = 48;
    BishopMoves[ 57 ][ 1 ][ 0 ] = 50;
    BishopMoves[ 57 ][ 1 ][ 1 ] = 43;
    BishopMoves[ 57 ][ 1 ][ 2 ] = 36;
    BishopMoves[ 57 ][ 1 ][ 3 ] = 29;
    BishopMoves[ 57 ][ 1 ][ 4 ] = 22;
    BishopMoves[ 57 ][ 1 ][ 5 ] = 15;

    BishopMoves[ 58 ] = new int[ 2 ][];
    BishopMoves[ 58 ][ 0 ] = new int[ 2 ];
    BishopMoves[ 58 ][ 1 ] = new int[ 5 ];
    BishopMoves[ 58 ][ 0 ][ 0 ] = 49;
    BishopMoves[ 58 ][ 0 ][ 1 ] = 40;
    BishopMoves[ 58 ][ 1 ][ 0 ] = 51;
    BishopMoves[ 58 ][ 1 ][ 1 ] = 44;
    BishopMoves[ 58 ][ 1 ][ 2 ] = 37;
    BishopMoves[ 58 ][ 1 ][ 3 ] = 30;
    BishopMoves[ 58 ][ 1 ][ 4 ] = 23;

    BishopMoves[ 59 ] = new int[ 2 ][];
    BishopMoves[ 59 ][ 0 ] = new int[ 3 ];
    BishopMoves[ 59 ][ 1 ] = new int[ 4 ];
    BishopMoves[ 59 ][ 0 ][ 0 ] = 50;
    BishopMoves[ 59 ][ 0 ][ 1 ] = 41;
    BishopMoves[ 59 ][ 0 ][ 2 ] = 32;
    BishopMoves[ 59 ][ 1 ][ 0 ] = 52;
    BishopMoves[ 59 ][ 1 ][ 1 ] = 45;
    BishopMoves[ 59 ][ 1 ][ 2 ] = 38;
    BishopMoves[ 59 ][ 1 ][ 3 ] = 31;

    BishopMoves[ 60 ] = new int[ 2 ][];
    BishopMoves[ 60 ][ 0 ] = new int[ 4 ];
    BishopMoves[ 60 ][ 1 ] = new int[ 3 ];
    BishopMoves[ 60 ][ 0 ][ 0 ] = 51;
    BishopMoves[ 60 ][ 0 ][ 1 ] = 42;
    BishopMoves[ 60 ][ 0 ][ 2 ] = 33;
    BishopMoves[ 60 ][ 0 ][ 3 ] = 24;
    BishopMoves[ 60 ][ 1 ][ 0 ] = 53;
    BishopMoves[ 60 ][ 1 ][ 1 ] = 46;
    BishopMoves[ 60 ][ 1 ][ 2 ] = 39;

    BishopMoves[ 61 ] = new int[ 2 ][];
    BishopMoves[ 61 ][ 0 ] = new int[ 5 ];
    BishopMoves[ 61 ][ 1 ] = new int[ 2 ];
    BishopMoves[ 61 ][ 0 ][ 0 ] = 52;
    BishopMoves[ 61 ][ 0 ][ 1 ] = 43;
    BishopMoves[ 61 ][ 0 ][ 2 ] = 34;
    BishopMoves[ 61 ][ 0 ][ 3 ] = 25;
    BishopMoves[ 61 ][ 0 ][ 4 ] = 16;
    BishopMoves[ 61 ][ 1 ][ 0 ] = 54;
    BishopMoves[ 61 ][ 1 ][ 1 ] = 47;

    BishopMoves[ 62 ] = new int[ 2 ][];
    BishopMoves[ 62 ][ 0 ] = new int[ 6 ];
    BishopMoves[ 62 ][ 1 ] = new int[ 1 ];
    BishopMoves[ 62 ][ 0 ][ 0 ] = 53;
    BishopMoves[ 62 ][ 0 ][ 1 ] = 44;
    BishopMoves[ 62 ][ 0 ][ 2 ] = 35;
    BishopMoves[ 62 ][ 0 ][ 3 ] = 26;
    BishopMoves[ 62 ][ 0 ][ 4 ] = 17;
    BishopMoves[ 62 ][ 0 ][ 5 ] = 8;
    BishopMoves[ 62 ][ 1 ][ 0 ] = 55;

    BishopMoves[ 63 ] = new int[ 1 ][ 7 ];
    BishopMoves[ 63 ][ 0 ][ 0 ] = 54;
    BishopMoves[ 63 ][ 0 ][ 1 ] = 45;
    BishopMoves[ 63 ][ 0 ][ 2 ] = 36;
    BishopMoves[ 63 ][ 0 ][ 3 ] = 27;
    BishopMoves[ 63 ][ 0 ][ 4 ] = 18;
    BishopMoves[ 63 ][ 0 ][ 5 ] = 9;
    BishopMoves[ 63 ][ 0 ][ 6 ] = 0;

    RookMoves = new int[ 64 ][][];
    RookMoves[ 0 ] = new int[ 2 ][];
    RookMoves[ 0 ][ 0 ] = new int[ 7 ];
    RookMoves[ 0 ][ 1 ] = new int[ 7 ];
    RookMoves[ 0 ][ 0 ][ 0 ] = 1;
    RookMoves[ 0 ][ 0 ][ 1 ] = 2;
    RookMoves[ 0 ][ 0 ][ 2 ] = 3;
    RookMoves[ 0 ][ 0 ][ 3 ] = 4;
    RookMoves[ 0 ][ 0 ][ 4 ] = 5;
    RookMoves[ 0 ][ 0 ][ 5 ] = 6;
    RookMoves[ 0 ][ 0 ][ 6 ] = 7;
    RookMoves[ 0 ][ 1 ][ 0 ] = 8;
    RookMoves[ 0 ][ 1 ][ 1 ] = 16;
    RookMoves[ 0 ][ 1 ][ 2 ] = 24;
    RookMoves[ 0 ][ 1 ][ 3 ] = 32;
    RookMoves[ 0 ][ 1 ][ 4 ] = 40;
    RookMoves[ 0 ][ 1 ][ 5 ] = 48;
    RookMoves[ 0 ][ 1 ][ 6 ] = 56;

    RookMoves[ 1 ] = new int[ 3 ][];
    RookMoves[ 1 ][ 0 ] = new int[ 1 ];
    RookMoves[ 1 ][ 1 ] = new int[ 6 ];
    RookMoves[ 1 ][ 2 ] = new int[ 7 ];
    RookMoves[ 1 ][ 0 ][ 0 ] = 0;
    RookMoves[ 1 ][ 1 ][ 0 ] = 2;
    RookMoves[ 1 ][ 1 ][ 1 ] = 3;
    RookMoves[ 1 ][ 1 ][ 2 ] = 4;
    RookMoves[ 1 ][ 1 ][ 3 ] = 5;
    RookMoves[ 1 ][ 1 ][ 4 ] = 6;
    RookMoves[ 1 ][ 1 ][ 5 ] = 7;
    RookMoves[ 1 ][ 2 ][ 0 ] = 9;
    RookMoves[ 1 ][ 2 ][ 1 ] = 17;
    RookMoves[ 1 ][ 2 ][ 2 ] = 25;
    RookMoves[ 1 ][ 2 ][ 3 ] = 33;
    RookMoves[ 1 ][ 2 ][ 4 ] = 41;
    RookMoves[ 1 ][ 2 ][ 5 ] = 49;
    RookMoves[ 1 ][ 2 ][ 6 ] = 57;

    RookMoves[ 2 ] = new int[ 3 ][];
    RookMoves[ 2 ][ 0 ] = new int[ 2 ];
    RookMoves[ 2 ][ 1 ] = new int[ 5 ];
    RookMoves[ 2 ][ 2 ] = new int[ 7 ];
    RookMoves[ 2 ][ 0 ][ 0 ] = 1;
    RookMoves[ 2 ][ 0 ][ 1 ] = 0;
    RookMoves[ 2 ][ 1 ][ 0 ] = 3;
    RookMoves[ 2 ][ 1 ][ 1 ] = 4;
    RookMoves[ 2 ][ 1 ][ 2 ] = 5;
    RookMoves[ 2 ][ 1 ][ 3 ] = 6;
    RookMoves[ 2 ][ 1 ][ 4 ] = 7;
    RookMoves[ 2 ][ 2 ][ 0 ] = 10;
    RookMoves[ 2 ][ 2 ][ 1 ] = 18;
    RookMoves[ 2 ][ 2 ][ 2 ] = 26;
    RookMoves[ 2 ][ 2 ][ 3 ] = 34;
    RookMoves[ 2 ][ 2 ][ 4 ] = 42;
    RookMoves[ 2 ][ 2 ][ 5 ] = 50;
    RookMoves[ 2 ][ 2 ][ 6 ] = 58;

    RookMoves[ 3 ] = new int[ 3 ][];
    RookMoves[ 3 ][ 0 ] = new int[ 3 ];
    RookMoves[ 3 ][ 1 ] = new int[ 4 ];
    RookMoves[ 3 ][ 2 ] = new int[ 7 ];
    RookMoves[ 3 ][ 0 ][ 0 ] = 2;
    RookMoves[ 3 ][ 0 ][ 1 ] = 1;
    RookMoves[ 3 ][ 0 ][ 2 ] = 0;
    RookMoves[ 3 ][ 1 ][ 0 ] = 4;
    RookMoves[ 3 ][ 1 ][ 1 ] = 5;
    RookMoves[ 3 ][ 1 ][ 2 ] = 6;
    RookMoves[ 3 ][ 1 ][ 3 ] = 7;
    RookMoves[ 3 ][ 2 ][ 0 ] = 11;
    RookMoves[ 3 ][ 2 ][ 1 ] = 19;
    RookMoves[ 3 ][ 2 ][ 2 ] = 27;
    RookMoves[ 3 ][ 2 ][ 3 ] = 35;
    RookMoves[ 3 ][ 2 ][ 4 ] = 43;
    RookMoves[ 3 ][ 2 ][ 5 ] = 51;
    RookMoves[ 3 ][ 2 ][ 6 ] = 59;

    RookMoves[ 4 ] = new int[ 3 ][];
    RookMoves[ 4 ][ 0 ] = new int[ 4 ];
    RookMoves[ 4 ][ 1 ] = new int[ 3 ];
    RookMoves[ 4 ][ 2 ] = new int[ 7 ];
    RookMoves[ 4 ][ 0 ][ 0 ] = 3;
    RookMoves[ 4 ][ 0 ][ 1 ] = 2;
    RookMoves[ 4 ][ 0 ][ 2 ] = 1;
    RookMoves[ 4 ][ 0 ][ 3 ] = 0;
    RookMoves[ 4 ][ 1 ][ 0 ] = 5;
    RookMoves[ 4 ][ 1 ][ 1 ] = 6;
    RookMoves[ 4 ][ 1 ][ 2 ] = 7;
    RookMoves[ 4 ][ 2 ][ 0 ] = 12;
    RookMoves[ 4 ][ 2 ][ 1 ] = 20;
    RookMoves[ 4 ][ 2 ][ 2 ] = 28;
    RookMoves[ 4 ][ 2 ][ 3 ] = 36;
    RookMoves[ 4 ][ 2 ][ 4 ] = 44;
    RookMoves[ 4 ][ 2 ][ 5 ] = 52;
    RookMoves[ 4 ][ 2 ][ 6 ] = 60;

    RookMoves[ 5 ] = new int[ 3 ][];
    RookMoves[ 5 ][ 0 ] = new int[ 5 ];
    RookMoves[ 5 ][ 1 ] = new int[ 2 ];
    RookMoves[ 5 ][ 2 ] = new int[ 7 ];
    RookMoves[ 5 ][ 0 ][ 0 ] = 4;
    RookMoves[ 5 ][ 0 ][ 1 ] = 3;
    RookMoves[ 5 ][ 0 ][ 2 ] = 2;
    RookMoves[ 5 ][ 0 ][ 3 ] = 1;
    RookMoves[ 5 ][ 0 ][ 4 ] = 0;
    RookMoves[ 5 ][ 1 ][ 0 ] = 6;
    RookMoves[ 5 ][ 1 ][ 1 ] = 7;
    RookMoves[ 5 ][ 2 ][ 0 ] = 13;
    RookMoves[ 5 ][ 2 ][ 1 ] = 21;
    RookMoves[ 5 ][ 2 ][ 2 ] = 29;
    RookMoves[ 5 ][ 2 ][ 3 ] = 37;
    RookMoves[ 5 ][ 2 ][ 4 ] = 45;
    RookMoves[ 5 ][ 2 ][ 5 ] = 53;
    RookMoves[ 5 ][ 2 ][ 6 ] = 61;

    RookMoves[ 6 ] = new int[ 3 ][];
    RookMoves[ 6 ][ 0 ] = new int[ 6 ];
    RookMoves[ 6 ][ 1 ] = new int[ 1 ];
    RookMoves[ 6 ][ 2 ] = new int[ 7 ];
    RookMoves[ 6 ][ 0 ][ 0 ] = 5;
    RookMoves[ 6 ][ 0 ][ 1 ] = 4;
    RookMoves[ 6 ][ 0 ][ 2 ] = 3;
    RookMoves[ 6 ][ 0 ][ 3 ] = 2;
    RookMoves[ 6 ][ 0 ][ 4 ] = 1;
    RookMoves[ 6 ][ 0 ][ 5 ] = 0;
    RookMoves[ 6 ][ 1 ][ 0 ] = 7;
    RookMoves[ 6 ][ 2 ][ 0 ] = 14;
    RookMoves[ 6 ][ 2 ][ 1 ] = 22;
    RookMoves[ 6 ][ 2 ][ 2 ] = 30;
    RookMoves[ 6 ][ 2 ][ 3 ] = 38;
    RookMoves[ 6 ][ 2 ][ 4 ] = 46;
    RookMoves[ 6 ][ 2 ][ 5 ] = 54;
    RookMoves[ 6 ][ 2 ][ 6 ] = 62;

    RookMoves[ 7 ] = new int[ 2 ][];
    RookMoves[ 7 ][ 0 ] = new int[ 7 ];
    RookMoves[ 7 ][ 1 ] = new int[ 7 ];
    RookMoves[ 7 ][ 0 ][ 0 ] = 6;
    RookMoves[ 7 ][ 0 ][ 1 ] = 5;
    RookMoves[ 7 ][ 0 ][ 2 ] = 4;
    RookMoves[ 7 ][ 0 ][ 3 ] = 3;
    RookMoves[ 7 ][ 0 ][ 4 ] = 2;
    RookMoves[ 7 ][ 0 ][ 5 ] = 1;
    RookMoves[ 7 ][ 0 ][ 6 ] = 0;
    RookMoves[ 7 ][ 1 ][ 0 ] = 15;
    RookMoves[ 7 ][ 1 ][ 1 ] = 23;
    RookMoves[ 7 ][ 1 ][ 2 ] = 31;
    RookMoves[ 7 ][ 1 ][ 3 ] = 39;
    RookMoves[ 7 ][ 1 ][ 4 ] = 47;
    RookMoves[ 7 ][ 1 ][ 5 ] = 55;
    RookMoves[ 7 ][ 1 ][ 6 ] = 63;

    RookMoves[ 8 ] = new int[ 3 ][];
    RookMoves[ 8 ][ 0 ] = new int[ 7 ];
    RookMoves[ 8 ][ 1 ] = new int[ 1 ];
    RookMoves[ 8 ][ 2 ] = new int[ 6 ];
    RookMoves[ 8 ][ 0 ][ 0 ] = 9;
    RookMoves[ 8 ][ 0 ][ 1 ] = 10;
    RookMoves[ 8 ][ 0 ][ 2 ] = 11;
    RookMoves[ 8 ][ 0 ][ 3 ] = 12;
    RookMoves[ 8 ][ 0 ][ 4 ] = 13;
    RookMoves[ 8 ][ 0 ][ 5 ] = 14;
    RookMoves[ 8 ][ 0 ][ 6 ] = 15;
    RookMoves[ 8 ][ 1 ][ 0 ] = 0;
    RookMoves[ 8 ][ 2 ][ 0 ] = 16;
    RookMoves[ 8 ][ 2 ][ 1 ] = 24;
    RookMoves[ 8 ][ 2 ][ 2 ] = 32;
    RookMoves[ 8 ][ 2 ][ 3 ] = 40;
    RookMoves[ 8 ][ 2 ][ 4 ] = 48;
    RookMoves[ 8 ][ 2 ][ 5 ] = 56;

    RookMoves[ 9 ] = new int[ 4 ][];
    RookMoves[ 9 ][ 0 ] = new int[ 1 ];
    RookMoves[ 9 ][ 1 ] = new int[ 6 ];
    RookMoves[ 9 ][ 2 ] = new int[ 1 ];
    RookMoves[ 9 ][ 3 ] = new int[ 6 ];
    RookMoves[ 9 ][ 0 ][ 0 ] = 8;
    RookMoves[ 9 ][ 1 ][ 0 ] = 10;
    RookMoves[ 9 ][ 1 ][ 1 ] = 11;
    RookMoves[ 9 ][ 1 ][ 2 ] = 12;
    RookMoves[ 9 ][ 1 ][ 3 ] = 13;
    RookMoves[ 9 ][ 1 ][ 4 ] = 14;
    RookMoves[ 9 ][ 1 ][ 5 ] = 15;
    RookMoves[ 9 ][ 2 ][ 0 ] = 1;
    RookMoves[ 9 ][ 3 ][ 0 ] = 17;
    RookMoves[ 9 ][ 3 ][ 1 ] = 25;
    RookMoves[ 9 ][ 3 ][ 2 ] = 33;
    RookMoves[ 9 ][ 3 ][ 3 ] = 41;
    RookMoves[ 9 ][ 3 ][ 4 ] = 49;
    RookMoves[ 9 ][ 3 ][ 5 ] = 57;

    RookMoves[ 10 ] = new int[ 4 ][];
    RookMoves[ 10 ][ 0 ] = new int[ 2 ];
    RookMoves[ 10 ][ 1 ] = new int[ 5 ];
    RookMoves[ 10 ][ 2 ] = new int[ 1 ];
    RookMoves[ 10 ][ 3 ] = new int[ 6 ];
    RookMoves[ 10 ][ 0 ][ 0 ] = 9;
    RookMoves[ 10 ][ 0 ][ 1 ] = 8;
    RookMoves[ 10 ][ 1 ][ 0 ] = 11;
    RookMoves[ 10 ][ 1 ][ 1 ] = 12;
    RookMoves[ 10 ][ 1 ][ 2 ] = 13;
    RookMoves[ 10 ][ 1 ][ 3 ] = 14;
    RookMoves[ 10 ][ 1 ][ 4 ] = 15;
    RookMoves[ 10 ][ 2 ][ 0 ] = 2;
    RookMoves[ 10 ][ 3 ][ 0 ] = 18;
    RookMoves[ 10 ][ 3 ][ 1 ] = 26;
    RookMoves[ 10 ][ 3 ][ 2 ] = 34;
    RookMoves[ 10 ][ 3 ][ 3 ] = 42;
    RookMoves[ 10 ][ 3 ][ 4 ] = 50;
    RookMoves[ 10 ][ 3 ][ 5 ] = 58;

    RookMoves[ 11 ] = new int[ 4 ][];
    RookMoves[ 11 ][ 0 ] = new int[ 3 ];
    RookMoves[ 11 ][ 1 ] = new int[ 4 ];
    RookMoves[ 11 ][ 2 ] = new int[ 1 ];
    RookMoves[ 11 ][ 3 ] = new int[ 6 ];
    RookMoves[ 11 ][ 0 ][ 0 ] = 10;
    RookMoves[ 11 ][ 0 ][ 1 ] = 9;
    RookMoves[ 11 ][ 0 ][ 2 ] = 8;
    RookMoves[ 11 ][ 1 ][ 0 ] = 12;
    RookMoves[ 11 ][ 1 ][ 1 ] = 13;
    RookMoves[ 11 ][ 1 ][ 2 ] = 14;
    RookMoves[ 11 ][ 1 ][ 3 ] = 15;
    RookMoves[ 11 ][ 2 ][ 0 ] = 3;
    RookMoves[ 11 ][ 3 ][ 0 ] = 19;
    RookMoves[ 11 ][ 3 ][ 1 ] = 27;
    RookMoves[ 11 ][ 3 ][ 2 ] = 35;
    RookMoves[ 11 ][ 3 ][ 3 ] = 43;
    RookMoves[ 11 ][ 3 ][ 4 ] = 51;
    RookMoves[ 11 ][ 3 ][ 5 ] = 59;

    RookMoves[ 12 ] = new int[ 4 ][];
    RookMoves[ 12 ][ 0 ] = new int[ 4 ];
    RookMoves[ 12 ][ 1 ] = new int[ 3 ];
    RookMoves[ 12 ][ 2 ] = new int[ 1 ];
    RookMoves[ 12 ][ 3 ] = new int[ 6 ];
    RookMoves[ 12 ][ 0 ][ 0 ] = 11;
    RookMoves[ 12 ][ 0 ][ 1 ] = 10;
    RookMoves[ 12 ][ 0 ][ 2 ] = 9;
    RookMoves[ 12 ][ 0 ][ 3 ] = 8;
    RookMoves[ 12 ][ 1 ][ 0 ] = 13;
    RookMoves[ 12 ][ 1 ][ 1 ] = 14;
    RookMoves[ 12 ][ 1 ][ 2 ] = 15;
    RookMoves[ 12 ][ 2 ][ 0 ] = 4;
    RookMoves[ 12 ][ 3 ][ 0 ] = 20;
    RookMoves[ 12 ][ 3 ][ 1 ] = 28;
    RookMoves[ 12 ][ 3 ][ 2 ] = 36;
    RookMoves[ 12 ][ 3 ][ 3 ] = 44;
    RookMoves[ 12 ][ 3 ][ 4 ] = 52;
    RookMoves[ 12 ][ 3 ][ 5 ] = 60;

    RookMoves[ 13 ] = new int[ 4 ][];
    RookMoves[ 13 ][ 0 ] = new int[ 5 ];
    RookMoves[ 13 ][ 1 ] = new int[ 2 ];
    RookMoves[ 13 ][ 2 ] = new int[ 1 ];
    RookMoves[ 13 ][ 3 ] = new int[ 6 ];
    RookMoves[ 13 ][ 0 ][ 0 ] = 12;
    RookMoves[ 13 ][ 0 ][ 1 ] = 11;
    RookMoves[ 13 ][ 0 ][ 2 ] = 10;
    RookMoves[ 13 ][ 0 ][ 3 ] = 9;
    RookMoves[ 13 ][ 0 ][ 4 ] = 8;
    RookMoves[ 13 ][ 1 ][ 0 ] = 14;
    RookMoves[ 13 ][ 1 ][ 1 ] = 15;
    RookMoves[ 13 ][ 2 ][ 0 ] = 5;
    RookMoves[ 13 ][ 3 ][ 0 ] = 21;
    RookMoves[ 13 ][ 3 ][ 1 ] = 29;
    RookMoves[ 13 ][ 3 ][ 2 ] = 37;
    RookMoves[ 13 ][ 3 ][ 3 ] = 45;
    RookMoves[ 13 ][ 3 ][ 4 ] = 53;
    RookMoves[ 13 ][ 3 ][ 5 ] = 61;

    RookMoves[ 14 ] = new int[ 4 ][];
    RookMoves[ 14 ][ 0 ] = new int[ 6 ];
    RookMoves[ 14 ][ 1 ] = new int[ 1 ];
    RookMoves[ 14 ][ 2 ] = new int[ 1 ];
    RookMoves[ 14 ][ 3 ] = new int[ 6 ];
    RookMoves[ 14 ][ 0 ][ 0 ] = 13;
    RookMoves[ 14 ][ 0 ][ 1 ] = 12;
    RookMoves[ 14 ][ 0 ][ 2 ] = 11;
    RookMoves[ 14 ][ 0 ][ 3 ] = 10;
    RookMoves[ 14 ][ 0 ][ 4 ] = 9;
    RookMoves[ 14 ][ 0 ][ 5 ] = 8;
    RookMoves[ 14 ][ 1 ][ 0 ] = 15;
    RookMoves[ 14 ][ 2 ][ 0 ] = 6;
    RookMoves[ 14 ][ 3 ][ 0 ] = 22;
    RookMoves[ 14 ][ 3 ][ 1 ] = 30;
    RookMoves[ 14 ][ 3 ][ 2 ] = 38;
    RookMoves[ 14 ][ 3 ][ 3 ] = 46;
    RookMoves[ 14 ][ 3 ][ 4 ] = 54;
    RookMoves[ 14 ][ 3 ][ 5 ] = 62;

    RookMoves[ 15 ] = new int[ 3 ][];
    RookMoves[ 15 ][ 0 ] = new int[ 7 ];
    RookMoves[ 15 ][ 1 ] = new int[ 1 ];
    RookMoves[ 15 ][ 2 ] = new int[ 6 ];
    RookMoves[ 15 ][ 0 ][ 0 ] = 14;
    RookMoves[ 15 ][ 0 ][ 1 ] = 13;
    RookMoves[ 15 ][ 0 ][ 2 ] = 12;
    RookMoves[ 15 ][ 0 ][ 3 ] = 11;
    RookMoves[ 15 ][ 0 ][ 4 ] = 10;
    RookMoves[ 15 ][ 0 ][ 5 ] = 9;
    RookMoves[ 15 ][ 0 ][ 6 ] = 8;
    RookMoves[ 15 ][ 1 ][ 0 ] = 7;
    RookMoves[ 15 ][ 2 ][ 0 ] = 23;
    RookMoves[ 15 ][ 2 ][ 1 ] = 31;
    RookMoves[ 15 ][ 2 ][ 2 ] = 39;
    RookMoves[ 15 ][ 2 ][ 3 ] = 47;
    RookMoves[ 15 ][ 2 ][ 4 ] = 55;
    RookMoves[ 15 ][ 2 ][ 5 ] = 63;

    RookMoves[ 16 ] = new int[ 3 ][];
    RookMoves[ 16 ][ 0 ] = new int[ 2 ];
    RookMoves[ 16 ][ 1 ] = new int[ 5 ];
    RookMoves[ 16 ][ 2 ] = new int[ 7 ];
    RookMoves[ 16 ][ 0 ][ 0 ] = 8;
    RookMoves[ 16 ][ 0 ][ 1 ] = 0;
    RookMoves[ 16 ][ 1 ][ 0 ] = 24;
    RookMoves[ 16 ][ 1 ][ 1 ] = 32;
    RookMoves[ 16 ][ 1 ][ 2 ] = 40;
    RookMoves[ 16 ][ 1 ][ 3 ] = 48;
    RookMoves[ 16 ][ 1 ][ 4 ] = 56;
    RookMoves[ 16 ][ 2 ][ 0 ] = 17;
    RookMoves[ 16 ][ 2 ][ 1 ] = 18;
    RookMoves[ 16 ][ 2 ][ 2 ] = 19;
    RookMoves[ 16 ][ 2 ][ 3 ] = 20;
    RookMoves[ 16 ][ 2 ][ 4 ] = 21;
    RookMoves[ 16 ][ 2 ][ 5 ] = 22;
    RookMoves[ 16 ][ 2 ][ 6 ] = 23;

    RookMoves[ 17 ] = new int[ 4 ][];
    RookMoves[ 17 ][ 0 ] = new int[ 1 ];
    RookMoves[ 17 ][ 1 ] = new int[ 6 ];
    RookMoves[ 17 ][ 2 ] = new int[ 2 ];
    RookMoves[ 17 ][ 3 ] = new int[ 5 ];
    RookMoves[ 17 ][ 0 ][ 0 ] = 16;
    RookMoves[ 17 ][ 1 ][ 0 ] = 18;
    RookMoves[ 17 ][ 1 ][ 1 ] = 19;
    RookMoves[ 17 ][ 1 ][ 2 ] = 20;
    RookMoves[ 17 ][ 1 ][ 3 ] = 21;
    RookMoves[ 17 ][ 1 ][ 4 ] = 22;
    RookMoves[ 17 ][ 1 ][ 5 ] = 23;
    RookMoves[ 17 ][ 2 ][ 0 ] = 9;
    RookMoves[ 17 ][ 2 ][ 1 ] = 1;
    RookMoves[ 17 ][ 3 ][ 0 ] = 25;
    RookMoves[ 17 ][ 3 ][ 1 ] = 33;
    RookMoves[ 17 ][ 3 ][ 2 ] = 41;
    RookMoves[ 17 ][ 3 ][ 3 ] = 49;
    RookMoves[ 17 ][ 3 ][ 4 ] = 57;

    RookMoves[ 18 ] = new int[ 4 ][];
    RookMoves[ 18 ][ 0 ] = new int[ 2 ];
    RookMoves[ 18 ][ 1 ] = new int[ 5 ];
    RookMoves[ 18 ][ 2 ] = new int[ 2 ];
    RookMoves[ 18 ][ 3 ] = new int[ 5 ];
    RookMoves[ 18 ][ 0 ][ 0 ] = 17;
    RookMoves[ 18 ][ 0 ][ 1 ] = 16;
    RookMoves[ 18 ][ 1 ][ 0 ] = 19;
    RookMoves[ 18 ][ 1 ][ 1 ] = 20;
    RookMoves[ 18 ][ 1 ][ 2 ] = 21;
    RookMoves[ 18 ][ 1 ][ 3 ] = 22;
    RookMoves[ 18 ][ 1 ][ 4 ] = 23;
    RookMoves[ 18 ][ 2 ][ 0 ] = 10;
    RookMoves[ 18 ][ 2 ][ 1 ] = 2;
    RookMoves[ 18 ][ 3 ][ 0 ] = 26;
    RookMoves[ 18 ][ 3 ][ 1 ] = 34;
    RookMoves[ 18 ][ 3 ][ 2 ] = 42;
    RookMoves[ 18 ][ 3 ][ 3 ] = 50;
    RookMoves[ 18 ][ 3 ][ 4 ] = 58;

    RookMoves[ 19 ] = new int[ 4 ][];
    RookMoves[ 19 ][ 0 ] = new int[ 3 ];
    RookMoves[ 19 ][ 1 ] = new int[ 4 ];
    RookMoves[ 19 ][ 2 ] = new int[ 2 ];
    RookMoves[ 19 ][ 3 ] = new int[ 5 ];
    RookMoves[ 19 ][ 0 ][ 0 ] = 18;
    RookMoves[ 19 ][ 0 ][ 1 ] = 17;
    RookMoves[ 19 ][ 0 ][ 2 ] = 16;
    RookMoves[ 19 ][ 1 ][ 0 ] = 20;
    RookMoves[ 19 ][ 1 ][ 1 ] = 21;
    RookMoves[ 19 ][ 1 ][ 2 ] = 22;
    RookMoves[ 19 ][ 1 ][ 3 ] = 23;
    RookMoves[ 19 ][ 2 ][ 0 ] = 11;
    RookMoves[ 19 ][ 2 ][ 1 ] = 3;
    RookMoves[ 19 ][ 3 ][ 0 ] = 27;
    RookMoves[ 19 ][ 3 ][ 1 ] = 35;
    RookMoves[ 19 ][ 3 ][ 2 ] = 43;
    RookMoves[ 19 ][ 3 ][ 3 ] = 51;
    RookMoves[ 19 ][ 3 ][ 4 ] = 59;

    RookMoves[ 20 ] = new int[ 4 ][];
    RookMoves[ 20 ][ 0 ] = new int[ 4 ];
    RookMoves[ 20 ][ 1 ] = new int[ 3 ];
    RookMoves[ 20 ][ 2 ] = new int[ 2 ];
    RookMoves[ 20 ][ 3 ] = new int[ 5 ];
    RookMoves[ 20 ][ 0 ][ 0 ] = 19;
    RookMoves[ 20 ][ 0 ][ 1 ] = 18;
    RookMoves[ 20 ][ 0 ][ 2 ] = 17;
    RookMoves[ 20 ][ 0 ][ 3 ] = 16;
    RookMoves[ 20 ][ 1 ][ 0 ] = 21;
    RookMoves[ 20 ][ 1 ][ 1 ] = 22;
    RookMoves[ 20 ][ 1 ][ 2 ] = 23;
    RookMoves[ 20 ][ 2 ][ 0 ] = 12;
    RookMoves[ 20 ][ 2 ][ 1 ] = 4;
    RookMoves[ 20 ][ 3 ][ 0 ] = 28;
    RookMoves[ 20 ][ 3 ][ 1 ] = 36;
    RookMoves[ 20 ][ 3 ][ 2 ] = 44;
    RookMoves[ 20 ][ 3 ][ 3 ] = 52;
    RookMoves[ 20 ][ 3 ][ 4 ] = 60;

    RookMoves[ 21 ] = new int[ 4 ][];
    RookMoves[ 21 ][ 0 ] = new int[ 5 ];
    RookMoves[ 21 ][ 1 ] = new int[ 2 ];
    RookMoves[ 21 ][ 2 ] = new int[ 2 ];
    RookMoves[ 21 ][ 3 ] = new int[ 5 ];
    RookMoves[ 21 ][ 0 ][ 0 ] = 20;
    RookMoves[ 21 ][ 0 ][ 1 ] = 19;
    RookMoves[ 21 ][ 0 ][ 2 ] = 18;
    RookMoves[ 21 ][ 0 ][ 3 ] = 17;
    RookMoves[ 21 ][ 0 ][ 4 ] = 16;
    RookMoves[ 21 ][ 1 ][ 0 ] = 22;
    RookMoves[ 21 ][ 1 ][ 1 ] = 23;
    RookMoves[ 21 ][ 2 ][ 0 ] = 13;
    RookMoves[ 21 ][ 2 ][ 1 ] = 5;
    RookMoves[ 21 ][ 3 ][ 0 ] = 29;
    RookMoves[ 21 ][ 3 ][ 1 ] = 37;
    RookMoves[ 21 ][ 3 ][ 2 ] = 45;
    RookMoves[ 21 ][ 3 ][ 3 ] = 53;
    RookMoves[ 21 ][ 3 ][ 4 ] = 61;

    RookMoves[ 22 ] = new int[ 4 ][];
    RookMoves[ 22 ][ 0 ] = new int[ 6 ];
    RookMoves[ 22 ][ 1 ] = new int[ 1 ];
    RookMoves[ 22 ][ 2 ] = new int[ 2 ];
    RookMoves[ 22 ][ 3 ] = new int[ 5 ];
    RookMoves[ 22 ][ 0 ][ 0 ] = 21;
    RookMoves[ 22 ][ 0 ][ 1 ] = 20;
    RookMoves[ 22 ][ 0 ][ 2 ] = 19;
    RookMoves[ 22 ][ 0 ][ 3 ] = 18;
    RookMoves[ 22 ][ 0 ][ 4 ] = 17;
    RookMoves[ 22 ][ 0 ][ 5 ] = 16;
    RookMoves[ 22 ][ 1 ][ 0 ] = 23;
    RookMoves[ 22 ][ 2 ][ 0 ] = 14;
    RookMoves[ 22 ][ 2 ][ 1 ] = 6;
    RookMoves[ 22 ][ 3 ][ 0 ] = 30;
    RookMoves[ 22 ][ 3 ][ 1 ] = 38;
    RookMoves[ 22 ][ 3 ][ 2 ] = 46;
    RookMoves[ 22 ][ 3 ][ 3 ] = 54;
    RookMoves[ 22 ][ 3 ][ 4 ] = 62;

    RookMoves[ 23 ] = new int[ 3 ][];
    RookMoves[ 23 ][ 0 ] = new int[ 7 ];
    RookMoves[ 23 ][ 1 ] = new int[ 2 ];
    RookMoves[ 23 ][ 2 ] = new int[ 5 ];
    RookMoves[ 23 ][ 0 ][ 0 ] = 22;
    RookMoves[ 23 ][ 0 ][ 1 ] = 21;
    RookMoves[ 23 ][ 0 ][ 2 ] = 20;
    RookMoves[ 23 ][ 0 ][ 3 ] = 19;
    RookMoves[ 23 ][ 0 ][ 4 ] = 18;
    RookMoves[ 23 ][ 0 ][ 5 ] = 17;
    RookMoves[ 23 ][ 0 ][ 6 ] = 16;
    RookMoves[ 23 ][ 1 ][ 0 ] = 15;
    RookMoves[ 23 ][ 1 ][ 1 ] = 7;
    RookMoves[ 23 ][ 2 ][ 0 ] = 31;
    RookMoves[ 23 ][ 2 ][ 1 ] = 39;
    RookMoves[ 23 ][ 2 ][ 2 ] = 47;
    RookMoves[ 23 ][ 2 ][ 3 ] = 55;
    RookMoves[ 23 ][ 2 ][ 4 ] = 63;

    RookMoves[ 24 ] = new int[ 3 ][];
    RookMoves[ 24 ][ 0 ] = new int[ 3 ];
    RookMoves[ 24 ][ 1 ] = new int[ 4 ];
    RookMoves[ 24 ][ 2 ] = new int[ 7 ];
    RookMoves[ 24 ][ 0 ][ 0 ] = 16;
    RookMoves[ 24 ][ 0 ][ 1 ] = 8;
    RookMoves[ 24 ][ 0 ][ 2 ] = 8;
    RookMoves[ 24 ][ 1 ][ 0 ] = 32;
    RookMoves[ 24 ][ 1 ][ 1 ] = 40;
    RookMoves[ 24 ][ 1 ][ 2 ] = 48;
    RookMoves[ 24 ][ 1 ][ 3 ] = 56;
    RookMoves[ 24 ][ 2 ][ 0 ] = 25;
    RookMoves[ 24 ][ 2 ][ 1 ] = 26;
    RookMoves[ 24 ][ 2 ][ 2 ] = 27;
    RookMoves[ 24 ][ 2 ][ 3 ] = 28;
    RookMoves[ 24 ][ 2 ][ 4 ] = 29;
    RookMoves[ 24 ][ 2 ][ 5 ] = 30;
    RookMoves[ 24 ][ 2 ][ 6 ] = 31;

    RookMoves[ 25 ] = new int[ 4 ][];
    RookMoves[ 25 ][ 0 ] = new int[ 1 ];
    RookMoves[ 25 ][ 1 ] = new int[ 6 ];
    RookMoves[ 25 ][ 2 ] = new int[ 3 ];
    RookMoves[ 25 ][ 3 ] = new int[ 4 ];
    RookMoves[ 25 ][ 0 ][ 0 ] = 24;
    RookMoves[ 25 ][ 1 ][ 0 ] = 26;
    RookMoves[ 25 ][ 1 ][ 1 ] = 27;
    RookMoves[ 25 ][ 1 ][ 2 ] = 28;
    RookMoves[ 25 ][ 1 ][ 3 ] = 29;
    RookMoves[ 25 ][ 1 ][ 4 ] = 30;
    RookMoves[ 25 ][ 1 ][ 5 ] = 31;
    RookMoves[ 25 ][ 2 ][ 0 ] = 17;
    RookMoves[ 25 ][ 2 ][ 1 ] = 9;
    RookMoves[ 25 ][ 2 ][ 2 ] = 1;
    RookMoves[ 25 ][ 3 ][ 0 ] = 33;
    RookMoves[ 25 ][ 3 ][ 1 ] = 41;
    RookMoves[ 25 ][ 3 ][ 2 ] = 49;
    RookMoves[ 25 ][ 3 ][ 3 ] = 57;

    RookMoves[ 26 ] = new int[ 4 ][];
    RookMoves[ 26 ][ 0 ] = new int[ 2 ];
    RookMoves[ 26 ][ 1 ] = new int[ 5 ];
    RookMoves[ 26 ][ 2 ] = new int[ 3 ];
    RookMoves[ 26 ][ 3 ] = new int[ 4 ];
    RookMoves[ 26 ][ 0 ][ 0 ] = 25;
    RookMoves[ 26 ][ 0 ][ 1 ] = 24;
    RookMoves[ 26 ][ 1 ][ 0 ] = 27;
    RookMoves[ 26 ][ 1 ][ 1 ] = 28;
    RookMoves[ 26 ][ 1 ][ 2 ] = 29;
    RookMoves[ 26 ][ 1 ][ 3 ] = 30;
    RookMoves[ 26 ][ 1 ][ 4 ] = 31;
    RookMoves[ 26 ][ 2 ][ 0 ] = 18;
    RookMoves[ 26 ][ 2 ][ 1 ] = 10;
    RookMoves[ 26 ][ 2 ][ 2 ] = 2;
    RookMoves[ 26 ][ 3 ][ 0 ] = 34;
    RookMoves[ 26 ][ 3 ][ 1 ] = 42;
    RookMoves[ 26 ][ 3 ][ 2 ] = 50;
    RookMoves[ 26 ][ 3 ][ 3 ] = 58;

    RookMoves[ 27 ] = new int[ 4 ][];
    RookMoves[ 27 ][ 0 ] = new int[ 3 ];
    RookMoves[ 27 ][ 1 ] = new int[ 4 ];
    RookMoves[ 27 ][ 2 ] = new int[ 3 ];
    RookMoves[ 27 ][ 3 ] = new int[ 4 ];
    RookMoves[ 27 ][ 0 ][ 0 ] = 26;
    RookMoves[ 27 ][ 0 ][ 1 ] = 25;
    RookMoves[ 27 ][ 0 ][ 2 ] = 24;
    RookMoves[ 27 ][ 1 ][ 0 ] = 28;
    RookMoves[ 27 ][ 1 ][ 1 ] = 29;
    RookMoves[ 27 ][ 1 ][ 2 ] = 30;
    RookMoves[ 27 ][ 1 ][ 3 ] = 31;
    RookMoves[ 27 ][ 2 ][ 0 ] = 19;
    RookMoves[ 27 ][ 2 ][ 1 ] = 11;
    RookMoves[ 27 ][ 2 ][ 2 ] = 3;
    RookMoves[ 27 ][ 3 ][ 0 ] = 35;
    RookMoves[ 27 ][ 3 ][ 1 ] = 43;
    RookMoves[ 27 ][ 3 ][ 2 ] = 51;
    RookMoves[ 27 ][ 3 ][ 3 ] = 59;

    RookMoves[ 28 ] = new int[ 4 ][];
    RookMoves[ 28 ][ 0 ] = new int[ 4 ];
    RookMoves[ 28 ][ 1 ] = new int[ 3 ];
    RookMoves[ 28 ][ 2 ] = new int[ 3 ];
    RookMoves[ 28 ][ 3 ] = new int[ 4 ];
    RookMoves[ 28 ][ 0 ][ 0 ] = 27;
    RookMoves[ 28 ][ 0 ][ 1 ] = 26;
    RookMoves[ 28 ][ 0 ][ 2 ] = 25;
    RookMoves[ 28 ][ 0 ][ 3 ] = 24;
    RookMoves[ 28 ][ 1 ][ 0 ] = 29;
    RookMoves[ 28 ][ 1 ][ 1 ] = 30;
    RookMoves[ 28 ][ 1 ][ 2 ] = 31;
    RookMoves[ 28 ][ 2 ][ 0 ] = 20;
    RookMoves[ 28 ][ 2 ][ 1 ] = 12;
    RookMoves[ 28 ][ 2 ][ 2 ] = 4;
    RookMoves[ 28 ][ 3 ][ 0 ] = 36;
    RookMoves[ 28 ][ 3 ][ 1 ] = 44;
    RookMoves[ 28 ][ 3 ][ 2 ] = 52;
    RookMoves[ 28 ][ 3 ][ 3 ] = 60;

    RookMoves[ 29 ] = new int[ 4 ][];
    RookMoves[ 29 ][ 0 ] = new int[ 5 ];
    RookMoves[ 29 ][ 1 ] = new int[ 2 ];
    RookMoves[ 29 ][ 2 ] = new int[ 3 ];
    RookMoves[ 29 ][ 3 ] = new int[ 4 ];
    RookMoves[ 29 ][ 0 ][ 0 ] = 28;
    RookMoves[ 29 ][ 0 ][ 1 ] = 27;
    RookMoves[ 29 ][ 0 ][ 2 ] = 26;
    RookMoves[ 29 ][ 0 ][ 3 ] = 25;
    RookMoves[ 29 ][ 0 ][ 4 ] = 24;
    RookMoves[ 29 ][ 1 ][ 0 ] = 30;
    RookMoves[ 29 ][ 1 ][ 1 ] = 31;
    RookMoves[ 29 ][ 2 ][ 0 ] = 21;
    RookMoves[ 29 ][ 2 ][ 1 ] = 13;
    RookMoves[ 29 ][ 2 ][ 2 ] = 5;
    RookMoves[ 29 ][ 3 ][ 0 ] = 37;
    RookMoves[ 29 ][ 3 ][ 1 ] = 45;
    RookMoves[ 29 ][ 3 ][ 2 ] = 53;
    RookMoves[ 29 ][ 3 ][ 3 ] = 61;

    RookMoves[ 30 ] = new int[ 4 ][];
    RookMoves[ 30 ][ 0 ] = new int[ 6 ];
    RookMoves[ 30 ][ 1 ] = new int[ 1 ];
    RookMoves[ 30 ][ 2 ] = new int[ 3 ];
    RookMoves[ 30 ][ 3 ] = new int[ 4 ];
    RookMoves[ 30 ][ 0 ][ 0 ] = 29;
    RookMoves[ 30 ][ 0 ][ 1 ] = 28;
    RookMoves[ 30 ][ 0 ][ 2 ] = 27;
    RookMoves[ 30 ][ 0 ][ 3 ] = 26;
    RookMoves[ 30 ][ 0 ][ 4 ] = 25;
    RookMoves[ 30 ][ 0 ][ 5 ] = 24;
    RookMoves[ 30 ][ 1 ][ 0 ] = 31;
    RookMoves[ 30 ][ 2 ][ 0 ] = 22;
    RookMoves[ 30 ][ 2 ][ 1 ] = 14;
    RookMoves[ 30 ][ 2 ][ 2 ] = 6;
    RookMoves[ 30 ][ 3 ][ 0 ] = 38;
    RookMoves[ 30 ][ 3 ][ 1 ] = 46;
    RookMoves[ 30 ][ 3 ][ 2 ] = 54;
    RookMoves[ 30 ][ 3 ][ 3 ] = 62;

    RookMoves[ 31 ] = new int[ 3 ][];
    RookMoves[ 31 ][ 0 ] = new int[ 7 ];
    RookMoves[ 31 ][ 1 ] = new int[ 3 ];
    RookMoves[ 31 ][ 2 ] = new int[ 4 ];
    RookMoves[ 31 ][ 0 ][ 0 ] = 30;
    RookMoves[ 31 ][ 0 ][ 1 ] = 29;
    RookMoves[ 31 ][ 0 ][ 2 ] = 28;
    RookMoves[ 31 ][ 0 ][ 3 ] = 27;
    RookMoves[ 31 ][ 0 ][ 4 ] = 26;
    RookMoves[ 31 ][ 0 ][ 5 ] = 25;
    RookMoves[ 31 ][ 0 ][ 6 ] = 24;
    RookMoves[ 31 ][ 1 ][ 0 ] = 23;
    RookMoves[ 31 ][ 1 ][ 1 ] = 15;
    RookMoves[ 31 ][ 1 ][ 2 ] = 7;
    RookMoves[ 31 ][ 2 ][ 0 ] = 39;
    RookMoves[ 31 ][ 2 ][ 1 ] = 47;
    RookMoves[ 31 ][ 2 ][ 2 ] = 55;
    RookMoves[ 31 ][ 2 ][ 3 ] = 63;

    RookMoves[ 32 ] = new int[ 3 ][];
    RookMoves[ 32 ][ 0 ] = new int[ 7 ];
    RookMoves[ 32 ][ 1 ] = new int[ 3 ];
    RookMoves[ 32 ][ 2 ] = new int[ 4 ];
    RookMoves[ 32 ][ 0 ][ 0 ] = 33;
    RookMoves[ 32 ][ 0 ][ 1 ] = 34;
    RookMoves[ 32 ][ 0 ][ 2 ] = 35;
    RookMoves[ 32 ][ 0 ][ 3 ] = 36;
    RookMoves[ 32 ][ 0 ][ 4 ] = 37;
    RookMoves[ 32 ][ 0 ][ 5 ] = 38;
    RookMoves[ 32 ][ 0 ][ 6 ] = 39;
    RookMoves[ 32 ][ 1 ][ 0 ] = 40;
    RookMoves[ 32 ][ 1 ][ 1 ] = 48;
    RookMoves[ 32 ][ 1 ][ 2 ] = 56;
    RookMoves[ 32 ][ 2 ][ 0 ] = 24;
    RookMoves[ 32 ][ 2 ][ 1 ] = 16;
    RookMoves[ 32 ][ 2 ][ 2 ] = 8;
    RookMoves[ 32 ][ 2 ][ 3 ] = 0;

    RookMoves[ 33 ] = new int[ 4 ][];
    RookMoves[ 33 ][ 0 ] = new int[ 1 ];
    RookMoves[ 33 ][ 1 ] = new int[ 6 ];
    RookMoves[ 33 ][ 2 ] = new int[ 4 ];
    RookMoves[ 33 ][ 3 ] = new int[ 3 ];
    RookMoves[ 33 ][ 0 ][ 0 ] = 32;
    RookMoves[ 33 ][ 1 ][ 0 ] = 34;
    RookMoves[ 33 ][ 1 ][ 1 ] = 35;
    RookMoves[ 33 ][ 1 ][ 2 ] = 36;
    RookMoves[ 33 ][ 1 ][ 3 ] = 37;
    RookMoves[ 33 ][ 1 ][ 4 ] = 38;
    RookMoves[ 33 ][ 1 ][ 5 ] = 39;
    RookMoves[ 33 ][ 2 ][ 0 ] = 25;
    RookMoves[ 33 ][ 2 ][ 1 ] = 17;
    RookMoves[ 33 ][ 2 ][ 2 ] = 9;
    RookMoves[ 33 ][ 2 ][ 3 ] = 1;
    RookMoves[ 33 ][ 3 ][ 0 ] = 41;
    RookMoves[ 33 ][ 3 ][ 1 ] = 49;
    RookMoves[ 33 ][ 3 ][ 2 ] = 57;

    RookMoves[ 34 ] = new int[ 4 ][];
    RookMoves[ 34 ][ 0 ] = new int[ 2 ];
    RookMoves[ 34 ][ 1 ] = new int[ 5 ];
    RookMoves[ 34 ][ 2 ] = new int[ 4 ];
    RookMoves[ 34 ][ 3 ] = new int[ 3 ];
    RookMoves[ 34 ][ 0 ][ 0 ] = 33;
    RookMoves[ 34 ][ 0 ][ 1 ] = 32;
    RookMoves[ 34 ][ 1 ][ 0 ] = 35;
    RookMoves[ 34 ][ 1 ][ 1 ] = 36;
    RookMoves[ 34 ][ 1 ][ 2 ] = 37;
    RookMoves[ 34 ][ 1 ][ 3 ] = 38;
    RookMoves[ 34 ][ 1 ][ 4 ] = 39;
    RookMoves[ 34 ][ 2 ][ 0 ] = 26;
    RookMoves[ 34 ][ 2 ][ 1 ] = 18;
    RookMoves[ 34 ][ 2 ][ 2 ] = 10;
    RookMoves[ 34 ][ 2 ][ 3 ] = 2;
    RookMoves[ 34 ][ 3 ][ 0 ] = 42;
    RookMoves[ 34 ][ 3 ][ 1 ] = 50;
    RookMoves[ 34 ][ 3 ][ 2 ] = 58;

    RookMoves[ 35 ] = new int[ 4 ][];
    RookMoves[ 35 ][ 0 ] = new int[ 3 ];
    RookMoves[ 35 ][ 1 ] = new int[ 4 ];
    RookMoves[ 35 ][ 2 ] = new int[ 4 ];
    RookMoves[ 35 ][ 3 ] = new int[ 3 ];
    RookMoves[ 35 ][ 0 ][ 0 ] = 34;
    RookMoves[ 35 ][ 0 ][ 1 ] = 33;
    RookMoves[ 35 ][ 0 ][ 2 ] = 32;
    RookMoves[ 35 ][ 1 ][ 0 ] = 36;
    RookMoves[ 35 ][ 1 ][ 1 ] = 37;
    RookMoves[ 35 ][ 1 ][ 2 ] = 38;
    RookMoves[ 35 ][ 1 ][ 3 ] = 39;
    RookMoves[ 35 ][ 2 ][ 0 ] = 27;
    RookMoves[ 35 ][ 2 ][ 1 ] = 19;
    RookMoves[ 35 ][ 2 ][ 2 ] = 11;
    RookMoves[ 35 ][ 2 ][ 3 ] = 3;
    RookMoves[ 35 ][ 3 ][ 0 ] = 43;
    RookMoves[ 35 ][ 3 ][ 1 ] = 51;
    RookMoves[ 35 ][ 3 ][ 2 ] = 59;

    RookMoves[ 36 ] = new int[ 4 ][];
    RookMoves[ 36 ][ 0 ] = new int[ 4 ];
    RookMoves[ 36 ][ 1 ] = new int[ 3 ];
    RookMoves[ 36 ][ 2 ] = new int[ 4 ];
    RookMoves[ 36 ][ 3 ] = new int[ 3 ];
    RookMoves[ 36 ][ 0 ][ 0 ] = 35;
    RookMoves[ 36 ][ 0 ][ 1 ] = 34;
    RookMoves[ 36 ][ 0 ][ 2 ] = 33;
    RookMoves[ 36 ][ 0 ][ 3 ] = 32;
    RookMoves[ 36 ][ 1 ][ 0 ] = 37;
    RookMoves[ 36 ][ 1 ][ 1 ] = 38;
    RookMoves[ 36 ][ 1 ][ 2 ] = 39;
    RookMoves[ 36 ][ 2 ][ 0 ] = 28;
    RookMoves[ 36 ][ 2 ][ 1 ] = 20;
    RookMoves[ 36 ][ 2 ][ 2 ] = 12;
    RookMoves[ 36 ][ 2 ][ 3 ] = 4;
    RookMoves[ 36 ][ 3 ][ 0 ] = 44;
    RookMoves[ 36 ][ 3 ][ 1 ] = 52;
    RookMoves[ 36 ][ 3 ][ 2 ] = 60;

    RookMoves[ 37 ] = new int[ 4 ][];
    RookMoves[ 37 ][ 0 ] = new int[ 5 ];
    RookMoves[ 37 ][ 1 ] = new int[ 2 ];
    RookMoves[ 37 ][ 2 ] = new int[ 4 ];
    RookMoves[ 37 ][ 3 ] = new int[ 3 ];
    RookMoves[ 37 ][ 0 ][ 0 ] = 36;
    RookMoves[ 37 ][ 0 ][ 1 ] = 35;
    RookMoves[ 37 ][ 0 ][ 2 ] = 34;
    RookMoves[ 37 ][ 0 ][ 3 ] = 33;
    RookMoves[ 37 ][ 0 ][ 4 ] = 32;
    RookMoves[ 37 ][ 1 ][ 0 ] = 38;
    RookMoves[ 37 ][ 1 ][ 1 ] = 39;
    RookMoves[ 37 ][ 2 ][ 0 ] = 29;
    RookMoves[ 37 ][ 2 ][ 1 ] = 21;
    RookMoves[ 37 ][ 2 ][ 2 ] = 13;
    RookMoves[ 37 ][ 2 ][ 3 ] = 5;
    RookMoves[ 37 ][ 3 ][ 0 ] = 45;
    RookMoves[ 37 ][ 3 ][ 1 ] = 53;
    RookMoves[ 37 ][ 3 ][ 2 ] = 61;

    RookMoves[ 38 ] = new int[ 4 ][];
    RookMoves[ 38 ][ 0 ] = new int[ 6 ];
    RookMoves[ 38 ][ 1 ] = new int[ 1 ];
    RookMoves[ 38 ][ 2 ] = new int[ 4 ];
    RookMoves[ 38 ][ 3 ] = new int[ 3 ];
    RookMoves[ 38 ][ 0 ][ 0 ] = 37;
    RookMoves[ 38 ][ 0 ][ 1 ] = 36;
    RookMoves[ 38 ][ 0 ][ 2 ] = 35;
    RookMoves[ 38 ][ 0 ][ 3 ] = 34;
    RookMoves[ 38 ][ 0 ][ 4 ] = 33;
    RookMoves[ 38 ][ 0 ][ 5 ] = 32;
    RookMoves[ 38 ][ 1 ][ 0 ] = 39;
    RookMoves[ 38 ][ 2 ][ 0 ] = 30;
    RookMoves[ 38 ][ 2 ][ 1 ] = 22;
    RookMoves[ 38 ][ 2 ][ 2 ] = 14;
    RookMoves[ 38 ][ 2 ][ 3 ] = 6;
    RookMoves[ 38 ][ 3 ][ 0 ] = 46;
    RookMoves[ 38 ][ 3 ][ 1 ] = 54;
    RookMoves[ 38 ][ 3 ][ 2 ] = 62;

    RookMoves[ 39 ] = new int[ 3 ][];
    RookMoves[ 39 ][ 0 ] = new int[ 7 ];
    RookMoves[ 39 ][ 1 ] = new int[ 4 ];
    RookMoves[ 39 ][ 2 ] = new int[ 3 ];
    RookMoves[ 39 ][ 0 ][ 0 ] = 38;
    RookMoves[ 39 ][ 0 ][ 1 ] = 37;
    RookMoves[ 39 ][ 0 ][ 2 ] = 36;
    RookMoves[ 39 ][ 0 ][ 3 ] = 35;
    RookMoves[ 39 ][ 0 ][ 4 ] = 34;
    RookMoves[ 39 ][ 0 ][ 5 ] = 33;
    RookMoves[ 39 ][ 0 ][ 6 ] = 32;
    RookMoves[ 39 ][ 1 ][ 0 ] = 31;
    RookMoves[ 39 ][ 1 ][ 1 ] = 23;
    RookMoves[ 39 ][ 1 ][ 2 ] = 15;
    RookMoves[ 39 ][ 1 ][ 3 ] = 7;
    RookMoves[ 39 ][ 2 ][ 0 ] = 47;
    RookMoves[ 39 ][ 2 ][ 1 ] = 55;
    RookMoves[ 39 ][ 2 ][ 2 ] = 63;

    RookMoves[ 40 ] = new int[ 3 ][];
    RookMoves[ 40 ][ 0 ] = new int[ 7 ];
    RookMoves[ 40 ][ 1 ] = new int[ 5 ];
    RookMoves[ 40 ][ 2 ] = new int[ 2 ];
    RookMoves[ 40 ][ 0 ][ 0 ] = 41;
    RookMoves[ 40 ][ 0 ][ 1 ] = 42;
    RookMoves[ 40 ][ 0 ][ 2 ] = 43;
    RookMoves[ 40 ][ 0 ][ 3 ] = 44;
    RookMoves[ 40 ][ 0 ][ 4 ] = 45;
    RookMoves[ 40 ][ 0 ][ 5 ] = 46;
    RookMoves[ 40 ][ 0 ][ 6 ] = 47;
    RookMoves[ 40 ][ 1 ][ 0 ] = 32;
    RookMoves[ 40 ][ 1 ][ 1 ] = 24;
    RookMoves[ 40 ][ 1 ][ 2 ] = 16;
    RookMoves[ 40 ][ 1 ][ 3 ] = 8;
    RookMoves[ 40 ][ 1 ][ 4 ] = 0;
    RookMoves[ 40 ][ 2 ][ 0 ] = 48;
    RookMoves[ 40 ][ 2 ][ 1 ] = 56;

    RookMoves[ 41 ] = new int[ 4 ][];
    RookMoves[ 41 ][ 0 ] = new int[ 1 ];
    RookMoves[ 41 ][ 1 ] = new int[ 6 ];
    RookMoves[ 41 ][ 2 ] = new int[ 5 ];
    RookMoves[ 41 ][ 3 ] = new int[ 2 ];
    RookMoves[ 41 ][ 0 ][ 0 ] = 40;
    RookMoves[ 41 ][ 1 ][ 0 ] = 42;
    RookMoves[ 41 ][ 1 ][ 1 ] = 43;
    RookMoves[ 41 ][ 1 ][ 2 ] = 44;
    RookMoves[ 41 ][ 1 ][ 3 ] = 45;
    RookMoves[ 41 ][ 1 ][ 4 ] = 46;
    RookMoves[ 41 ][ 1 ][ 5 ] = 47;
    RookMoves[ 41 ][ 2 ][ 0 ] = 33;
    RookMoves[ 41 ][ 2 ][ 1 ] = 25;
    RookMoves[ 41 ][ 2 ][ 2 ] = 17;
    RookMoves[ 41 ][ 2 ][ 3 ] = 9;
    RookMoves[ 41 ][ 2 ][ 4 ] = 1;
    RookMoves[ 41 ][ 3 ][ 0 ] = 49;
    RookMoves[ 41 ][ 3 ][ 1 ] = 57;

    RookMoves[ 42 ] = new int[ 4 ][];
    RookMoves[ 42 ][ 0 ] = new int[ 2 ];
    RookMoves[ 42 ][ 1 ] = new int[ 5 ];
    RookMoves[ 42 ][ 2 ] = new int[ 5 ];
    RookMoves[ 42 ][ 3 ] = new int[ 2 ];
    RookMoves[ 42 ][ 0 ][ 0 ] = 41;
    RookMoves[ 42 ][ 0 ][ 1 ] = 40;
    RookMoves[ 42 ][ 1 ][ 0 ] = 43;
    RookMoves[ 42 ][ 1 ][ 1 ] = 44;
    RookMoves[ 42 ][ 1 ][ 2 ] = 45;
    RookMoves[ 42 ][ 1 ][ 3 ] = 46;
    RookMoves[ 42 ][ 1 ][ 4 ] = 47;
    RookMoves[ 42 ][ 2 ][ 0 ] = 34;
    RookMoves[ 42 ][ 2 ][ 1 ] = 26;
    RookMoves[ 42 ][ 2 ][ 2 ] = 18;
    RookMoves[ 42 ][ 2 ][ 3 ] = 10;
    RookMoves[ 42 ][ 2 ][ 4 ] = 2;
    RookMoves[ 42 ][ 3 ][ 0 ] = 50;
    RookMoves[ 42 ][ 3 ][ 1 ] = 58;

    RookMoves[ 43 ] = new int[ 4 ][];
    RookMoves[ 43 ][ 0 ] = new int[ 3 ];
    RookMoves[ 43 ][ 1 ] = new int[ 4 ];
    RookMoves[ 43 ][ 2 ] = new int[ 5 ];
    RookMoves[ 43 ][ 3 ] = new int[ 2 ];
    RookMoves[ 43 ][ 0 ][ 0 ] = 42;
    RookMoves[ 43 ][ 0 ][ 1 ] = 41;
    RookMoves[ 43 ][ 0 ][ 2 ] = 40;
    RookMoves[ 43 ][ 1 ][ 0 ] = 44;
    RookMoves[ 43 ][ 1 ][ 1 ] = 45;
    RookMoves[ 43 ][ 1 ][ 2 ] = 46;
    RookMoves[ 43 ][ 1 ][ 3 ] = 47;
    RookMoves[ 43 ][ 2 ][ 0 ] = 35;
    RookMoves[ 43 ][ 2 ][ 1 ] = 27;
    RookMoves[ 43 ][ 2 ][ 2 ] = 19;
    RookMoves[ 43 ][ 2 ][ 3 ] = 11;
    RookMoves[ 43 ][ 2 ][ 4 ] = 3;
    RookMoves[ 43 ][ 3 ][ 0 ] = 51;
    RookMoves[ 43 ][ 3 ][ 1 ] = 59;

    RookMoves[ 44 ] = new int[ 4 ][];
    RookMoves[ 44 ][ 0 ] = new int[ 4 ];
    RookMoves[ 44 ][ 1 ] = new int[ 3 ];
    RookMoves[ 44 ][ 2 ] = new int[ 5 ];
    RookMoves[ 44 ][ 3 ] = new int[ 2 ];
    RookMoves[ 44 ][ 0 ][ 0 ] = 43;
    RookMoves[ 44 ][ 0 ][ 1 ] = 42;
    RookMoves[ 44 ][ 0 ][ 2 ] = 41;
    RookMoves[ 44 ][ 0 ][ 3 ] = 40;
    RookMoves[ 44 ][ 1 ][ 0 ] = 45;
    RookMoves[ 44 ][ 1 ][ 1 ] = 46;
    RookMoves[ 44 ][ 1 ][ 2 ] = 47;
    RookMoves[ 44 ][ 2 ][ 0 ] = 36;
    RookMoves[ 44 ][ 2 ][ 1 ] = 28;
    RookMoves[ 44 ][ 2 ][ 2 ] = 20;
    RookMoves[ 44 ][ 2 ][ 3 ] = 12;
    RookMoves[ 44 ][ 2 ][ 4 ] = 4;
    RookMoves[ 44 ][ 3 ][ 0 ] = 52;
    RookMoves[ 44 ][ 3 ][ 1 ] = 60;

    RookMoves[ 45 ] = new int[ 4 ][];
    RookMoves[ 45 ][ 0 ] = new int[ 5 ];
    RookMoves[ 45 ][ 1 ] = new int[ 2 ];
    RookMoves[ 45 ][ 2 ] = new int[ 5 ];
    RookMoves[ 45 ][ 3 ] = new int[ 2 ];
    RookMoves[ 45 ][ 0 ][ 0 ] = 44;
    RookMoves[ 45 ][ 0 ][ 1 ] = 43;
    RookMoves[ 45 ][ 0 ][ 2 ] = 42;
    RookMoves[ 45 ][ 0 ][ 3 ] = 41;
    RookMoves[ 45 ][ 0 ][ 4 ] = 40;
    RookMoves[ 45 ][ 1 ][ 0 ] = 46;
    RookMoves[ 45 ][ 1 ][ 1 ] = 47;
    RookMoves[ 45 ][ 2 ][ 0 ] = 37;
    RookMoves[ 45 ][ 2 ][ 1 ] = 29;
    RookMoves[ 45 ][ 2 ][ 2 ] = 21;
    RookMoves[ 45 ][ 2 ][ 3 ] = 13;
    RookMoves[ 45 ][ 2 ][ 4 ] = 5;
    RookMoves[ 45 ][ 3 ][ 0 ] = 53;
    RookMoves[ 45 ][ 3 ][ 1 ] = 61;

    RookMoves[ 46 ] = new int[ 4 ][];
    RookMoves[ 46 ][ 0 ] = new int[ 6 ];
    RookMoves[ 46 ][ 1 ] = new int[ 1 ];
    RookMoves[ 46 ][ 2 ] = new int[ 5 ];
    RookMoves[ 46 ][ 3 ] = new int[ 2 ];
    RookMoves[ 46 ][ 0 ][ 0 ] = 45;
    RookMoves[ 46 ][ 0 ][ 1 ] = 44;
    RookMoves[ 46 ][ 0 ][ 2 ] = 43;
    RookMoves[ 46 ][ 0 ][ 3 ] = 42;
    RookMoves[ 46 ][ 0 ][ 4 ] = 41;
    RookMoves[ 46 ][ 0 ][ 5 ] = 40;
    RookMoves[ 46 ][ 1 ][ 0 ] = 47;
    RookMoves[ 46 ][ 2 ][ 0 ] = 38;
    RookMoves[ 46 ][ 2 ][ 1 ] = 30;
    RookMoves[ 46 ][ 2 ][ 2 ] = 22;
    RookMoves[ 46 ][ 2 ][ 3 ] = 14;
    RookMoves[ 46 ][ 2 ][ 4 ] = 6;
    RookMoves[ 46 ][ 3 ][ 0 ] = 54;
    RookMoves[ 46 ][ 3 ][ 1 ] = 62;

    RookMoves[ 47 ] = new int[ 3 ][];
    RookMoves[ 47 ][ 0 ] = new int[ 7 ];
    RookMoves[ 47 ][ 1 ] = new int[ 5 ];
    RookMoves[ 47 ][ 2 ] = new int[ 2 ];
    RookMoves[ 47 ][ 0 ][ 0 ] = 46;
    RookMoves[ 47 ][ 0 ][ 1 ] = 45;
    RookMoves[ 47 ][ 0 ][ 2 ] = 44;
    RookMoves[ 47 ][ 0 ][ 3 ] = 43;
    RookMoves[ 47 ][ 0 ][ 4 ] = 42;
    RookMoves[ 47 ][ 0 ][ 5 ] = 41;
    RookMoves[ 47 ][ 0 ][ 6 ] = 40;
    RookMoves[ 47 ][ 1 ][ 0 ] = 39;
    RookMoves[ 47 ][ 1 ][ 1 ] = 31;
    RookMoves[ 47 ][ 1 ][ 2 ] = 23;
    RookMoves[ 47 ][ 1 ][ 3 ] = 15;
    RookMoves[ 47 ][ 1 ][ 4 ] = 7;
    RookMoves[ 47 ][ 2 ][ 0 ] = 55;
    RookMoves[ 47 ][ 2 ][ 1 ] = 63;

    RookMoves[ 48 ] = new int[ 3 ][];
    RookMoves[ 48 ][ 0 ] = new int[ 7 ];
    RookMoves[ 48 ][ 1 ] = new int[ 6 ];
    RookMoves[ 48 ][ 2 ] = new int[ 1 ];
    RookMoves[ 48 ][ 0 ][ 0 ] = 49;
    RookMoves[ 48 ][ 0 ][ 1 ] = 50;
    RookMoves[ 48 ][ 0 ][ 2 ] = 51;
    RookMoves[ 48 ][ 0 ][ 3 ] = 52;
    RookMoves[ 48 ][ 0 ][ 4 ] = 53;
    RookMoves[ 48 ][ 0 ][ 5 ] = 54;
    RookMoves[ 48 ][ 0 ][ 6 ] = 55;
    RookMoves[ 48 ][ 1 ][ 0 ] = 40;
    RookMoves[ 48 ][ 1 ][ 1 ] = 32;
    RookMoves[ 48 ][ 1 ][ 2 ] = 24;
    RookMoves[ 48 ][ 1 ][ 3 ] = 16;
    RookMoves[ 48 ][ 1 ][ 4 ] = 8;
    RookMoves[ 48 ][ 1 ][ 5 ] = 0;
    RookMoves[ 48 ][ 2 ][ 0 ] = 56;

    RookMoves[ 49 ] = new int[ 4 ][];
    RookMoves[ 49 ][ 0 ] = new int[ 1 ];
    RookMoves[ 49 ][ 1 ] = new int[ 6 ];
    RookMoves[ 49 ][ 2 ] = new int[ 6 ];
    RookMoves[ 49 ][ 3 ] = new int[ 1 ];
    RookMoves[ 49 ][ 0 ][ 0 ] = 48;
    RookMoves[ 49 ][ 1 ][ 0 ] = 50;
    RookMoves[ 49 ][ 1 ][ 1 ] = 51;
    RookMoves[ 49 ][ 1 ][ 2 ] = 52;
    RookMoves[ 49 ][ 1 ][ 3 ] = 53;
    RookMoves[ 49 ][ 1 ][ 4 ] = 54;
    RookMoves[ 49 ][ 1 ][ 5 ] = 55;
    RookMoves[ 49 ][ 2 ][ 0 ] = 41;
    RookMoves[ 49 ][ 2 ][ 1 ] = 33;
    RookMoves[ 49 ][ 2 ][ 2 ] = 25;
    RookMoves[ 49 ][ 2 ][ 3 ] = 17;
    RookMoves[ 49 ][ 2 ][ 4 ] = 9;
    RookMoves[ 49 ][ 2 ][ 5 ] = 1;
    RookMoves[ 49 ][ 3 ][ 0 ] = 57;

    RookMoves[ 50 ] = new int[ 4 ][];
    RookMoves[ 50 ][ 0 ] = new int[ 2 ];
    RookMoves[ 50 ][ 1 ] = new int[ 5 ];
    RookMoves[ 50 ][ 2 ] = new int[ 6 ];
    RookMoves[ 50 ][ 3 ] = new int[ 1 ];
    RookMoves[ 50 ][ 0 ][ 0 ] = 49;
    RookMoves[ 50 ][ 0 ][ 1 ] = 48;
    RookMoves[ 50 ][ 1 ][ 0 ] = 51;
    RookMoves[ 50 ][ 1 ][ 1 ] = 52;
    RookMoves[ 50 ][ 1 ][ 2 ] = 53;
    RookMoves[ 50 ][ 1 ][ 3 ] = 54;
    RookMoves[ 50 ][ 1 ][ 4 ] = 55;
    RookMoves[ 50 ][ 2 ][ 0 ] = 42;
    RookMoves[ 50 ][ 2 ][ 1 ] = 34;
    RookMoves[ 50 ][ 2 ][ 2 ] = 26;
    RookMoves[ 50 ][ 2 ][ 3 ] = 18;
    RookMoves[ 50 ][ 2 ][ 4 ] = 10;
    RookMoves[ 50 ][ 2 ][ 5 ] = 2;
    RookMoves[ 50 ][ 3 ][ 0 ] = 58;

    RookMoves[ 51 ] = new int[ 4 ][];
    RookMoves[ 51 ][ 0 ] = new int[ 3 ];
    RookMoves[ 51 ][ 1 ] = new int[ 4 ];
    RookMoves[ 51 ][ 2 ] = new int[ 6 ];
    RookMoves[ 51 ][ 3 ] = new int[ 1 ];
    RookMoves[ 51 ][ 0 ][ 0 ] = 50;
    RookMoves[ 51 ][ 0 ][ 1 ] = 49;
    RookMoves[ 51 ][ 0 ][ 2 ] = 48;
    RookMoves[ 51 ][ 1 ][ 0 ] = 52;
    RookMoves[ 51 ][ 1 ][ 1 ] = 53;
    RookMoves[ 51 ][ 1 ][ 2 ] = 54;
    RookMoves[ 51 ][ 1 ][ 3 ] = 55;
    RookMoves[ 51 ][ 2 ][ 0 ] = 43;
    RookMoves[ 51 ][ 2 ][ 1 ] = 35;
    RookMoves[ 51 ][ 2 ][ 2 ] = 27;
    RookMoves[ 51 ][ 2 ][ 3 ] = 19;
    RookMoves[ 51 ][ 2 ][ 4 ] = 11;
    RookMoves[ 51 ][ 2 ][ 5 ] = 3;
    RookMoves[ 51 ][ 3 ][ 0 ] = 59;

    RookMoves[ 52 ] = new int[ 4 ][];
    RookMoves[ 52 ][ 0 ] = new int[ 4 ];
    RookMoves[ 52 ][ 1 ] = new int[ 3 ];
    RookMoves[ 52 ][ 2 ] = new int[ 6 ];
    RookMoves[ 52 ][ 3 ] = new int[ 1 ];
    RookMoves[ 52 ][ 0 ][ 0 ] = 51;
    RookMoves[ 52 ][ 0 ][ 1 ] = 50;
    RookMoves[ 52 ][ 0 ][ 2 ] = 49;
    RookMoves[ 52 ][ 0 ][ 3 ] = 48;
    RookMoves[ 52 ][ 1 ][ 0 ] = 53;
    RookMoves[ 52 ][ 1 ][ 1 ] = 54;
    RookMoves[ 52 ][ 1 ][ 2 ] = 55;
    RookMoves[ 52 ][ 2 ][ 0 ] = 44;
    RookMoves[ 52 ][ 2 ][ 1 ] = 36;
    RookMoves[ 52 ][ 2 ][ 2 ] = 28;
    RookMoves[ 52 ][ 2 ][ 3 ] = 20;
    RookMoves[ 52 ][ 2 ][ 4 ] = 12;
    RookMoves[ 52 ][ 2 ][ 5 ] = 4;
    RookMoves[ 52 ][ 3 ][ 0 ] = 60;

    RookMoves[ 53 ] = new int[ 4 ][];
    RookMoves[ 53 ][ 0 ] = new int[ 5 ];
    RookMoves[ 53 ][ 1 ] = new int[ 2 ];
    RookMoves[ 53 ][ 2 ] = new int[ 6 ];
    RookMoves[ 53 ][ 3 ] = new int[ 1 ];
    RookMoves[ 53 ][ 0 ][ 0 ] = 52;
    RookMoves[ 53 ][ 0 ][ 1 ] = 51;
    RookMoves[ 53 ][ 0 ][ 2 ] = 50;
    RookMoves[ 53 ][ 0 ][ 3 ] = 49;
    RookMoves[ 53 ][ 0 ][ 4 ] = 48;
    RookMoves[ 53 ][ 1 ][ 0 ] = 54;
    RookMoves[ 53 ][ 1 ][ 1 ] = 55;
    RookMoves[ 53 ][ 2 ][ 0 ] = 45;
    RookMoves[ 53 ][ 2 ][ 1 ] = 37;
    RookMoves[ 53 ][ 2 ][ 2 ] = 29;
    RookMoves[ 53 ][ 2 ][ 3 ] = 21;
    RookMoves[ 53 ][ 2 ][ 4 ] = 13;
    RookMoves[ 53 ][ 2 ][ 5 ] = 5;
    RookMoves[ 53 ][ 3 ][ 0 ] = 61;

    RookMoves[ 54 ] = new int[ 4 ][];
    RookMoves[ 54 ][ 0 ] = new int[ 6 ];
    RookMoves[ 54 ][ 1 ] = new int[ 1 ];
    RookMoves[ 54 ][ 2 ] = new int[ 6 ];
    RookMoves[ 54 ][ 3 ] = new int[ 1 ];
    RookMoves[ 54 ][ 0 ][ 0 ] = 53;
    RookMoves[ 54 ][ 0 ][ 1 ] = 52;
    RookMoves[ 54 ][ 0 ][ 2 ] = 51;
    RookMoves[ 54 ][ 0 ][ 3 ] = 50;
    RookMoves[ 54 ][ 0 ][ 4 ] = 49;
    RookMoves[ 54 ][ 0 ][ 5 ] = 48;
    RookMoves[ 54 ][ 1 ][ 0 ] = 55;
    RookMoves[ 54 ][ 2 ][ 0 ] = 46;
    RookMoves[ 54 ][ 2 ][ 1 ] = 38;
    RookMoves[ 54 ][ 2 ][ 2 ] = 30;
    RookMoves[ 54 ][ 2 ][ 3 ] = 22;
    RookMoves[ 54 ][ 2 ][ 4 ] = 14;
    RookMoves[ 54 ][ 2 ][ 5 ] = 6;
    RookMoves[ 54 ][ 3 ][ 0 ] = 62;

    RookMoves[ 55 ] = new int[ 3 ][];
    RookMoves[ 55 ][ 0 ] = new int[ 7 ];
    RookMoves[ 55 ][ 1 ] = new int[ 6 ];
    RookMoves[ 55 ][ 2 ] = new int[ 1 ];
    RookMoves[ 55 ][ 0 ][ 0 ] = 54;
    RookMoves[ 55 ][ 0 ][ 1 ] = 53;
    RookMoves[ 55 ][ 0 ][ 2 ] = 52;
    RookMoves[ 55 ][ 0 ][ 3 ] = 51;
    RookMoves[ 55 ][ 0 ][ 4 ] = 50;
    RookMoves[ 55 ][ 0 ][ 5 ] = 49;
    RookMoves[ 55 ][ 0 ][ 6 ] = 48;
    RookMoves[ 55 ][ 1 ][ 0 ] = 47;
    RookMoves[ 55 ][ 1 ][ 1 ] = 39;
    RookMoves[ 55 ][ 1 ][ 2 ] = 31;
    RookMoves[ 55 ][ 1 ][ 3 ] = 23;
    RookMoves[ 55 ][ 1 ][ 4 ] = 15;
    RookMoves[ 55 ][ 1 ][ 5 ] = 7;
    RookMoves[ 55 ][ 2 ][ 0 ] = 63;

    RookMoves[ 56 ] = new int[ 2 ][];
    RookMoves[ 56 ][ 0 ] = new int[ 7 ];
    RookMoves[ 56 ][ 1 ] = new int[ 7 ];
    RookMoves[ 56 ][ 0 ][ 0 ] = 57;
    RookMoves[ 56 ][ 0 ][ 1 ] = 58;
    RookMoves[ 56 ][ 0 ][ 2 ] = 59;
    RookMoves[ 56 ][ 0 ][ 3 ] = 60;
    RookMoves[ 56 ][ 0 ][ 4 ] = 61;
    RookMoves[ 56 ][ 0 ][ 5 ] = 62;
    RookMoves[ 56 ][ 0 ][ 6 ] = 63;
    RookMoves[ 56 ][ 1 ][ 0 ] = 48;
    RookMoves[ 56 ][ 1 ][ 1 ] = 40;
    RookMoves[ 56 ][ 1 ][ 2 ] = 32;
    RookMoves[ 56 ][ 1 ][ 3 ] = 24;
    RookMoves[ 56 ][ 1 ][ 4 ] = 16;
    RookMoves[ 56 ][ 1 ][ 5 ] = 8;
    RookMoves[ 56 ][ 1 ][ 6 ] = 0;

    RookMoves[ 57 ] = new int[ 3 ][];
    RookMoves[ 57 ][ 0 ] = new int[ 1 ];
    RookMoves[ 57 ][ 1 ] = new int[ 6 ];
    RookMoves[ 57 ][ 2 ] = new int[ 7 ];
    RookMoves[ 57 ][ 0 ][ 0 ] = 56;
    RookMoves[ 57 ][ 1 ][ 0 ] = 58;
    RookMoves[ 57 ][ 1 ][ 1 ] = 59;
    RookMoves[ 57 ][ 1 ][ 2 ] = 60;
    RookMoves[ 57 ][ 1 ][ 3 ] = 61;
    RookMoves[ 57 ][ 1 ][ 4 ] = 62;
    RookMoves[ 57 ][ 1 ][ 5 ] = 63;
    RookMoves[ 57 ][ 2 ][ 0 ] = 49;
    RookMoves[ 57 ][ 2 ][ 1 ] = 41;
    RookMoves[ 57 ][ 2 ][ 2 ] = 33;
    RookMoves[ 57 ][ 2 ][ 3 ] = 25;
    RookMoves[ 57 ][ 2 ][ 4 ] = 17;
    RookMoves[ 57 ][ 2 ][ 5 ] = 9;
    RookMoves[ 57 ][ 2 ][ 6 ] = 1;

    RookMoves[ 58 ] = new int[ 3 ][];
    RookMoves[ 58 ][ 0 ] = new int[ 2 ];
    RookMoves[ 58 ][ 1 ] = new int[ 5 ];
    RookMoves[ 58 ][ 2 ] = new int[ 7 ];
    RookMoves[ 58 ][ 0 ][ 0 ] = 57;
    RookMoves[ 58 ][ 0 ][ 1 ] = 56;
    RookMoves[ 58 ][ 1 ][ 0 ] = 59;
    RookMoves[ 58 ][ 1 ][ 1 ] = 60;
    RookMoves[ 58 ][ 1 ][ 2 ] = 61;
    RookMoves[ 58 ][ 1 ][ 3 ] = 62;
    RookMoves[ 58 ][ 1 ][ 4 ] = 63;
    RookMoves[ 58 ][ 2 ][ 0 ] = 50;
    RookMoves[ 58 ][ 2 ][ 1 ] = 42;
    RookMoves[ 58 ][ 2 ][ 2 ] = 34;
    RookMoves[ 58 ][ 2 ][ 3 ] = 26;
    RookMoves[ 58 ][ 2 ][ 4 ] = 18;
    RookMoves[ 58 ][ 2 ][ 5 ] = 10;
    RookMoves[ 58 ][ 2 ][ 6 ] = 2;

    RookMoves[ 59 ] = new int[ 3 ][];
    RookMoves[ 59 ][ 0 ] = new int[ 3 ];
    RookMoves[ 59 ][ 1 ] = new int[ 4 ];
    RookMoves[ 59 ][ 2 ] = new int[ 7 ];
    RookMoves[ 59 ][ 0 ][ 0 ] = 58;
    RookMoves[ 59 ][ 0 ][ 1 ] = 57;
    RookMoves[ 59 ][ 0 ][ 2 ] = 56;
    RookMoves[ 59 ][ 1 ][ 0 ] = 60;
    RookMoves[ 59 ][ 1 ][ 1 ] = 61;
    RookMoves[ 59 ][ 1 ][ 2 ] = 62;
    RookMoves[ 59 ][ 1 ][ 3 ] = 63;
    RookMoves[ 59 ][ 2 ][ 0 ] = 51;
    RookMoves[ 59 ][ 2 ][ 1 ] = 43;
    RookMoves[ 59 ][ 2 ][ 2 ] = 35;
    RookMoves[ 59 ][ 2 ][ 3 ] = 27;
    RookMoves[ 59 ][ 2 ][ 4 ] = 19;
    RookMoves[ 59 ][ 2 ][ 5 ] = 11;
    RookMoves[ 59 ][ 2 ][ 6 ] = 3;

    RookMoves[ 60 ] = new int[ 3 ][];
    RookMoves[ 60 ][ 0 ] = new int[ 4 ];
    RookMoves[ 60 ][ 1 ] = new int[ 3 ];
    RookMoves[ 60 ][ 2 ] = new int[ 7 ];
    RookMoves[ 60 ][ 0 ][ 0 ] = 59;
    RookMoves[ 60 ][ 0 ][ 1 ] = 58;
    RookMoves[ 60 ][ 0 ][ 2 ] = 57;
    RookMoves[ 60 ][ 0 ][ 3 ] = 56;
    RookMoves[ 60 ][ 1 ][ 0 ] = 61;
    RookMoves[ 60 ][ 1 ][ 1 ] = 62;
    RookMoves[ 60 ][ 1 ][ 2 ] = 63;
    RookMoves[ 60 ][ 2 ][ 0 ] = 52;
    RookMoves[ 60 ][ 2 ][ 1 ] = 44;
    RookMoves[ 60 ][ 2 ][ 2 ] = 36;
    RookMoves[ 60 ][ 2 ][ 3 ] = 28;
    RookMoves[ 60 ][ 2 ][ 4 ] = 20;
    RookMoves[ 60 ][ 2 ][ 5 ] = 12;
    RookMoves[ 60 ][ 2 ][ 6 ] = 4;

    RookMoves[ 61 ] = new int[ 3 ][];
    RookMoves[ 61 ][ 0 ] = new int[ 5 ];
    RookMoves[ 61 ][ 1 ] = new int[ 2 ];
    RookMoves[ 61 ][ 2 ] = new int[ 7 ];
    RookMoves[ 61 ][ 0 ][ 0 ] = 60;
    RookMoves[ 61 ][ 0 ][ 1 ] = 59;
    RookMoves[ 61 ][ 0 ][ 2 ] = 58;
    RookMoves[ 61 ][ 0 ][ 3 ] = 57;
    RookMoves[ 61 ][ 0 ][ 4 ] = 56;
    RookMoves[ 61 ][ 1 ][ 0 ] = 62;
    RookMoves[ 61 ][ 1 ][ 1 ] = 63;
    RookMoves[ 61 ][ 2 ][ 0 ] = 53;
    RookMoves[ 61 ][ 2 ][ 1 ] = 45;
    RookMoves[ 61 ][ 2 ][ 2 ] = 37;
    RookMoves[ 61 ][ 2 ][ 3 ] = 29;
    RookMoves[ 61 ][ 2 ][ 4 ] = 21;
    RookMoves[ 61 ][ 2 ][ 5 ] = 13;
    RookMoves[ 61 ][ 2 ][ 6 ] = 5;

    RookMoves[ 62 ] = new int[ 3 ][];
    RookMoves[ 62 ][ 0 ] = new int[ 6 ];
    RookMoves[ 62 ][ 1 ] = new int[ 1 ];
    RookMoves[ 62 ][ 2 ] = new int[ 7 ];
    RookMoves[ 62 ][ 0 ][ 0 ] = 61;
    RookMoves[ 62 ][ 0 ][ 1 ] = 60;
    RookMoves[ 62 ][ 0 ][ 2 ] = 59;
    RookMoves[ 62 ][ 0 ][ 3 ] = 58;
    RookMoves[ 62 ][ 0 ][ 4 ] = 57;
    RookMoves[ 62 ][ 0 ][ 5 ] = 56;
    RookMoves[ 62 ][ 1 ][ 0 ] = 63;
    RookMoves[ 62 ][ 2 ][ 0 ] = 54;
    RookMoves[ 62 ][ 2 ][ 1 ] = 46;
    RookMoves[ 62 ][ 2 ][ 2 ] = 38;
    RookMoves[ 62 ][ 2 ][ 3 ] = 30;
    RookMoves[ 62 ][ 2 ][ 4 ] = 22;
    RookMoves[ 62 ][ 2 ][ 5 ] = 14;
    RookMoves[ 62 ][ 2 ][ 6 ] = 6;

    RookMoves[ 63 ] = new int[ 2 ][];
    RookMoves[ 63 ][ 0 ] = new int[ 7 ];
    RookMoves[ 63 ][ 1 ] = new int[ 7 ];
    RookMoves[ 63 ][ 0 ][ 0 ] = 62;
    RookMoves[ 63 ][ 0 ][ 1 ] = 61;
    RookMoves[ 63 ][ 0 ][ 2 ] = 60;
    RookMoves[ 63 ][ 0 ][ 3 ] = 59;
    RookMoves[ 63 ][ 0 ][ 4 ] = 58;
    RookMoves[ 63 ][ 0 ][ 5 ] = 57;
    RookMoves[ 63 ][ 0 ][ 6 ] = 56;
    RookMoves[ 63 ][ 1 ][ 0 ] = 55;
    RookMoves[ 63 ][ 1 ][ 1 ] = 47;
    RookMoves[ 63 ][ 1 ][ 2 ] = 39;
    RookMoves[ 63 ][ 1 ][ 3 ] = 31;
    RookMoves[ 63 ][ 1 ][ 4 ] = 23;
    RookMoves[ 63 ][ 1 ][ 5 ] = 15;
    RookMoves[ 63 ][ 1 ][ 6 ] = 7;




  }
}