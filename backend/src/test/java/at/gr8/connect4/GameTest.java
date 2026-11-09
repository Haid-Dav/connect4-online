package at.gr8.connect4;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GameTest {
 @Test void gravityAndTurns(){Game g=new Game();g.move(1,3);assertEquals(1,g.board[5][3]);assertEquals(2,g.turn);assertThrows(IllegalArgumentException.class,()->g.move(1,2));assertEquals(1,g.moves);}
 @Test void verticalWin(){Game g=play(0,1,0,1,0,1,0);assertEquals(1,g.winner);assertEquals(4,g.winningCells.size());assertThrows(IllegalArgumentException.class,()->g.move(2,2));}
 @Test void horizontalWin(){assertEquals(1,play(0,0,1,1,2,2,3).winner);}
 @Test void diagonalWin(){assertEquals(1,play(0,1,1,2,4,2,2,3,4,3,5,3,3).winner);}
 @Test void mirroredDiagonalWin(){assertEquals(1,play(6,5,5,4,2,4,4,3,2,3,1,3,3).winner);}
 @Test void fullAndInvalidColumns(){Game g=play(0,0,0,0,0,0);assertThrows(IllegalArgumentException.class,()->g.move(1,0));assertThrows(IllegalArgumentException.class,()->g.move(1,-1));assertThrows(IllegalArgumentException.class,()->g.move(1,7));assertEquals(6,g.moves);}
 @Test void draw(){Game g=new Game();int[][] pattern={{1,1,2,2,1,1,2},{2,2,1,1,2,2,1},{1,1,2,2,1,1,2},{2,2,1,1,2,2,1},{1,1,2,2,1,1,2},{2,2,1,1,2,2,0}};g.board=pattern;g.moves=41;g.turn=1;g.move(1,6);assertTrue(g.finished());assertEquals(0,g.winner);}
 private Game play(int...columns){Game g=new Game();for(int c:columns)g.move(g.turn,c);return g;}
}
