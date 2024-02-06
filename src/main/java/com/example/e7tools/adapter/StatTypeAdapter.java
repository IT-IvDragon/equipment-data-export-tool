package com.example.e7tools.adapter;

import com.example.e7tools.model.Stat;
import com.google.gson.*;

import java.lang.reflect.Type;

public class StatTypeAdapter implements JsonSerializer<Stat>, JsonDeserializer<Stat> {
    @Override
    public JsonElement serialize(Stat src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getType());
        jsonObject.addProperty("value", src.getValue());
        jsonObject.addProperty("rolls", src.getRolls());
        jsonObject.addProperty("modified", src.getModified());
        return jsonObject;
    }

    @Override
    public Stat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        double value = jsonObject.get("value").getAsDouble();
        Integer rolls = jsonObject.get("rolls").getAsInt();
        Boolean modified = jsonObject.get("modified").getAsBoolean();
        return new Stat(type, value, rolls, modified);
    }
}
