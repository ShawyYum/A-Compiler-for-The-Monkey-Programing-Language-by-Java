package vm;

import object.Object;

import java.util.ArrayList;

public class Frame {
    public Object.Closure cl;
    public int ip;
    public int basePointer;

    public Frame(Object.Closure c,int b) {
        cl = c;
        ip = -1;
        basePointer = b;
    }

    public ArrayList<Byte> Instructions() {
        return cl.Fn.Instructions;
    }
}
