package com.example.notes.Util;

import com.alibaba.fastjson.JSON;

public class myJsonUtil {
    public static boolean isJson(String content){
        boolean result = false;
        try {
            Object obj= JSON.parse(content);
            result = true;
        } catch (Exception e) {
            result=false;
        }
        return result;
    }
}
