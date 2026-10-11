package funkwitzs.gamemanager.Engine;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.view.MotionEvent;
import funkwitzs.gamemanager.Engine.Components.Component;
import funkwitzs.gamemanager.Engine.Components.Interactable;
import funkwitzs.gamemanager.Engine.Components.Transformable;
import funkwitzs.gamemanager.Engine.Debug.Debuggable;
import funkwitzs.gamemanager.Engine.Math.Transform;
import funkwitzs.gamemanager.Engine.Math.Vector2;
import funkwitzs.gamemanager.Engine.Math.Vector3;
import funkwitzs.gamemanager.Engine.RenderObjects.SpriteRenderer;

public class GameObject {

  @Debuggable
  public String name;
  public GameObject parent = null;

  public ArrayList<GameObject> children = new ArrayList<GameObject>();

  public Transform transform = Transform.zero();

  public SpriteRenderer sprite = null;
  @Debuggable
  public boolean enabled = true;

  public ArrayList<Component> components = new ArrayList<Component>();

  public GameObject(String name) {
    this.name = name;
  }

  //#region Initilizers
  public void initilize() {
    for (Component comp : components) comp.start();
    for (Component comp : components) comp.awake();
  }
  public void deinitilize() {
    for (int i = 0; i < components.size(); i++) components.get(i).deinit();
  }
  //#endregion Initilizers

  //#region Setters

  public void enabled(boolean bool) {
    enabled = bool;
  }

  /** gets transform position */
  public Vector3 position() { return transform.position; }

  /** changes transform position, triggers <code>transformChange</code> */
  public void position(Vector3 vec3) {
    transform.position.copy(vec3); transformChange();
  }

  @Debuggable
  /** changes transform position, triggers <code>transformChange</code> */
  public void position(float x, float y, float z) {
    transform.position.set(x,y,z); transformChange();
  }

  /** gets transform scale */
  public Vector2 scale() { return transform.scale; }

  /** changes transform scale, triggers <code>transformChange</code> */
  public void scale(Vector2 vec2) {
    transform.scale.copy(vec2); transformChange();
  }

  @Debuggable
  /** changes transform scale, triggers <code>transformChange</code> */
  public void scale(float x, float y) {
    transform.scale.set(x, y); transformChange();
  }

  public SpriteRenderer sprite() {
    return sprite;
  }
  public void sprite(SpriteRenderer spr) {
    if (sprite != null && sprite != spr) {
      sprite.gameObject = null;
      removeSpriteFromRenderer();
    }

    if (spr == null) return;
    sprite = spr;
    sprite.gameObject = this;
    sprite.setByTransform(transform);
    addSpriteToRenderer();
  }

  //#endregion Setters

  //#region LoopLogic
  public void update() {
    for (int i = 0; i < components.size(); i++) {
      components.get(i).update();
    }
  }
  public void draw(Canvas canvas) {
    if (sprite == null) return;
    //sprite.drawToCanvas(canvas);
  }
  //#endregion LoopLogic

  //#region Misc
  public void onUserTouch(Vector2 point, int action) {
    for (Component comp : components) {
      if (comp instanceof Interactable) {
        Interactable inter = (Interactable)comp;
        switch (action) {
          case MotionEvent.ACTION_DOWN : inter.onDown(point); break;
          case MotionEvent.ACTION_UP : inter.onUp(point); break;
          case MotionEvent.ACTION_MOVE : inter.onDrag(point); break;
        }
      }
    }
  }
  public void addSpriteToRenderer() {
    EngineManager._inst.renderer.addRenderObject(sprite, sprite.layer);
  }
  public void removeSpriteFromRenderer() {
    EngineManager._inst.renderer.removeRenderObject(sprite);
  }
  //#endregion Misc

  //#region Transform
  public void transformChange() {
    if (sprite != null) sprite.setByTransform(transform);

    for (Component comp : components) {
      if (comp instanceof Transformable) {
        ((Transformable)comp).onTransformChange();
      }
    }
  }
  //#endregion Transform

  //#region ComponentLogic
  public void insertComponent(Component comp) {
    comp.gameObject = this; components.add(comp);
  }
  public void addComponent(Component comp) {
    if (components.contains(comp)) return;
    comp.gameObject = this; components.add(comp);
    comp.awake(); comp.start();///
  }
  public void removeComponent(Component comp) {
    if (!components.contains(comp)) return;
    comp.deinit();///
    comp.gameObject = null; components.remove(comp);
  }
  //#endregion ComponentLogic

  //#region SceneRequests
  public void Delete(GameObject gameObject) {
    gameObject.enabled = false;
    EngineManager._inst.scene.removeGameObject(gameObject);
  }
  //#endregion SceneRequests

  //#region Debug
  @Debuggable
  private String logSprite() {
    if (sprite == null) {
      return "Null Sprite.";
    }
    return sprite.toString();
  }
  //#endregion Debug
}
