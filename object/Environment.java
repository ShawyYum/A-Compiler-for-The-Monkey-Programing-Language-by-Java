package object;

import java.util.HashMap;
import java.util.Objects;

public class Environment {
    public static class environment {
        public HashMap<String,Object.object> store;
        public environment outer;

        public environment() {
            store = new HashMap<>();
            outer = null;
        }

        public environment NewEnclosedEnvironment(environment outer) {
            var env = new environment();
            this.outer = outer;
            return env;
        }

        public Object.Tuple Get(String name) {
            var ok = store.containsKey(name);
            Object.object obj = null;

            if(ok) {
                obj = store.get(name);
            }

            if(!ok && outer != null) {
                ok = outer.store.containsKey(name);
                obj = outer.Get(name);
            }

            return new Object.Tuple(Objects.requireNonNullElseGet(obj, Object.Null::new), ok);
        }

        public Object.object Set(String name,Object.object val) {
            store.put(name,val);
            return val;
        }
    }
}