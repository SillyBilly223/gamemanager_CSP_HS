package funkwitzs.gamemanager.Engine.Math;

public class Vector2 {

  public float x; public float y;

  public Vector2(float x, float y) { this.x=x;this.y=y; }

  public void set(float x, float y) { this.x=x;this.y=y; }
  public void copy(Vector2 vec) { this.x=vec.x;this.y=vec.y; }

  public Vector2 add(float x, float y) { return new Vector2(this.x+x,this.y+y); }
  public Vector2 sub(float x, float y) { return new Vector2(this.x-x,this.y-y); }
  public Vector2 div(float x, float y) { return new Vector2(this.x/x,this.y/y); }
  public Vector2 mult(float x, float y) { return new Vector2(this.x*x,this.y*y); }

  public Vector2 add(Vector2 vec) { return new Vector2(this.x+vec.x,this.y+vec.y); }
  public Vector2 sub(Vector2 vec) { return new Vector2(this.x-vec.x,this.y-vec.y); }
  public Vector2 div(Vector2 vec) { return new Vector2(this.x/vec.x,this.y/vec.y); }
  public Vector2 mult(Vector2 vec) { return new Vector2(this.x*vec.x,this.y*vec.y); }

  public void addLocal(float x, float y) { this.x += x; this.y += y; }
  public void subLocal(float x, float y) { this.x -= x; this.y -= y; }
  public void divLocal(float x, float y) { this.x /= x; this.y /= y; }
  public void multLocal(float x, float y) { this.x *= x; this.y *= y; }

  public void addLocal(Vector2 vec) { this.x += vec.x; this.y += vec.y; }
  public void subLocal(Vector2 vec) { this.x -= vec.x; this.y -= vec.y; }
  public void divLocal(Vector2 vec) { this.x /= vec.x; this.y /= vec.y; }
  public void multLocal(Vector2 vec) { this.x *= vec.x; this.y *= vec.y; }

  @Override
  public String toString() {
    return x + " " + y;
  }
}
