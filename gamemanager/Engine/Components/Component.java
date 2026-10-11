
package funkwitzs.gamemanager.Engine.Components;

import funkwitzs.gamemanager.Engine.GameObject;

public abstract class Component {
  public int type;
  public GameObject gameObject;

  public abstract void awake();
  public abstract void start();

  public abstract void update();
  public abstract void deinit();

  public static enum CompTypes {

  }
}
