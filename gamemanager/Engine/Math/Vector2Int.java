package funkwitzs.gamemanager.Engine.Math;

public class Vector2Int {
  public int X; public int Y;

  public Vector2Int(int x, int y) { X=x;Y=y; }
  public static Vector2Int zero() {return new Vector2Int(0,0);}

  public void Set(int x, int y) { X=x;Y=y; }
  public void Add(int x, int y) { X+=x;Y+=y; }
  public void Sub(int x, int y) { X-=x;Y-=y; }
}
