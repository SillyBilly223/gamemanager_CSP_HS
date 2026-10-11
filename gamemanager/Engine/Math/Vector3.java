package funkwitzs.gamemanager.Engine.Math;

public class Vector3 {
  public float x; public float y; public float z;

  public Vector3(float x, float y, float z) { this.x=x;this.y=y;this.z=z; }

  public Vector3 clone() { return new Vector3(x,y,z); }

  public void set(float x, float y, float z) { this.x=x;this.y=y;this.z=z; }
  public void copy(Vector3 vec) { this.x=vec.x;this.y=vec.y;this.z=vec.z; }

  public Vector3 add(float x, float y, float z) { return new Vector3(this.x+x,this.y+y,this.z+z); }
  public Vector3 sub(float x, float y, float z) { return new Vector3(this.x-x,this.y-y,this.z-z); }
  public Vector3 div(float x, float y, float z) { return new Vector3(this.x/x,this.y/y,this.z/z); }
  public Vector3 mult(float x, float y, float z) { return new Vector3(this.x*x,this.y*y,this.z*z); }

  public Vector3 add(Vector3 vec) { return new Vector3(this.x+vec.x,this.y+vec.y,this.z+vec.z); }
  public Vector3 sub(Vector3 vec) { return new Vector3(this.x-vec.x,this.y-vec.y,this.z-vec.z); }
  public Vector3 div(Vector3 vec) { return new Vector3(this.x/vec.x,this.y/vec.y,this.z/vec.z); }
  public Vector3 mult(Vector3 vec) { return new Vector3(this.x*vec.x,this.y*vec.y,this.z*vec.z); }

  public void addLocal(float x, float y, float z) { this.x += x; this.y += y; this.z += z; }
  public void subLocal(float x, float y, float z) { this.x -= x; this.y -= y; this.z -= z; }
  public void divLocal(float x, float y, float z) { this.x /= x; this.y /= y; this.z /= z; }
  public void multLocal(float x, float y, float z) { this.x *= x; this.y *= y; this.z *= z; }

  public void addLocal(Vector3 vec) { this.x += vec.x; this.y += vec.y; this.z += vec.z; }
  public void subLocal(Vector3 vec) { this.x -= vec.x; this.y -= vec.y; this.z -= vec.z; }
  public void divLocal(Vector3 vec) { this.x /= vec.x; this.y /= vec.y; this.z /= vec.z; }
  public void multLocal(Vector3 vec) { this.x *= vec.x; this.y *= vec.y; this.z *= vec.z; }

  @Override
  public String toString() {
    return x + " " + y + " " + z;
  }
}
