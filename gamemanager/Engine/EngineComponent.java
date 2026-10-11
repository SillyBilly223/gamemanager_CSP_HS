package funkwitzs.gamemanager.Engine;

import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class EngineComponent extends AndroidViewComponent {

  private final EngineRenderer renderer;

  public EngineComponent(ComponentContainer container) {
    super(container);
    renderer = new EngineRenderer(container.$context());

    renderer.setLayoutParams(new FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    ));

    container.$add(this);
  }

  @Override
  public View getView() {
    return renderer;
  }

  public EngineRenderer getRenderer() {
    return renderer;
  }
}
