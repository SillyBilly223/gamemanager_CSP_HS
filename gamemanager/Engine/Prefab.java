package funkwitzs.gamemanager.Engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Rect;
import funkwitzs.gamemanager.Engine.Components.Component;
import funkwitzs.gamemanager.Engine.Math.Transform;
import funkwitzs.gamemanager.Engine.Math.Vector2;
import funkwitzs.gamemanager.Engine.RenderObjects.Sprite;

public class Prefab {

  public Transform transform = Transform.zero();
  public SprRef sprite = null;
  public ArrayList<CompRef> components = new ArrayList<CompRef>();
  public boolean enabled = false;

  public static class SprRef {
    public Bitmap bitMap;
    public Rect src;
    public Vector2 scale;
    public SprRef(Bitmap bitMap, Rect src, Vector2 scale) {
      this.bitMap = bitMap; this.src = src; this.scale = scale;
    }
  }
  public static class CompRef {
    public String type;
    public Object[][] vars; // var[] {name,data}
    public CompRef(String type) {
      this.type = type;
    }
  }

  public GameObject createGameObject() {
    GameObject gameObject = new GameObject("GameObject");
    gameObject.transform = transform;

    if (sprite != null) gameObject.sprite = createSprite(sprite);

    for (CompRef ref : components) {
      Component comp = createComponent(ref);
      if (comp == null) return null;
      gameObject.insertComponent(comp);
    }

    gameObject.enabled = enabled;

    return gameObject;
  }
  public Sprite createSprite(SprRef spr) {
    return null;
  }
  public Component createComponent(CompRef comp) {
    Component component = null;
    try {
      Class<?> compClass = Class.forName(comp.type);
      Constructor<?> constructor = compClass.getDeclaredConstructor();
      component = (Component)constructor.newInstance();
    } catch (ClassNotFoundException e) {
      Debug.log("Component not found: " + comp.type);
      Debug.logStackTrace(e.getStackTrace());
    } catch (NoSuchMethodException e) {
      Debug.log("No suitable constructor found for: " + comp.type);
      Debug.logStackTrace(e.getStackTrace());
    } catch (Exception e) {
      Debug.log("Error creating instance: " + e.getMessage());
      Debug.logStackTrace(e.getStackTrace());
    }

    if (component == null) return null;

    try {
        for (Object[] var : comp.vars) {
            Field field = component.getClass().getDeclaredField((String)var[0]);
            field.setAccessible(true); field.set(component, var[1]);
        }
    } catch (Exception e) {
        Debug.log("Error setting fields for: " + comp.type);
        Debug.logStackTrace(e.getStackTrace());
        return null;
    }

    return component;
  }

  public void loadComponent() {

  }
}
