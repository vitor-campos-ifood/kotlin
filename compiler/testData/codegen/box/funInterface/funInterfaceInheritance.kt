// !LANGUAGE: +NewInference +FunctionalInterfaceConversion +SamConversionPerArgument +SamConversionForKotlinFunctions

// IGNORE_BACKEND: JVM, JVM_IR, JS_IR
// IGNORE_BACKEND_FIR: JVM_IR
// SKIP_DCE_DRIVEN

fun interface Base {
    fun invoke(): String
}

fun interface I : Base

fun interface Proxy : I {

    override fun invoke(): String = invokeInt().toString()

    fun invokeInt(): Int
}

fun runBase(b: Base) = b.invoke()

fun runI(i: I) = i.invoke()

fun runProxy(p: Proxy) = p.invoke()

fun box(): String {

    if (runI { "i" } != "i") return "fail1"

    if (runProxy { 10 } != "10") return "fail2"

    return runBase { "OK" }
}