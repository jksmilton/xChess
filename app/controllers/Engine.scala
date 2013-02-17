package controllers

class Engine {
    val WHITE = 0
    val BLACK = 1
    val PAWN = 0
    val ROOK = 6
    val KNIGHT = 2
    val BISHOP = 4
    val QUEEN = 8
    val KING = 10
    
    val WHITE_PAWN = WHITE + PAWN
    val WHITE_ROOK = WHITE + ROOK
    val WHITE_KNIGHT = WHITE + KNIGHT
    val WHITE_BISHOP = WHITE + BISHOP
    val WHITE_QUEEN = WHITE + QUEEN
    val WHITE_KING = WHITE + KING
    
    val BLACK_PAWN = BLACK + PAWN
    val BLACK_ROOK = BLACK + ROOK
    val BLACK_KNIGHT = BLACK + KNIGHT
    val BLACK_BISHOP = BLACK + BISHOP
    val BLACK_QUEEN = BLACK + QUEEN
    val BLACK_KING = BLACK + KING
    
    val EMPTY_SQUARE = 12

    val ALL_PIECES = 12
    val ALL_SQUARES = 64
    
    val ALL_WHITE_PIECES = ALL_PIECES + WHITE
    val ALL_BLACK_PIECES = ALL_PIECES + BLACK
    
    val ALL_BITBOARDS = 14
    
    
    def this(transcript : List[String]) = {
        this
        
        
        
    }
    
}