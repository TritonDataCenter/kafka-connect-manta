package com.joyent.manta.kafka;

/*

  Simple Helper class for mocking test framework.

 */
class ObjectFactory {
    <T> T getObject(final Class<T> klass, final T obj) {
        return obj;
    }
}
