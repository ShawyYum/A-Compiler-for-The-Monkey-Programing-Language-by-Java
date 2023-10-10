package vm;


import code.Code;
import compiler.Compiler;
import object.Builtins;
import object.Object;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import static code.Code.Opcode.*;

public class VM {
    public static final int StackSize = 2048;
    public static final int GlobalSize = 65536;
    public static final int MaxFrames = 1024;

    public static final Object.Error ERROR = new Object.Error("");
    public static final Object.Boolean TRUE = new Object.Boolean(true);
    public static final Object.Boolean FALSE = new Object.Boolean(false);
    public static final Object.Null NULL = new Object.Null();

    public ArrayList<Object> constants;
    public ArrayList<Object> stack;
    public int sp;
    public ArrayList<Object> globals;
    public ArrayList<Frame> frames;
    int frameIndex;

    public VM(Compiler.Bytecode bytecode,ArrayList<Object> s) {
        var mainFn = new Object.CompiledFunction(bytecode.Instructions);
        var mainClosure = new Object.Closure(mainFn);
        var mainFrame = new Frame(mainClosure,0);
        frames = new ArrayList<>(MaxFrames);
        frames.add(mainFrame);
        constants = bytecode.Constants;
        stack = new ArrayList<>(StackSize);
        sp = 0;
        frameIndex = 1;
        globals = s;
    }

    public Object.Error Run() {
        int ip;
        ArrayList<Byte> ins;
        byte op;

        while (currentFrame().ip < currentFrame().Instructions().size() - 1) {
            currentFrame().ip++;

            ip = currentFrame().ip;
            ins = currentFrame().Instructions();
            op = ins.get(ip);

            if (Objects.equals(op, OpConstant.getValue())) {
                int constIndex = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                var err = push(constants.get(constIndex));
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpPop.getValue())) {
                pop();
            }
            else if (Objects.equals(op, OpAdd.getValue()) || Objects.equals(op, OpSub.getValue()) || Objects.equals(op, OpMul.getValue()) || Objects.equals(op, OpDiv.getValue())) {
                var err = executeBinaryOperation(op);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpTrue.getValue())) {
                var err = push(TRUE);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpFalse.getValue())) {
                var err = push(FALSE);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpEqual.getValue()) || Objects.equals(op, OpNotEqual.getValue()) || Objects.equals(op, OpGreaterThan.getValue()) || Objects.equals(op,OpGreater.getValue())) {
                var err = executeComparison(op);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpBang.getValue())) {
                var err = executeBangOperator();
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpMinus.getValue())) {
                var err = executeMinusOperator();
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpJump.getValue())) {
                var pos = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip = pos - 1;
            }
            else if (Objects.equals(op, OpJumpNotTruthy.getValue())) {
                var pos = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                var condition = pop();
                if (!isTruthy(condition)) {
                    currentFrame().ip = pos - 1;
                }
            }
            else if (Objects.equals(op, OpNull.getValue())) {
                var err = push(NULL);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpSetGlobal.getValue())) {
                var globalIndex = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                globals.add(globalIndex, pop());
            }
            else if (Objects.equals(op, OpGetGlobal.getValue())) {
                var globalIndex = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                var err = push(globals.get(globalIndex));
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpArray.getValue())) {
                var numElements = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                var array = buildArray(sp - numElements, sp);
                sp = sp - numElements;

                var err = push(array);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpHash.getValue())) {
                var numElements = Code.ReadUint16(ins,ip + 1);
                currentFrame().ip += 2;

                var result = buildHash(sp - numElements, sp);
                if (!Objects.equals(result.second.Message, "")) {
                    return result.second;
                }
                sp = sp - numElements;

                var err = push(result.first);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpIndex.getValue())) {
                var index = pop();
                var left = pop();

                var err = executeIndexExpression(left, index);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpCall.getValue())) {
                var numArgs = Code.ReadUint8(ins,ip + 1);
                currentFrame().ip += 1;

                var err = executeCall(numArgs);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpReturnValue.getValue())) {
                var returnValue = pop();

                var frame = popFrame();
                sp = frame.basePointer - 1;

                var err = push(returnValue);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpReturn.getValue())) {
                var frame = popFrame();
                sp = frame.basePointer - 1;

                var err = push(new Object.Null());
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpSetLocal.getValue())) {
                var localIndex = Code.ReadUint8(ins,ip + 1);
                currentFrame().ip += 1;

                var frame = currentFrame();

                stack.add(frame.basePointer + localIndex, pop());
            }
            else if (Objects.equals(op, OpGetLocal.getValue())) {
                var localIndex = Code.ReadUint8(ins,ip + 1);
                currentFrame().ip += 1;

                var frame = currentFrame();

                var err = push(stack.get(frame.basePointer + localIndex));
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpGetBuiltin.getValue())) {
                var builtinIndex = Code.ReadUint8(ins,ip + 1);
                currentFrame().ip += 1;

                var definition = Builtins.builtins.get(builtinIndex);

                var err = push(definition.Builtin);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpClosure.getValue())) {
                var closureConstIndex = Code.ReadUint16(ins,ip + 1);
                var numFree = Code.ReadUint8(ins,ip + 3);
                currentFrame().ip += 3;

                var err = pushClosure(closureConstIndex, numFree);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpGetFree.getValue())) {
                var freeIndex = Code.ReadUint8(ins,ip + 1);
                currentFrame().ip += 1;

                var currentClosure = currentFrame().cl;
                var err = push(currentClosure.Free.get(freeIndex));
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }
            else if (Objects.equals(op, OpCurrentClosure.getValue())) {
                var currentClosure = currentFrame().cl;
                var err = push(currentClosure);
                if (!Objects.equals(err.Message, "")) {
                    return err;
                }
            }

        }

        return ERROR;
    }

    public Object.Error push(Object o) {
        if(sp >= StackSize) {
            return new Object.Error("stack overflow");
        }

        stack.add(sp,o);

        sp++;

        return ERROR;
    }

    public Object pop() {
        var o = stack.get(sp - 1);
        sp--;
        return o;
    }

    public Object.Error executeBinaryOperation(byte op) {
        var right = pop();
        var left = pop();

        var leftType = left.Type();
        var rightType = right.Type();

        if(Objects.equals(leftType, Object.INTEGER_OBJ) && Objects.equals(rightType, Object.INTEGER_OBJ)) {
            return executeBinaryIntegerOperation(op,left,right);
        }
        if(Objects.equals(leftType, Object.STRING_OBJ) && Objects.equals(rightType, Object.STRING_OBJ)) {
            return executeBinaryStringOperation(op,left,right);
        }
        return new Object.Error(String.format("unsupported types for binary operation: %s %s",
                        leftType,rightType));
    }

    public Object.Error executeBinaryIntegerOperation(byte op,Object left,Object right) {
        var leftValue = ((Object.Integer)left).Value;
        var rightValue = ((Object.Integer)right).Value;

        int result;

        if (Objects.equals(op, OpAdd.getValue())) {
            result = leftValue + rightValue;
        }
        else if (Objects.equals(op, OpSub.getValue())) {
            result = leftValue - rightValue;
        }
        else if (Objects.equals(op, OpMul.getValue())) {
            result = leftValue * rightValue;
        }
        else if (Objects.equals(op, OpDiv.getValue())) {
            result = leftValue / rightValue;
        }
        else {
            return new Object.Error(String.format("unknown integer operator: %d", op));
        }


        return push(new Object.Integer(result));
    }

    public Object.Error executeComparison(byte op) {
        var right = pop();
        var left = pop();

        if(Objects.equals(left.Type(), Object.INTEGER_OBJ) && Objects.equals(right.Type(), Object.INTEGER_OBJ)) {
            return executeIntegerComparison(op,left,right);
        }
        else if(Objects.equals(left.Type(), Object.CHAR_OBJ) && Objects.equals(right.Type(), Object.CHAR_OBJ)) {
            return executeCharComparison(op,left,right);
        }
        else if(op == OpEqual.getValue()) {
            return push(nativeBoolToBooleanObject(left == right));
        }
        else if(op == OpNotEqual.getValue()) {
            return push(nativeBoolToBooleanObject(left != right));
        }
        else {
            return new Object.Error(String.format("unknown operator: %d (%s %s)",
                    op, left.Type(), right.Type()));}
    }

    public Object.Error executeIntegerComparison(byte op,Object left,Object right) {
        var leftValue = ((Object.Integer)left).Value;
        var rightValue = ((Object.Integer)right).Value;

        if (Objects.equals(op, OpEqual.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue == rightValue));
        }
        else if (Objects.equals(op, OpNotEqual.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue != rightValue));
        }
        else if (Objects.equals(op, OpGreaterThan.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue > rightValue));
        }
        else if(Objects.equals(op, OpGreater.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue >= rightValue));
        }
        else {
            return new Object.Error(String.format("unknown operator: %d", op));
        }
    }

    public Object.Error executeCharComparison(byte op,Object left,Object right) {
        var leftValue = ((Object.Char)left).Value;
        var rightValue = ((Object.Char)right).Value;

        if (Objects.equals(op, OpEqual.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue == rightValue));
        }
        else if (Objects.equals(op, OpNotEqual.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue != rightValue));
        }
        else if (Objects.equals(op, OpGreaterThan.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue > rightValue));
        }
        else if(Objects.equals(op, OpGreater.getValue())) {
            return push(nativeBoolToBooleanObject(leftValue >= rightValue));
        }
        else {
            return new Object.Error(String.format("unknown operator: %d", op));
        }
    }

    public Object.Error executeBangOperator() {
        var operand = pop();

        if(Objects.equals(operand, TRUE)) {
            return push(FALSE);
        }
        if(Objects.equals(operand, FALSE)) {
            return push(TRUE);
        }
        if(Objects.equals(operand, NULL)) {
            return push(TRUE);
        }

        return push(FALSE);
    }

    public Object.Error executeMinusOperator() {
        var operand = pop();

        if(!Objects.equals(operand.Type(), Object.INTEGER_OBJ)) {
            return new Object.Error(String.format("unsupported type for negation: %s",operand.Type()));
        }

        var value = ((Object.Integer)operand).Value;
        return push(new Object.Integer(-value));
    }

    public Object.Error executeBinaryStringOperation(byte op,Object left,Object right) {
        if(!Objects.equals(op, OpAdd.getValue())) {
            return new Object.Error(String.format("unknown string operator: %d",op));
        }

        var leftValue = ((Object.String)left).Value;
        var rightValue = ((Object.String)right).Value;

        return push(new Object.String(leftValue + rightValue));
    }

    public Object buildArray(int startIndex,int endIndex) {
        var elements = new ArrayList<Object>(endIndex - startIndex);

        for(int i = startIndex;i < endIndex;i++) {
            elements.add(i - startIndex,stack.get(i));
        }

        return new Object.Array(elements);
    }

    public Tuple<Object, Object.Error> buildHash(int startIndex,int endIndex) {
        var hashedPairs = new HashMap<Object.HashKey,Object.HashPair>();

        for(int i = startIndex;i < endIndex;i += 2) {
            var key = stack.get(i);
            var value = stack.get(i + 1);

            var pair = new Object.HashPair(key,value);

            if(!(key instanceof Object.Hashable hashKey)) {
                return new Tuple<>(null, new Object.Error(String.format("unusable as hash key: %s", key.Type())));
            }

            hashedPairs.put(hashKey.Hashkey(),pair);
        }

        return new Tuple<>(new Object.Hash(hashedPairs), ERROR);
    }

    public Object.Error executeIndexExpression(Object left,Object index) {
        if(Objects.equals(left.Type(), Object.ARRAY_OBJ) && Objects.equals(index.Type(), Object.INTEGER_OBJ)) {
            return executeArrayIndex(left,index);
        }
        if(Objects.equals(left.Type(), Object.HASH_OBJ)) {
            return executeHashIndex(left,index);
        }
        return new Object.Error(String.format("index operator not supported: %s",left.Type()));
    }

    public Object.Error executeArrayIndex(Object array,Object index) {
        var arrayObject = (Object.Array)array;
        var i = ((Object.Integer)index).Value;
        var max = arrayObject.Elements.size() - 1;

        if(i < 0 || i > max) {
            return push(NULL);
        }

        return push(arrayObject.Elements.get(i));
    }

    public Object.Error executeHashIndex(Object hash,Object index) {
        var hashObject = (Object.Hash)hash;

        if(!(index instanceof Object.Hashable key)) {
            return new Object.Error(String.format("unusable as hash key: %s",index.Type()));
        }

        if(!hashObject.Pairs.containsKey(key.Hashkey())) {
            return push(NULL);
        }
        var pair = hashObject.Pairs.get(key.Hashkey());

        return push(pair.Value);
    }

    public Frame currentFrame() {
        return frames.get(frameIndex - 1);
    }

    public void pushFrame(Frame f) {
        frames.add(frameIndex,f);
        frameIndex++;
    }

    public Frame popFrame() {
        frameIndex--;
        return frames.get(frameIndex);
    }

    public Object.Error executeCall(int numArgs) {
        var callee = stack.get(sp - 1 - numArgs);
        if(callee instanceof Object.Closure) {
            return callClosure((Object.Closure)callee,numArgs);
        }
        if(callee instanceof Object.Builtin) {
            return callBuiltin((Object.Builtin)callee,numArgs);
        }
        return new Object.Error("calling non-closure and non-builtin");
    }

    public Object.Error callClosure(Object.Closure cl,int numArgs) {
        if(!Objects.equals(numArgs,cl.Fn.Numparameters)) {
            return new Object.Error(String.format("wrong number of arguments: want=%d, got=%d",
                        cl.Fn.Numparameters,numArgs));
        }

        var frame = new Frame(cl,sp - numArgs);
        pushFrame(frame);

        sp = frame.basePointer + cl.Fn.Numlocals;

        return ERROR;
    }

    public Object.Error callBuiltin(Object.Builtin builtin,int numArgs) {
        var args = new ArrayList<>(stack.subList(sp - numArgs,sp));

        var result = builtin.Fn.Fn(args.toArray(new Object[0]));
        sp = sp - numArgs - 1;

        if(!Objects.equals(result,new Object.Null())) {
            push(result);
        }
        else {
            push(new Object.Null());
        }

        return ERROR;
    }

    public Object.Error pushClosure(int constIndex,int numFree) {
        var constant = constants.get(constIndex);
        if(!(constant instanceof Object.CompiledFunction function)) {
            return new Object.Error(String.format("not a function: " + constant));
        }

        var free = new ArrayList<Object>(numFree);
        for(int i = 0;i < numFree;i++) {
            free.add(i,stack.get(sp - numFree + i));
        }
        sp = sp - numFree;

        var closure = new Object.Closure(function,free);
        return push(closure);
    }

    public Object.Boolean nativeBoolToBooleanObject(boolean input) {
        if(input) {
            return TRUE;
        }
        else {
            return FALSE;
        }
    }

    public boolean isTruthy(Object obj) {
        if(obj instanceof Object.Boolean) {
            return ((Object.Boolean)obj).Value;
        }
        return !(obj instanceof Object.Null);
    }

    public static class Tuple<A,B> {
        public A first;
        public B second;

        public Tuple(A a,B b) {
            first = a;
            second = b;
        }
    }
}
