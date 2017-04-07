package edu.brown.cs.jmrs.io;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * An interface for objects that can be represnted in json form.
 *
 * Created by henry on 4/3/2017.
 */
public interface JsonSerializable {
  Gson GSON = new Gson();
  JsonParser PARSER = new JsonParser();

  /**
   * @return The JSON serialized representation of this object.
   */
  String toJson();

  /**
   * @param params
   *          A map of parameters to create JSON from.
   * @return JSON representing the provided parameters.
   */
  default String toJson(Map<String, ?> params) {
    // TODO: Is this slow?
    JsonObject result = new JsonObject();
    for (Entry<String, ?> entry : params.entrySet()) {

      if (entry.getValue() instanceof JsonSerializable) {
        // parse serializables into their own objects
        result.add(entry.getKey(),
            PARSER.parse(((JsonSerializable) entry.getValue()).toJson()));
      } else if (entry.getValue() instanceof Number) {
        result.addProperty(entry.getKey(), (Number) entry.getValue());
      } else if (entry.getValue() instanceof Boolean) {
        result.addProperty(entry.getKey(), (Boolean) entry.getValue());
      } else {
        result.addProperty(entry.getKey(), entry.getValue().toString());
      }
    }
    return GSON.toJson(result);
  }

  /**
   * @param obj
   *          The object to JSONify using its internal fields.
   * @return A JSON string representing all non-transient fields in obj. Mark
   *         fields with the 'transient' keyword to remove them from this
   *         string.
   */
  default String toJson(Object obj) {
    return GSON.toJson(obj);
  }

  /**
   * @param lst
   *          A collection of JsonSerialzable objects.
   * @return The combined JSON of all those objects.
   */
  static String toJson(Collection<? extends JsonSerializable> lst) {
    if (lst.size() == 0) {
      return "[]";
    }

    StringBuilder result = new StringBuilder("[");
    for (JsonSerializable jsonObj : lst) {
      result.append(jsonObj.toJson() + ",");
    }
    return result.substring(0, result.length() - 1) + "]";
  }

  /**
   * @param lst
   *          A collection of any type of object.
   * @param serializer
   *          The function to serialize each object to a string.
   * @return The combined JSON of all those objects, where each object is
   *         converted to Json using serializer.
   * @param <T>
   *          The type of object in lst to serialize.
   */
  static <T> String toJson(Collection<T> lst, Function<T, String> serializer) {
    if (lst.size() == 0) {
      return "[]";
    }

    StringBuilder result = new StringBuilder("[");
    for (T obj : lst) {
      result.append(serializer.apply(obj) + ",");
    }
    return result.substring(0, result.length() - 1) + "]";
  }
}
