
package funkwitzs.gamemanager.Engine.Components;

import funkwitzs.gamemanager.Engine.Math.Vector2;

public interface Interactable {
  void onDown(Vector2 point);
  void onUp(Vector2 point);
  void onDrag(Vector2 point);
}