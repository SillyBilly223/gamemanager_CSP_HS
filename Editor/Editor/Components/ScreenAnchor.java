package funkwitzs.gamemanager.Engine.Components;
import funkwitzs.gamemanager.Engine.EngineManager;
import funkwitzs.gamemanager.Engine.Math.Vector2;

public class ScreenAnchor extends Component implements Transformable {

  public float xAnchor;
  public float yAnchor;

  private Vector2 screenSize;

  @Override
  public void awake() {
    screenSize = EngineManager._inst.renderer.screenSize.clone();
    layout();
  }

  @Override
  public void start() {
  }

  @Override
  public void update() {
  }

  @Override
  public void deinit() {
  }

  private void layout() {
    float px = Math.min(Math.max((xAnchor / 100f) * screenSize.x, 0), screenSize.x);
    float py = Math.min(Math.max((yAnchor / 100f) * screenSize.y, 0), screenSize.y);

    gameObject.position(px, py, gameObject.transform.position.z);
  }

  @Override
  public void onEnabled() {
  }

  @Override
  public void onDisabled() {
  }

  @Override
  public void onTransformChange() {
    layout();
  }

}
