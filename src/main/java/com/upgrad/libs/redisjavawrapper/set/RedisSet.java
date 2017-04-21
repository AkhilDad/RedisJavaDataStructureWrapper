package com.upgrad.libs.redisjavawrapper.set;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * Created by akhil on 21/04/17.
 */

public class RedisSet<E>  extends AbstractSet<E> {

    private final Jedis jedis;
    private final String key;
    private final Class<E> aClass;
    private final ObjectMapper objectMapper;

    public RedisSet(Jedis jedis, String key, Class<E> aClass) {
        this.jedis = jedis;
        this.key = key;
        this.aClass = aClass;
        this.objectMapper = new ObjectMapper();
    }
    @Override
    public Iterator<E> iterator() {
        return new RedisSetIterator(0);
    }



    @Override
    public int size() {
        return jedis.scard(key).intValue();
    }

    @Override
    public boolean add(E e) {
        boolean isAdded = false;
        try {
            final String stringValue = getStringValue(e);
            if (!jedis.sismember(key, stringValue)) {
                jedis.sadd(key, stringValue);
                isAdded = true;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return isAdded;
    }

    @Override
    public boolean remove(Object o) {
        boolean isRemoved = false;
        if (o.getClass() == aClass) {
            try {
                String vale = getStringValue((E)o);
                if (jedis.sismember(key, vale)) {
                    isRemoved = true;
                    jedis.srem(key, vale);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isRemoved;
    }

    final class RedisSetIterator implements Iterator<E> {
        final int mOffset;
        int mSize;
        int mIndex;
        boolean mCanRemove = false;

        RedisSetIterator(int offset) {
            mOffset = offset;
            mSize = size();
        }

        @Override
        public boolean hasNext() {
            return mIndex < mSize;
        }

        @Override
        public E next() {
            final Set<String> smembers = jedis.smembers(key);
            final Iterator<String> iterator = smembers.iterator();
            int i = 0;
            E valueFromString = null;
            while (iterator.hasNext()) {
                if (i == mIndex) {
                    valueFromString = getValueFromString(iterator.next());

                } else {
                    iterator.next();
                }
                i++;
            }
            mIndex++;
            if (valueFromString == null) {
                throw new NoSuchElementException();
            }
            return valueFromString;
        }

        @Override
        public void remove() {
            if (!mCanRemove) {
                throw new IllegalStateException();
            }
        }
    }

    private E getValueFromString(String value) {
        try {
            if (aClass  == String.class) {
                return (E)value;
            } else {
                return objectMapper.readValue(value, aClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStringValue(E e) throws IOException {
        String value;
        if (e instanceof String) {
            value = (String) e;
        } else {
            value = objectMapper.writeValueAsString(e);
        }
        return value;
    }
}
