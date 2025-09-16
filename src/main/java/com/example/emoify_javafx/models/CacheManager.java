package com.example.emoify_javafx.models;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static final String CACHE_FILE = "cache.txt";
    private static final Map<String, String> cache = new HashMap<>();

    static {

        File file = new File(CACHE_FILE);

        if(file.exists()){
            try(BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE))) {
                String line;
                while ((line = reader.readLine()) != null){
                    String[] parts = line.split("=", 2);
                    if(parts.length == 2){
                        cache.put(parts[0], parts[1]);
                    }
                }
            } catch (IOException ignored){

            }
        }
    }

    public static void put(String key, String value){
        cache.put(key, value);
        saveToFile();
    }

    public static String get(String key){
        return cache.get(key);
    }

    public static Boolean getContained(String key){
        return cache.containsKey(key);
    }

    private static void saveToFile(){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE_FILE))){
            for(Map.Entry<String, String> entry : cache.entrySet()){
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        }catch (IOException ignored){

        }
    }
}
