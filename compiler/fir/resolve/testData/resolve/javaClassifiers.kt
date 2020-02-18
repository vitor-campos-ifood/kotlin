// FILE: Owner.java

public class Owner {
    public static class Nested {
        public static class Son {
            public static class GrandSon {
                public static int field = 42;
            }
        }
    }
}

// FILE: test.kt

fun test() {
    val x = Owner.Nested.Son.GrandSon.field
}