package com.upgrad.libs.redisjavaclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrad.libs.redisjavawrapper.list.RedisList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

public class RedisClient {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost");
        System.out.println("Connection to server sucessfully");
//        User user = new User("Akhil");
//        List<User> userList = new RedisList<>(jedis, "xyz", User.class);
//        userList.clear();
//        userList.add(new User("akhil"));
//        userList.add(new User("Akhil"));
//        userList.add(new User("Dad"));
//        userList.add(new User("akhil"));
//        iterate(userList);
//        userList.remove(0);
//        iterate(userList);
//        userList.add(2, new User("Update"));
//        userList.remove(new User("akhil"));
//        iterate(userList);
//        System.out.print("Last index of"+userList.lastIndexOf(new User("akhil")));

//        while (true) {
//            try {
//                Thread.sleep(2000);
//                iterate(userList);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        List<String> userList = new RedisList<>(jedis, "xchgsdyz", String.class);
        List<String> userList = new ArrayList<>();
        userList.clear();
        userList.add("akhil");
        userList.add("Akhil");
        userList.add("Dad");
        userList.add("akhil");
        iterate(userList);
        userList.remove(0);
        iterate(userList);
        userList.add(2, "Update");
        userList.remove("akhil");
        iterate(userList);
        userList.add("1");
        userList.add("2");
        userList.add("3");
        userList.add("4");
        userList.add("5");
        userList.add("6");
        userList.add("7");
        userList.add("8");
        userList.add("9");
        iterate(userList);
        final List<String> stringList = userList.subList(5, 7);
        iterate(stringList);
        stringList.add("new value1");
        stringList.add("new value2");
        stringList.remove(0);
        iterate(userList);
//        System.out.print("Last index of->"+userList.lastIndexOf("akhil"));
//        User user1 = new User("Akhil");
//        ObjectMapper objectMapper = new ObjectMapper();
//        String value = null;
//        try {
//            value = objectMapper.writeValueAsString(user1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        User user2 = objectMapper.convertValue(value, User.class);
//        try {
//            user2 = objectMapper.readValue(value, User.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.print("User 1 -->"+user1.toString());
//        System.out.print("User 2 -->"+user2.toString());

//        while (true) {
//            try {
//                Thread.sleep(2000);
//                iterate(userList);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private static <E> void iterate(List<E> userList) {
        System.out.println("\n\nIterating ---"+userList.size());
        for(int i = 0; i<userList.size(); i++) {
            System.out.println("Stored string in redis : ->"+i+":"+userList.get(i));
        }
    }
}
