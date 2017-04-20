package com.upgrad.libs.redisjavaclient;

/**
 * Created by akhil on 21/04/17.
 */

public class User {
    public String name;

    public User() {

    }

    public User(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        System.out.println(((User)obj).toString()+"--"+toString());
        return name.equals(obj.toString());
    }

    @Override
    public String toString() {
        return name;
    }
}
