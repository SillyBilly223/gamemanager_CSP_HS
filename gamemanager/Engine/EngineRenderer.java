package funkwitzs.gamemanager.Engine;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import funkwitzs.gamemanager.Engine.Debug.Debuggable;
import funkwitzs.gamemanager.Engine.Math.Vector2;
import funkwitzs.gamemanager.Engine.RenderObjects.RenderObject;

public class EngineRenderer extends SurfaceView {

  public SurfaceHolder holder;
  public RenderObject touchingObject = null;

  public ArrayList<RenderObject> renderObjects = new ArrayList<>();
  public ArrayList<Layer> canvasLayers = new ArrayList<>();
  public ArrayList<Layer> debugLayers = new ArrayList<>(3);

  public Vector2 screenSize = new Vector2(0,0);

  public EngineRenderer(Context context) {
    super(context);
    holder = getHolder();
    setOnTouchListener(EngineManager._inst.inputs);
    setOnKeyListener(EngineManager._inst.inputs);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();

    screenSize = new Vector2(getWidth(), getHeight());
  }

  public boolean addRenderObject(RenderObject rObj, int layer) {
    if (renderObjects.contains(rObj)) return false;
    renderObjects.add(rObj);
    getLayer(layer).renderObjects.add(rObj);
    return true;
  }

  public boolean removeRenderObject(RenderObject rObj) {
    if (!renderObjects.contains(rObj)) return false;
    renderObjects.remove(rObj);
    getLayer(rObj.layer).renderObjects.remove(rObj);
    return true;
  }

  public Layer getLayer(int layerID) {
    for (Layer layer : canvasLayers) if (layer.id == layerID) return layer;
    Layer layer = new Layer(layerID);
    canvasLayers.add(layer);
    canvasLayers.sort(Comparator.comparingInt(l -> l.id));
    return layer;
  }

  public void drawRenderObjects(Canvas canvas) {
    for (Layer layer : canvasLayers) {
      for (RenderObject rObj : layer.renderObjects) {
        if (!rObj.enabled) continue;
        rObj.drawToCanvas(canvas);
      }
    }
  }

  public void interactRenderObjects(Vector2 point, int action) {

    if (action == MotionEvent.ACTION_UP) {
      if (touchingObject == null) return;
      touchingObject.processTouch(point, MotionEvent.ACTION_UP);
      touchingObject = null;
      return;
    }

    for (int i = canvasLayers.size() - 1; i >= 0; i--) {
      Layer layer = canvasLayers.get(i);
      for (int j = layer.renderObjects.size() - 1; j >= 0; j--) {
        RenderObject rObj = layer.renderObjects.get(j);
        if (!rObj.enabled || !rObj.interactable) continue;

        if (rObj.contains(point)) {
          processTouchRenderObject(rObj, point, action);
        }
        return;
      }
    }
  }

  public void processTouchRenderObject(RenderObject rObj, Vector2 point, int action) {
    if (touchingObject == null) {
      touchingObject = rObj;
      rObj.processTouch(point, MotionEvent.ACTION_DOWN);
    }

    if (touchingObject != rObj) {
      touchingObject.processTouch(point, MotionEvent.ACTION_UP);
      touchingObject = rObj;
      rObj.processTouch(point, MotionEvent.ACTION_DOWN);
    }

    if (action == MotionEvent.ACTION_MOVE) {
      touchingObject.processTouch(point, MotionEvent.ACTION_MOVE);
    }
  }

  public class Layer {
    public int id = 0;
    public ArrayList<RenderObject> renderObjects = new ArrayList<>();
    public Layer(int id) { this.id = id; }
  }

  //#region Debug
  @Debuggable
  private int getRenderObjectCount() {
    return renderObjects.size();
  }
  @Debuggable
  private int getLayerCount() {
    return canvasLayers.size();
  }
  //#endregion Debug

}
