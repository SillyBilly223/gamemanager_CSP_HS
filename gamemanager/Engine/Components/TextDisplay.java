package funkwitzs.gamemanager.Engine.Components;

import funkwitzs.gamemanager.Engine.EngineManager;
import funkwitzs.gamemanager.Engine.RenderObjects.TextRenderer;

public class TextDisplay extends Component implements Transformable  {

  private TextRenderer textDisplay;

  @Override
  public void awake() {
    textDisplay = new TextRenderer();

    textDisplay.position = gameObject.transform.position.toVector2();

    addTextToRenderer();
  }

  @Override
  public void start() {

  }

  public String text() {
    return textDisplay.text;
  }
  public void text(String text) {
    textDisplay.text = text;
  }

  public int layer() {
    return textDisplay.layer;
  }
  public void layer(int layer) {
    textDisplay.layer = layer;
    refreashTextRenderer();
  }

  public void refreashTextRenderer() {
    EngineManager._inst.renderer.addRenderObject(textDisplay, textDisplay.layer);
    EngineManager._inst.renderer.removeRenderObject(textDisplay);
  }

  public void addTextToRenderer() {
    EngineManager._inst.renderer.addRenderObject(textDisplay, textDisplay.layer);
  }

  public void removeTextFromRenderer() {
    EngineManager._inst.renderer.removeRenderObject(textDisplay);
  }

  @Override
  public void update() {
  }

  @Override
  public void deinit() {
  }

  @Override
  public void onTransformChange() {
    textDisplay.position = gameObject.transform.position.toVector2();
  }

}
