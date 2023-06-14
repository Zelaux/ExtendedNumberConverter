package com.zelaux.numberconverter.actions.underscore;

import com.intellij.util.IntPair;
import com.zelaux.numberconverter.numbertype.NumberType;
import com.zelaux.numberconverter.utils.Pool;

public class CaretDescriptor implements Pool.Poolable {
   public IntPair range;
   public String text;
   public IntPair destination;
   public int spacing;
    public NumberType.RadixType radixType;

    private CaretDescriptor() {
        super();
    }

    public static final Pool<CaretDescriptor> pool = new Pool.PoolImpl<>(CaretDescriptor::new);

    public CaretDescriptor set(IntPair range,
                               String text,
                               IntPair destination,
                               int spacing,
                               NumberType.RadixType radixType) {
        this.range = range;
        this.text = text;
        this.destination = destination;
        this.spacing = spacing;
        this.radixType=radixType;
        return this;
    }

    @Override
    public void reset() {
        range = destination = null;
        text = null;
        radixType=null;
        spacing = -1;
    }

    public void free() {
        pool.free(this);
    }
}
