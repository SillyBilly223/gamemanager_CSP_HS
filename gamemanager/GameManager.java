package funkwitzs.gamemanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import funkwitzs.gamemanager.Engine.*;


public class GameManager extends AndroidNonvisibleComponent {

  public static GameManager _inst;

  public Form form;
  public Context context;
  public ComponentContainer container;

  public static EngineManager engine;

  public GameManager(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    form = container.$form();
    this.container = container;
    _inst = this;
  }

  @SimpleFunction(description = "Initilizes Scene")
  public void InitilizeEngine() {
    engine = new EngineManager(container);
  }

  public NodeList getXMLElements(String path, String rootElements) {

    try ( InputStream stream = form.openAssetForExtension(this, path) ) {

      if (stream == null) {
        Debug.log("Invalid InputStream path :" + path);
        return null;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      Document doc = null;

      try { DocumentBuilder builder = factory.newDocumentBuilder(); doc = builder.parse(stream); }
      catch(Exception e) {
        Debug.log("Error parsing XML: " + path + " - " + e.getMessage());
        return null;
      }

      doc.getDocumentElement().normalize();

      NodeList elements = doc.getElementsByTagName(rootElements);
      return elements;
    }
    catch (IOException e) {
      Debug.log("IOException opening InputStream: " + path + " - " + e.getMessage());
      return null;
    }
  }

  public ArrayList<ZipContainFile> loadFilesFromZip(String zipPath, String fileType) {
    try ( InputStream stream = form.openAssetForExtension(this, zipPath) ) {
      ArrayList<ZipContainFile> files = new ArrayList<>();
      try (ZipInputStream zip = new ZipInputStream(stream)) {
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int count;
          while ((count = zip.read(buffer)) != -1) {
              baos.write(buffer, 0, count);
          }
          files.add(new ZipContainFile(baos.toByteArray(), entry.getName()));
          zip.closeEntry();
        }
      } catch (IOException e) {
        Debug.log("IOException opening ZipInputStream: " + zipPath + " - " + e.getMessage());
        return null;
      }
      return files;
    }
    catch (IOException e) {
      Debug.log("IOException opening InputStream: " + zipPath + " - " + e.getMessage());
      return null;
    }
  }

  public static class ZipContainFile  {
    public byte[] data;
    public String name;
    public ZipContainFile(byte[] data, String name) {
      this.data = data; this.name = name;
    }
  }

  public void showTextPrompt(String title, Consumer<String> callback) {
    final EditText input = new EditText(context);

    new AlertDialog.Builder(context)
      .setTitle(title)
      .setView(input)
      .setPositiveButton("OK", (dialog, which) -> {
        callback.accept(input.getText().toString());
      })
      .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
      .show();
  }
}