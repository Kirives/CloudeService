package com.example.demo.services;

import org.springframework.stereotype.Service;

@Service
public class StringServices {


    public String getLastSub(String originalString,String delimer){
        int laastIndex = originalString.lastIndexOf(delimer);
        return originalString.substring(laastIndex+delimer.length());
    }
}
