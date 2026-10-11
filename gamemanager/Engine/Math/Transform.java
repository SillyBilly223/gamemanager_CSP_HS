package funkwitzs.gamemanager.Engine.Math;

import funkwitzs.gamemanager.Engine.GameObject;

public class Transform {

  public GameObject gameObject;

  public Vector3 position = new Vector3(0,0,0);
  //#region Position Get/Set
  public Vector3 position() { return position; }
  public void position(Vector3 vec3) { position.copy(vec3); }
  public void position(float x, float y, float z) { position.set(x, y, z); }
  //#endregion

  public Vector2 scale = new Vector2(1, 1);
  //#region Scale Get/Set
  public Vector2 scale() { return scale; }
  public void scale(Vector2 vec2) { scale.copy(vec2); }
  public void scale(float w, float h) { scale.set(w, h); }
  //#endregion

  public Transform() {}
  public Transform(Vector3 vec3, float w, float h) { position.copy(vec3); scale.set(w, h); }

  public static Transform zero() { return new Transform(new Vector3(0, 0, 0), 0, 0); }

  public void Set(float x, float y, float z, float w, float h) { position.set(x,y,z); scale.set(w, h); }
  public void copy(Transform trans) { position.copy(trans.position); scale.copy(trans.scale); }

  @Override
  public String toString() {
    return "Position: " + position.toString() + " Scale: " + scale.toString();
  }
}
