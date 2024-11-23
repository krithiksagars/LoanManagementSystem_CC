package com.java.loan.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBPropertyUtil {
    public static String getConnectionString(String propertyFileName) {
        Properties properties = new Properties();
        try (InputStream input = DBPropertyUtil.class.getClassLoader().getResourceAsStream(propertyFileName)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + propertyFileName);
            }
            properties.load(input);
            
            return properties.getProperty("url");
        } catch (IOException e) {
            throw new RuntimeException("Error loading database properties", e);
        }
    }
}