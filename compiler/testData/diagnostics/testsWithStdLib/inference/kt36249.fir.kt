// !DIAGNOSTICS: -UNUSED_PARAMETER UNUSED_VARIABLE -UNUSED_EXPRESSION

import kotlin.reflect.KClass
fun <T : PsiElement> select(vararg classes: KClass<out T>): T? {
    return null
}
interface PomRenameableTarget
interface PsiElement
interface PsiMethod : PsiElement, PomRenameableTarget
interface PsiClass : PsiElement, PomRenameableTarget
class A {
    val context get() = select(PsiMethod::class, PsiClass::class) // NI: {PomRenameableTarget & PsiElement}?
}
fun main() {
    val x = A().context // Any? in NI, PsiElement? in OI
    x
}
