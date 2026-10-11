package funkwitzs.gamemanager.Engine;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;

import funkwitzs.gamemanager.*;
import funkwitzs.gamemanager.GameManager.ZipContainFile;
import funkwitzs.gamemanager.Engine.Math.*;
import funkwitzs.gamemanager.Engine.Prefab.*;
import funkwitzs.gamemanager.Engine.RenderObjects.Sprite;

public class Resources {

  private static boolean Initilized = false;
  public static Context context;
  ///
  public static final String PACKAGEPATH = "/funkwitzs/game-manager/Package/";
  public static final String GFXPATH = "/funkwitzs/game-manager/Package/gfx/";

  public static final HashMap<String, Bitmap> LOADED_GFX = new HashMap<String, Bitmap>();
  public static final HashMap<String, Prefab> LOADED_PREFABS = new HashMap<String, Prefab>();
  //public static final HashMap<String, Scene> LOADED_SCENES = new HashMap<String, Scene>();

  public static void Initilize() {
    if (Initilized) return;
    context = GameManager._inst.context;

    loadGfx();
    loadPrefabs();

    Initilized = true;
  }

  public static Bitmap GetBitmap(String id) {
    Bitmap bitmap = LOADED_GFX.get(id);
    if (bitmap == null) {
      Debug.log("Couldint get Gfx because of invalid id : " + id, Color.RED);
      return null;
    }
    return bitmap;
  }
  public static Sprite GetSprite(String id) {
    Bitmap bitmap = LOADED_GFX.get(id);
    if (bitmap == null) {
      Debug.log("Couldint create sprite because of invalid Gfx id : " + id, Color.RED);
      return null;
    }
    Sprite sprite = new Sprite(bitmap);
    return sprite;
  }

  //#region Loading
  private static boolean loadGfx() {
    ArrayList<ZipContainFile> gfxs = GameManager._inst.loadFilesFromZip("Gfx.zip",".png");
    if (gfxs == null) {
      return false;
    }

    for (ZipContainFile gfx : gfxs) {

      if (LOADED_GFX.containsKey(gfx.name)) {
        Debug.log("Skipped loaded gfx :" + gfx.name); continue;
      }

      Bitmap bitmap = BitmapFactory.decodeByteArray(gfx.data, 0, gfx.data.length);
      if (bitmap  == null) {
        Debug.log("Failed to decode gfx byte data :" + gfx.name); continue;
      }

      Debug.log("Loaded gfx :" + gfx.name);
      LOADED_GFX.put(gfx.name, bitmap);
    }

    return true;
  }

  private static boolean loadPrefabs() {

    NodeList prefabs = GameManager._inst.getXMLElements("GamePrefabs.xml", "Prefab");
    if (prefabs == null) return false;

    for (int i = 0; i < prefabs.getLength(); i++) {

      Node node_prefab = prefabs.item(i);
      if (node_prefab.getNodeType() != Node.ELEMENT_NODE) continue;

      Element ref = (Element)node_prefab;
      Prefab prefab = new Prefab();
      NodeList data = ref.getChildNodes();

      for (int j = 0; j < data.getLength(); j++) {

        Node node_data = data.item(i);
        if (node_data.getNodeType() != Node.ELEMENT_NODE) continue;

        switch (data.item(j).getNodeName()) {
          case "Transform":
            loadPrefabs_LoadTransform(prefab, (Element)data.item(j));
          break;
          case "Sprite":
            loadPrefabs_LoadSprite(prefab, (Element)data.item(j));
          break;
          case "Component":
            loadPrefabs_LoadComponent(prefab, (Element)data.item(j));
          break;
        }
      }

      LOADED_PREFABS.put((String)ref.getAttribute("name"), prefab);
    }

    return true;
  }
  private static void loadPrefabs_LoadTransform(Prefab prefab, Element element) {

    Transform transform = Transform.zero();

    NodeList vars = element.getChildNodes();
    for (int i = 0; i < vars.getLength(); i++) {

      Node node_var = vars.item(i);
      if (node_var.getNodeType() != Node.ELEMENT_NODE) continue;

      Element var = (Element)vars.item(i);
      switch (var.getNodeName()) {
        case "Position":
          transform.position(
            Float.parseFloat(var.getAttribute("x")),
            Float.parseFloat(var.getAttribute("y")),
            Float.parseFloat(var.getAttribute("z"))
          );
        break;
        case "Scale":
          transform.scale(
            Float.parseFloat(var.getAttribute("w")),
            Float.parseFloat(var.getAttribute("h"))
          );
        break;
      }
    }

    prefab.transform = transform;
  }
  private static void loadPrefabs_LoadSprite(Prefab prefab, Element element) {

    Bitmap map = LOADED_GFX.get(element.getAttribute("gfx"));
    if (map == null) {
      return;
    }

    Rect src = new Rect(0,0,map.getWidth(),map.getHeight());
    Vector2 scale = new Vector2(src.right, src.bottom);

    NodeList vars = element.getChildNodes();
    for (int i = 0; i < vars.getLength(); i++) {

      Node node_var = vars.item(i);
      if (node_var.getNodeType() != Node.ELEMENT_NODE) continue;

      Element var = (Element)vars.item(i);
      switch (var.getNodeName()) {
        case "Source":
          src.set(
            Integer.parseInt(var.getAttribute("l")),
            Integer.parseInt(var.getAttribute("t")),
            Integer.parseInt(var.getAttribute("r")),
            Integer.parseInt(var.getAttribute("b"))
            );
        break;
        case "Scale":
          scale.set(
            Integer.parseInt(var.getAttribute("w")),
            Integer.parseInt(var.getAttribute("h"))
          );
        break;
      }
    }

    prefab.sprite = new SprRef(map, src, scale);
  }
  private static void loadPrefabs_LoadComponent(Prefab prefab, Element element) {
    CompRef comp = new CompRef((String)element.getAttribute("type"));

    /// Parse component values? like [c(4)] <-- gets child 4 from root component
    NodeList vars = element.getChildNodes(); comp.vars = new Object[vars.getLength()][];
    for (int i = 0; i < vars.getLength(); i++) {

      Node node_var = vars.item(i);
      if (node_var.getNodeType() != Node.ELEMENT_NODE) continue;

      Element var = (Element)vars.item(i);
      comp.vars[i] = new Object[] { var.getAttribute("name"), var.getAttribute("value") };
    }

    prefab.components.add(comp);
  }
  //#endregion Loading
}
