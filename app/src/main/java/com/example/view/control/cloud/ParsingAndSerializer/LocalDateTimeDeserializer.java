package com.example.view.control.cloud.ParsingAndSerializer;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ein benutzerdefinierter Deserializer für die Klasse {@link LocalDateTime}, der ISO 8601
 * formatierte Zeitstempel aus JSON extrahiert und in ein {@link LocalDateTime}-Objekt umwandelt.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    // ISO 8601 Format für LocalDateTime

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Deserialisiert einen ISO 8601 Zeitstempel-String aus JSON in ein {@link LocalDateTime}-Objekt.
     *
     * @param json        Das JSON-Element, das den Zeitstempel im ISO 8601-Format enthält.
     * @param typeOfT     Der Typ des zu deserialisierenden Objekts.
     * @param context     Der Deserialisierungs-Kontext.
     * @return Ein {@link LocalDateTime}-Objekt, das den deserialisierten Zeitstempel darstellt.
     * @throws JsonParseException Wenn ein Fehler beim Deserialisieren auftritt.
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Deserialisieren von ISO 8601 String
        return LocalDateTime.parse(json.getAsString(), formatter);
    }
}
