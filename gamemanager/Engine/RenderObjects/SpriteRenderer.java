package funkwitzs.gamemanager.Engine.RenderObjects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import funkwitzs.gamemanager.Engine.GameObject;
import funkwitzs.gamemanager.Engine.Components.Component;
import funkwitzs.gamemanager.Engine.Components.Interactable;
import funkwitzs.gamemanager.Engine.Math.Transform;
import funkwitzs.gamemanager.Engine.Math.Vector2;

public class SpriteRenderer extends RenderObject {

  public Bitmap bitmap;

  public Rect src = new Rect();
  public RectF dst = new RectF();

  public GameObject gameObject = null;
  public Component component = null;

  public SpriteRenderer(Bitmap map) {
    bitmap = map;
    Reset();
  }

  public void Reset() {
    src.set(0,0,bitmap.getWidth(),bitmap.getHeight());
    dst.set(src);
  }

  public void setSource(int l, int t, int r, int b) {
    src.set(l,t,r,b);
  }

  public void SetPosition(float x, float y) {
    float w = (dst.width()/2); float h = (dst.height()/2);
    dst.set(x-w, y-h, x+w, y+h);
  }
  public void setSize(float w, float h) {
    float x = dst.centerX(); float y = dst.centerY();
    w = w/2; h= h/2;
    dst.set(x-w,y-h, x+w,y+h);
  }

  public void setByTransform(Transform transform) {
    float w = transform.scale.x / 2;
    float h = transform.scale.y / 2;
    float x = transform.position.x;
    float y = transform.position.y;
    dst.set(x-w, y-h, x+w, y+h);
  }

  public Vector2 Positon() {
    return new Vector2(dst.centerX(), dst.centerY());
  }
  public Vector2 Scale() {
    return new Vector2(dst.width(), dst.height());
  }

  @Override
  public void drawToCanvas(Canvas canvas) {
    canvas.drawBitmap(bitmap, src, dst, null);
  }

  @Override
  public boolean contains(Vector2 point) {
    return point.x >= dst.left && point.x <= dst.right && point.y >= dst.top && point.y <= dst.bottom;
  }

  @Override
  public void processTouch(Vector2 point, int action) {
    if (gameObject != null) gameObject.onUserTouch(point, action);
    else if (component != null) {
      if (component instanceof Interactable) {
        Interactable inter = (Interactable)component;
        switch (action) {
          case MotionEvent.ACTION_DOWN : inter.onDown(point); break;
          case MotionEvent.ACTION_UP : inter.onUp(point); break;
          case MotionEvent.ACTION_MOVE : inter.onDrag(point); break;
        }
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
      "src: %s dst: %s layer: %d enabled: %b interactable: %b",
      dst.toString(), src.toString(), layer, enabled, interactable
    );
  }
}
