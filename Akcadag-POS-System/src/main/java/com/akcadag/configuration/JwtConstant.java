package com.akcadag.configuration;

public class JwtConstant {
    public static final String JWT_SECRET = "f9A2kLm8Pq7sXzY1Vr5tWcB3Nh6GqD4E";
    public static final String JWT_HEADER = "Authorization";
}
/*
bu kismi application.properties de tanimlayarak
jwt.secret=f9A2kLm8Pq7sXzY1Vr5tWcB3Nh6GqD4E

@Value("${jwt.secret}")
public static String JWT_SECRET;  bu sekilde de kullanilablir.

*
* */