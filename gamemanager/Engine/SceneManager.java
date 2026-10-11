package funkwitzs.gamemanager.Engine;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Canvas;
import funkwitzs.gamemanager.Engine.Debug.Debuggable;

public class SceneManager {

  public ArrayList<GameObject> gameObjects_Scene = new ArrayList<GameObject>();
  public HashMap<String, GameObject> gameObjects_Name = new HashMap<>();
  public ArrayList<SceneFlag> flags = new ArrayList<SceneFlag>();

  public class SceneFlag {
    public FlagType type; public Object data;
    public SceneFlag(FlagType type, Object data) {
      this.type = type; this.data = data;
    }
  }
  public enum FlagType {
    GOBJ_ADD,
    GOBJ_REMOVE;
  }

  public void processFlags() {
    if (flags.size() == 0) return;
    for (int i = 0; i < flags.size(); i++) {
      SceneFlag flag = flags.get(i);
      switch(flag.type) {
        case GOBJ_ADD:
        addGameObjectToScene((GameObject)flag.data);
        break;
        case GOBJ_REMOVE:
        removeGameObjectFromScene((GameObject)flag.data);
        break;
      }
    }
    flags.clear();
  }

  public void updateScene() {
    for (GameObject gObj : gameObjects_Scene) {
      if (!gObj.enabled) continue;
      gObj.update();
    }
  }
  public void drawScene(Canvas canvas) {
    for (GameObject gObj : gameObjects_Scene) {
      if (!gObj.enabled) continue;
      gObj.draw(canvas);
    }
  }

  private void addGameObjectToScene(GameObject gObj) {
    gameObjects_Name.put(getValidGameObjectName(gObj.name), gObj);
    gameObjects_Scene.add(gObj);
    gObj.initilize();
  }
  private void removeGameObjectFromScene(GameObject gObj) {
    gObj.deinitilize();
    gameObjects_Scene.remove(gObj);
    gameObjects_Name.remove(gObj.name);
  }

  public String getValidGameObjectName(String name) {
    String validName = name;
    for (int i = 1; gameObjects_Name.containsKey(validName); i++) {
      validName = name + "_" + i;
    }
    return validName;
  }

  public void addGameObject(GameObject gObj) {
    if (gameObjects_Scene.contains(gObj)) return;
    flags.add(new SceneFlag(FlagType.GOBJ_ADD, gObj));
  }
  public void removeGameObject(GameObject gObj) {
    if (!gameObjects_Scene.contains(gObj)) return;
    flags.add(new SceneFlag(FlagType.GOBJ_REMOVE, gObj));
  }

  @Debuggable
  private boolean containsName(String name) {
    return gameObjects_Name.containsKey(name);
  }
  @Debuggable
  public GameObject getGameObjectByName(String name) {
    return gameObjects_Name.get(name);
  }

  //#region Debug
  @Debuggable
  private int getGameObjectCount() {
    return gameObjects_Scene.size();
  }
  //#endregion Debug

}
