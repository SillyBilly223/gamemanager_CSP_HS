package funkwitzs.gamemanager.Engine.RenderObjects;

import android.graphics.Canvas;
import android.graphics.Paint;
import funkwitzs.gamemanager.Engine.Math.*;

public class TextDisplay extends RenderObject {

  public String text = "";
  public Paint paint = new Paint();
  public Vector2 position = new Vector2(0,0);

  @Override
  public void drawToCanvas(Canvas canvas) {
    canvas.drawText(text, position.x, position.y, paint);
  }

  @Override
  public boolean contains(Vector2 point) {
    return false;
  }

  @Override
  public void processTouch(Vector2 point, int action) {
    return;
  }
}
