package funkwitzs.gamemanager.Engine.Components;

import android.graphics.Color;
import funkwitzs.gamemanager.Engine.EngineManager;
import funkwitzs.gamemanager.Engine.RenderObjects.TextObject;


public class TextDisplay extends Component implements Transformable  {

  public String text = "";
  public float size = 6;

  private TextObject textDisplay;

  @Override
  public void awake() {
    textDisplay = new TextObject();

    textDisplay.position = gameObject.transform.position.toVector2();
    textDisplay.paint.setColor(Color.WHITE);
    textDisplay.paint.setTextSize(size);
    textDisplay.text = text;
  }

  @Override
  public void start() {
    addTextToRenderer();
  }

  public String text() {
    return text;
  }
  public void text(String text) {
    this.text = text;
    textDisplay.text = this.text;
  }

  public float size(){
    return size;
  }
  public void size(float size){
    this.size = size;
    textDisplay.paint.setTextSize(size);
  }

  public int layer() {
    return textDisplay.layer;
  }
  public void layer(int layer) {
    if (textDisplay.layer == layer) return;

    removeTextFromRenderer();
    textDisplay.layer = layer;
    addTextToRenderer();
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

  @Override
  public void onEnabled() {
    addTextToRenderer();
  }
  @Override
  public void onDisabled() {
    removeTextFromRenderer();
  }
}
