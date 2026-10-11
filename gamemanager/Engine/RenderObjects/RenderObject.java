package funkwitzs.gamemanager.Engine.RenderObjects;

import android.graphics.Canvas;
import funkwitzs.gamemanager.Engine.Math.Vector2;

  public abstract class RenderObject {

    public int layer = 0;

    public boolean enabled = true;
    public boolean interactable = true;

    public abstract void drawToCanvas(Canvas canvas);

    public abstract boolean contains(Vector2 point);

    public abstract void processTouch(Vector2 point, int action);

  }
