package funkwitzs.gamemanager.Engine;

import com.google.appinventor.components.runtime.ComponentContainer;

import android.graphics.Canvas;
import android.graphics.Color;

public class EngineManager implements Runnable {

  ///GLOBAL
  public static EngineManager _inst;
  ///GAMELOOP
  private Thread Thread;
  private boolean Running = false;
  public static final String THREADNAME = "EngineThread";
  ///
  public EngineComponent component;
  public EngineRenderer renderer;
  public SceneManager scene = new SceneManager();
  public InputManager inputs = new InputManager();

  public EngineManager(ComponentContainer container) {
    _inst = this;

    component = new EngineComponent(container);
    renderer = component.getRenderer();

    init();
  }

  public void init() {
    if (Running) return;

    Resources.Initilize();
    Debug.Initilize();

    Running = true;
    Thread = new Thread(this, THREADNAME); Thread.start();
  }

  @Override
  public void run() {
    GameObject gObj = new GameObject("TestObject");
    gObj.position(30,30,0);
    gObj.sprite(Resources.GetSprite("Card_TestCard.png"));
    gObj.enabled = true;
    scene.addGameObject(gObj);

    renderer = component.getRenderer();
    while (Running) {

      scene.processFlags();
      scene.updateScene();

      draw(renderer);

      try { Thread.sleep(16); } catch (InterruptedException e) {}
    }
  }

  public void draw(EngineRenderer r) {
    if (!r.holder.getSurface().isValid()) return;
    Canvas canvas = r.holder.lockCanvas();
    canvas.drawColor(Color.BLACK);

    r.drawRenderObjects(canvas);

    Debug.drawGameObjects(canvas, scene.gameObjects_Scene);
    Debug.draw(canvas);
    r.holder.unlockCanvasAndPost(canvas);
  }

  public void deinit() {
    if (!Running) return;

    ///

    Running = false;
    try { Thread.join(); } catch (InterruptedException e) {}
  }
}
