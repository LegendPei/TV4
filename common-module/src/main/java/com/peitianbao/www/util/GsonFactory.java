package com.peitianbao.www.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author leg
 */
public class GsonFactory {
    @Getter
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setLenient()
            .serializeNulls()
            .create();

}