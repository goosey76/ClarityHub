package com.example.view.control.cloud.ParsingAndSerializer;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ein benutzerdefinierter Serializer für die Klasse {@link LocalDateTime}, der ein {@link LocalDateTime}-Objekt
 * in einen ISO 8601 formatierten String für die JSON-Serialisierung umwandelt.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {

    // ISO 8601 Format für LocalDateTime
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * Serialisiert ein {@link LocalDateTime}-Objekt als ISO 8601 formatierten String.
     *
     * @param localDateTime Das {@link LocalDateTime}-Objekt, das serialisiert werden soll.
     * @param typeOfSrc     Der Typ des zu serialisierenden Objekts.
     * @param context       Der Serialisierungs-Kontext.
     * @return Ein {@link JsonElement}, das den serialisierten ISO 8601 Zeitstempel enthält.
     */
    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
        // Serialisieren als ISO 8601 String
        return context.serialize(localDateTime.format(formatter));
    }
}
