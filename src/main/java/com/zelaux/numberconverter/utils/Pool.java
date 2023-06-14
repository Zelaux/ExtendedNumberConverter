package com.zelaux.numberconverter.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * A pool of objects that can be reused to avoid allocation.
 * @author Nathan Sweet
 */
abstract public class Pool<T>{
    public static class PoolImpl<T> extends Pool<T>{
        public final Supplier<T> supplier;

        public PoolImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected T newObject() {
            return supplier.get();
        }
    }
    /** The maximum number of objects that will be pooled. */
    public final int max;
    private final ArrayList<T> freeObjects;
    /** The highest number of free objects. Can be reset any time. */
    public int peak;

    /** Creates a pool with an initial capacity of 16 and no maximum. */
    public Pool(){
        this(16, Integer.MAX_VALUE);
    }

    /** Creates a pool with the specified initial capacity and no maximum. */
    public Pool(int initialCapacity){
        this(initialCapacity, Integer.MAX_VALUE);
    }

    /** @param max The maximum number of free objects to store in this pool. */
    public Pool(int initialCapacity, int max){
        freeObjects = new ArrayList<>(initialCapacity);
        this.max = max;
    }

    abstract protected T newObject();

    /**
     * Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
     * {@link #free(Object) freed}).
     */
    public T obtain(){
        int size = freeObjects.size();
        return size == 0 ? newObject() : freeObjects.remove(size-1);
    }

    /**
     * Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
     * {@link #max} free objects, the specified object is reset but not added to the pool.
     * <p>
     * The pool does not check if an object is already freed, so the same object must not be freed multiple times.
     */
    public void free(T object){
        if(object == null) throw new IllegalArgumentException("object cannot be null.");
        if(freeObjects.size() < max){
            freeObjects.add(object);
            peak = Math.max(peak, freeObjects.size());
        }
        reset(object);
    }

    /**
     * Called when an object is freed to clear the state of the object for possible later reuse. The default implementation calls
     * {@link Poolable#reset()} if the object is {@link Poolable}.
     */
    protected void reset(T object){
        if(object instanceof Poolable) ((Poolable)object).reset();
    }

    /**
     * Puts the specified objects in the pool. Null objects within the array are silently ignored.
     * <p>
     * The pool does not check if an object is already freed, so the same object must not be freed multiple times.
     * @see #free(Object)
     */
    public void freeAll(Collection<T> objects){
        if(objects == null) throw new IllegalArgumentException("objects cannot be null.");
        ArrayList<T> freeObjects = this.freeObjects;
        int max = this.max;
        for (T object : objects) {
            if(object == null) continue;
            if(freeObjects.size() < max) freeObjects.add(object);
            reset(object);
        }
        peak = Math.max(peak, freeObjects.size());
    }

    /** Removes all free objects from this pool. */
    public void clear(){
        freeObjects.clear();
    }

    /** The number of objects available to be obtained. */
    public int getFree(){
        return freeObjects.size();
    }

    /** Objects implementing this interface will have {@link #reset()} called when passed to {@link Pool#free(Object)}. */
    public interface Poolable{
        /** Resets the object for reuse. Object references should be nulled and fields may be set to default values. */
        void reset();
    }
}
