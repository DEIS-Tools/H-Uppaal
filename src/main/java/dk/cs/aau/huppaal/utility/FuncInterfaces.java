package dk.cs.aau.huppaal.utility;

public class FuncInterfaces {
    @FunctionalInterface
    public interface Runnable1<T1> {
        void run(T1 t1);
    }
    @FunctionalInterface
    public interface Runnable2<T1,T2> {
        void run(T1 t1, T2 t2);
    }

    @FunctionalInterface
    public interface Action<R> {
        R run();
    }
    @FunctionalInterface
    public interface Action1<R,T1> {
        R run(T1 t1);
    }
    @FunctionalInterface
    public interface Action2<R,T1,T2> {
        R run(T1 t1, T2 t2);
    }
}
