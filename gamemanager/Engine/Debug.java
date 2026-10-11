package funkwitzs.gamemanager.Engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Debug {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.METHOD})
  public @interface Debuggable {}

  private static List<LogLine> logs = new ArrayList<>();
  private static boolean enabled = true;
  private static Object logObject = null;
  public static float position;

  public static Paint textPaint = new Paint();
  public static Paint colorPaint = new Paint();

  private static class LogLine {
    String text; int color = Color.BLACK;
    public LogLine(String txt, int col) {
      text = txt;
      color = col;
    }
  }
  private static class ParsedMethod {
    String name; String[] args;
    public ParsedMethod(String n, String[] a) {
      name = n; args = a;
    }
  }

  public static void Initilize() {
    logs.add(new LogLine("Start of log", Color.WHITE));
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(20);
    position = textPaint.getTextSize();
    colorPaint.setColor(Color.WHITE);
    colorPaint.setTextSize(12);
  }

  @Debuggable
  public static void toggelLog() {
    enabled = !enabled;
  }
  public static void shiftLog(boolean shiftUp) {
    position -= 8 * (shiftUp? -1 : 1);
  }

  public static void draw(Canvas canvas) {
    if (!enabled) return;
    float row = position; int h = canvas.getHeight();
    for (int i = 0; i < logs.size(); i++) {
      String[] lines = logs.get(i).text.split("\\r?\\n"); textPaint.setColor(logs.get(i).color);
      for (int j = 0; j < lines.length; j++, row += textPaint.getTextSize()) {
        if (row < 0) continue; if (row > h) return;
        canvas.drawText(lines[j], 5, row, textPaint);
      }
    }
  }

  public static void drawGameObjects(Canvas canvas, ArrayList<GameObject> gObjs) {
    if (!enabled) return;
    for (int i = 0; i < gObjs.size(); i++) {
      GameObject gObj = gObjs.get(i); colorPaint.setColor(gObj.enabled? Color.GREEN : Color.RED);
      canvas.drawRect(gObj.transform.position().x, gObj.transform.position().y, gObj.transform.position().x + 12, gObj.transform.position().y + 12, colorPaint);
      canvas.drawText("GameObject (" + i + ")", gObj.transform.position().x + 13, gObj.transform.position().y + 12, colorPaint);
    }
  }

  public static void log(String text) {
    logs.add(new LogLine(text, Color.WHITE));
  }
  public static void log(String text, int color) {
    logs.add(new LogLine(text, color));
  }

  public static void logStackTrace(StackTraceElement[] stackTrace) {
    String text = "";
    for (StackTraceElement el : stackTrace) {
      text = text + el.toString() + "\n";
    }
    logs.add(new LogLine(text, Color.RED));
  }

  public static void processLogInput(String input) {
    String[] parts = input.split("\\.");

    if (parts[0].isEmpty()) {
        log("invalid log", Color.YELLOW);
        return;
    }

    switch (parts[0]) {
      case "help":
        log("help:\nscene\nrenderer\nlogr\nlogs", Color.CYAN);
      return;
      case "clear":
        logs.clear();
        logs.add(new LogLine("Start of log.", Color.WHITE));
        position = textPaint.getTextSize();
      return;
      case "scene":
        processClassToLog(EngineManager._inst.scene, parts, 1);
      return;
      case "renderer":
        processClassToLog(EngineManager._inst.renderer, parts, 1);
      return;
      case "logs":
        if (logObject == null) logs.add(new LogLine("There is no saved log object!", Color.YELLOW));
        processClassToLog(logObject, parts, 1);
      return;
    }

    log("invalid log", Color.YELLOW);
  }

  //gobj.logcount()  gobj.get().position(x,y,z)
  private static void processClassToLog(Object obj, String[] input, int i) {


    if (i >= input.length) {
      if (obj == null) return;
      classContentsToLog(obj);
      return;
    }

    if (obj == null) {
      log("log: " + input[i - 1] + " is null??");
      return;
    }

    if (input[i].equals("logr")) {
      Debug.log(obj.toString(), Color.CYAN);
      return;
    }
    else if (input[i].equals("logs")) {
      logObject = obj;
      return;
    }

    ParsedMethod parmethod = parseMethod(input[i]);
    if (parmethod != null) {

      Method method = getClassDebugMethod(obj, parmethod.name);

      if (method == null) {
        log("log method: " + parmethod.name + " is invalid!", Color.RED);
        return;
      }

      Class<?>[] params = method.getParameterTypes();
      Object[] args = new Object[params.length];

          if (parmethod.args.length != args.length) {
              log("log method:" + parmethod.name + " dosent have the required Paramaters!", Color.RED);
        return;
      }

      for (int j = 0; j < args.length; j++) {
        String type = params[j].getSimpleName().toLowerCase();

        switch(type) {
          case "int":
            args[j] = Integer.parseInt(parmethod.args[j]);
            break;
          case "float":
            args[j] = Float.parseFloat(parmethod.args[j]);
            break;
          case "string":
            args[j] = parmethod.args[j];
            break;
          default:
            log("input paramater:" + params[j] + " is not a basic type (int,float,string)!", Color.RED);
            return;
        }
      }

      try {
        Object result = method.invoke(obj, args);
        if (result == null) return;
        processClassToLog(result, input, i + 1);
        return;
      }
      catch (IllegalAccessException | InvocationTargetException ex) {
        log("log method:" + method.getName() + " failed: " + ex.getClass().getSimpleName(), Color.RED);
        return;
      }
    }
    else {
      Field field = getClassDebugField(obj, input[i]);

      if (field == null) {
        log("log field: " + input[i] + " is invalid!", Color.RED);
        return;
      }

      try {
        Object value = field.get(obj);
        processClassToLog(value, input, i + 1);
        return;
      }
      catch (IllegalAccessException ex) {
        log("Field " + field.getName() + " is not accessible!", Color.RED);
        return;
      }
    }
  }

  private static ParsedMethod parseMethod(String str) {
    int open = str.indexOf('(');
    int close = str.lastIndexOf(')');

    if (open == -1 || close == -1 || close < open) return null;

    String name = str.substring(0, open).trim();
    String inside = str.substring(open + 1, close).trim();

    String[] args = inside.isEmpty() ? new String[0] : inside.split("\\s*,\\s*");

    return new ParsedMethod(name, args);
  }

  private static Field getClassDebugField(Object cls, String name) {
    for (Field field : getDebuggableFields(cls)) {
      if (field.getName().equals(name)) return field;
    }
    return null;
  }
  private static Method getClassDebugMethod(Object cls, String name) {
    for (Method method : getDebuggableMethods(cls)) {
      if (method.getName().equals(name)) return method;
    }
    return null;
  }

  private static void classContentsToLog(Object cls) {
    StringBuilder log = new StringBuilder();

    for (Field field : cls.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Debuggable.class)) {
        field.setAccessible(true);
        log.append(field.getName()).append("\n");
      }
    }
    for (Method method : cls.getClass().getDeclaredMethods()) {
      if (method.isAnnotationPresent(Debuggable.class)) {
        method.setAccessible(true);
        log.append(method.getGenericReturnType()).append(" ").append(method.getName()).append("(");

        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
          log.append(":").append(params[i].getSimpleName());
          if (i < params.length - 1) log.append(", ");
        }
        log.append(")\n");
      }
    }

    Debug.log(cls.getClass().getName() + (log.length() > 0? " ==>" : ""), Color.CYAN);
    Debug.log(log.toString(), Color.WHITE);
  }

  private static Field[] getDebuggableFields(Object cls) {
    ArrayList<Field> fields = new ArrayList<>();
    for (Field field : cls.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Debuggable.class)) {
        field.setAccessible(true);
        fields.add(field);
      }
    }
    return fields.toArray(new Field[0]);
  }
  private static Method[] getDebuggableMethods(Object cls) {
    ArrayList<Method> methods = new ArrayList<>();
    for (Method method : cls.getClass().getDeclaredMethods()) {
      if (method.isAnnotationPresent(Debuggable.class)) {
        method.setAccessible(true);
        methods.add(method);
      }
    }
    return methods.toArray(new Method[0]);
  }
}
