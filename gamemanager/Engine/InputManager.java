package funkwitzs.gamemanager.Engine;

import android.view.View.OnTouchListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import funkwitzs.gamemanager.GameManager;
import funkwitzs.gamemanager.Engine.Math.Vector2;

public class InputManager implements OnTouchListener, OnKeyListener {

  public boolean enabled = true;
  public static Vector2 userTouchPositon = new Vector2(0, 0);
  public static int userTouchState = MotionEvent.ACTION_UP;

  @Override
  public boolean onTouch(View v, android.view.MotionEvent event) {
    if (!enabled) return false;

    userTouchState = event.getAction();
    userTouchPositon = new Vector2(event.getX(),event.getY());

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        EngineManager._inst.renderer.interactRenderObjects(userTouchPositon, userTouchState);
      break;
      case MotionEvent.ACTION_MOVE:
        EngineManager._inst.renderer.interactRenderObjects(userTouchPositon, userTouchState);
      break;
      case MotionEvent.ACTION_UP:
        EngineManager._inst.renderer.interactRenderObjects(userTouchPositon, userTouchState);
      break;
    }
    return true;
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (!enabled) return false;

    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_ESCAPE:
          Debug.toggelLog();
        break;
        case KeyEvent.KEYCODE_W:
          Debug.shiftLog(true);
        break;
        case KeyEvent.KEYCODE_S:
          Debug.shiftLog(false);
        break;
        case KeyEvent.KEYCODE_ENTER:
          enabled = false;
          GameManager._inst.showTextPrompt("INPUT DEBUG", (input) -> {
            Debug.processLogInput(input);
            EngineManager._inst.inputs.enabled = true;
          });
        break;
      }
    }
    return true;
  }

}
