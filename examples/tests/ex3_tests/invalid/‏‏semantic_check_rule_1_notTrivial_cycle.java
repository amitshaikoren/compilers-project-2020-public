class Main {
    public static void main(String[] args) {
        System.out.println(1);
    }
}

class Shared {
    int theThing;

    public int theThing() {
        return theThing;
    }
}

class A extends C{ }

class B extends A {
    public int theThing() {
        return theThing;
    }
}

class C extends B {
    public int theThing() {
        return theThing;
    }
}

class D extends A{
    public int theThing() {
        int theThing;

        return theThing;
    }
}

class E extends A {
    public int theThing(int theThing) {
        return theThing;
    }
}
