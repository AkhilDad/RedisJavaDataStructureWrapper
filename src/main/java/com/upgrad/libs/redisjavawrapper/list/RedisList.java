package com.upgrad.libs.redisjavawrapper.list;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

/**
 * Created by akhil on 20/04/17.
 */

public class RedisList<E> extends AbstractList<E> {

    private final String key;
    private Jedis jedis;
    private Class<? extends E> typeClass;
    private ObjectMapper objectMapper;

    public RedisList(Jedis jedis, String key, Class<E> typeClass) {
        this.jedis = jedis;
        this.key = key;
        this.typeClass = typeClass;
        objectMapper = new ObjectMapper();
    }

    @Override
    public boolean add(E element) {
        try {
            jedis.rpush(key, getStringValue(element));
        } catch (IOException e) {
            e.printStackTrace();
            final RuntimeException runtimeException = new RuntimeException("Unable to insert");
            runtimeException.setStackTrace(e.getStackTrace());
            throw runtimeException;
        }
        return true;
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

    @Override
    public E get(int index) {
        rangeCheck(index);
        return getValueFromString(jedis.lindex(key, index));
    }

    @Override
    public E set(int index, E element) {
        rangeCheck(index);
        final String oldValue = jedis.lindex(key, index);
        try {
            jedis.lset(key, index, getStringValue(element));
        } catch (IOException e) {
            e.printStackTrace();
            final RuntimeException runtimeException = new RuntimeException("Unable to set");
            runtimeException.setStackTrace(e.getStackTrace());
            throw runtimeException;
        }
        return getValueFromString(oldValue);
    }

    @Override
    public void add(int index, E element) {
        rangeCheck(index);
        try {
            jedis.linsert(key, BinaryClient.LIST_POSITION.BEFORE, jedis.lindex(key, index), getStringValue(element));
        } catch (IOException e) {
            e.printStackTrace();
            final RuntimeException runtimeException = new RuntimeException("Unable to insert");
            runtimeException.setStackTrace(e.getStackTrace());
            throw runtimeException;
        }
    }

    @Override
    public E remove(int index) {
        rangeCheck(index);
        E e = getValueFromString(jedis.lindex(key, index));
        String value = UUID.randomUUID().toString();
        jedis.lset(key, index, value);
        jedis.lrem(key, index > size() / 2 ? -1 : 1, value);
        return e;
    }

    @Override
    public Iterator<E> iterator() {
        return new RedisListItr();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new RedisListListItr(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new RedisListListItr(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    private void rangeCheck(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private E getValueFromString(String value) {
        try {
            if (typeClass  == String.class) {
             return (E)value;
            } else {
                return objectMapper.readValue(value, getGenericTypeClass());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int size() {
        return jedis.llen(key).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!isEmpty()) {
            final List<String> lrange = jedis.lrange(key, 0, size());
            return lrange.contains(o);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (isEmpty()) {
            return false;
        }
        final List<String> lrange = jedis.lrange(key, 0, size());
        for (String value : lrange) {
            if (o instanceof String) {
                o.equals(value);
            } else {
                final E type = getValueFromString(value);
                o.equals(type);
            }
        }
        return false;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size();
    }


    private Class<? extends E> getGenericTypeClass() {
       return typeClass;
    }

    private class RedisListItr implements Iterator<E> {

        public RedisListItr() {
            int lastRet = -1; // index of last element returned; -1 if no such
            int expectedModCount = modCount;
        }

        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size();
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size())
                throw new NoSuchElementException();
            cursor = i + 1;
            return RedisList.this.get(lastRet = i);
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                RedisList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = size();
            int i = cursor;
            if (i >= size) {
                return;
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) get(i++));
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class RedisListListItr extends RedisList<E>.RedisListItr implements ListIterator<E> {

        RedisListListItr(int var2) {
            this.cursor = var2;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public E previous() {
            this.checkForComodification();
            int var1 = this.cursor - 1;
            if(var1 < 0) {
                throw new NoSuchElementException();
            } else {
                if(var1 >= size()) {
                    throw new ConcurrentModificationException();
                } else {
                    this.cursor = var1;
                    return RedisList.this.get(this.lastRet = var1);
                }
            }
        }

        public void set(E var1) {
            if(this.lastRet < 0) {
                throw new IllegalStateException();
            } else {
                this.checkForComodification();

                try {
                    RedisList.this.set(this.lastRet, var1);
                } catch (IndexOutOfBoundsException var3) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        public void add(E var1) {
            this.checkForComodification();

            try {
                int var2 = this.cursor;
                RedisList.this.add(var2, var1);
                this.cursor = var2 + 1;
                this.lastRet = -1;
                this.expectedModCount = RedisList.this.modCount;
            } catch (IndexOutOfBoundsException var3) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
