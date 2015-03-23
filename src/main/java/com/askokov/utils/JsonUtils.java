package com.askokov.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

/**
 * This class has the json utils
 */
public final class JsonUtils {
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    /**
     * This is default constructor. it should be private because it is the util class
     */
    private JsonUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * This method converts the object to bytes array
     * @param object input object
     * @return byte[]
     * @throws IOException for unpredictable situation
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    /**
     * This method converts the object to string
     * @param object input object
     * @return string
     * @throws IOException for unpredictable situation
     */
    public static String convert2Json(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }

    /**
     * This method converts the json text to object by input class
     * @param json text json
     * @param cl class which return the instance
     * @param <T> the class of instance
     * @return instance of class from json text
     */
    public static <T> T convert2Object(String json, Class<T> cl) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, cl);
    }

    public static <T> List<T> convert2ListObject(String json, Class<T> cl) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, cl));
    }
}