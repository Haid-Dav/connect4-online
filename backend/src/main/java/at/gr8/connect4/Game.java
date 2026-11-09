package at.gr8.connect4;
import java.util.*;
/** Pure rules, independent of network and persistence. Rows run top to bottom. */
public class Game {
 public int[][] board = new int[6][7];
 public int turn=1, winner=0, moves=0;
 public List<int[]> winningCells = new ArrayList<>();
 public boolean finished() { return winner!=0 || moves==42; }
 public void move(int player,int column) {
  if(finished()) throw new IllegalArgumentException("Die Runde ist bereits beendet.");
  if(player!=turn) throw new IllegalArgumentException("Du bist noch nicht am Zug.");
  if(column<0 || column>6) throw new IllegalArgumentException("Ungültige Spalte.");
  int row=5; while(row>=0 && board[row][column]!=0) row--;
  if(row<0) throw new IllegalArgumentException("Diese Spalte ist voll.");
  board[row][column]=player; moves++;
  for(int[] d:new int[][]{{0,1},{1,0},{1,1},{1,-1}}) {
   List<int[]> line=new ArrayList<>(); line.add(new int[]{row,column});
   for(int sign:new int[]{-1,1}) { int r=row+sign*d[0],c=column+sign*d[1];
    while(r>=0 && r<6 && c>=0 && c<7 && board[r][c]==player) {line.add(new int[]{r,c}); r+=sign*d[0]; c+=sign*d[1];}
   }
   if(line.size()>=4) {winner=player; winningCells=line; break;}
  }
  if(!finished()) turn=3-turn;
 }
}
