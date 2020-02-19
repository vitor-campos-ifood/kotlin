// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-268
 * PLACE: overload-resolution, building-the-overload-candidate-set-ocs, operator-call -> paragraph 2 -> sentence 3
 * RELEVANT PLACES: overload-resolution, building-the-overload-candidate-set-ocs, call-with-an-explicit-receiver -> paragraph 6 -> sentence 1
 * overload-resolution, building-the-overload-candidate-set-ocs, operator-call -> paragraph 4 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: Non-extension member callables
 */


// FILE: LibCase1.kt
// TESTCASE NUMBER: 1, 2
package libPackage

import testPackCase1.Case
import testPackCase1.Case.Inv
import testPackCase1.Case.E

operator fun Case.E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) =  Inv()
operator fun Case.Inv.<!EXTENSION_SHADOWED_BY_MEMBER!>invoke<!>(i: Int) = 1


// FILE: TestCase1.kt
// TESTCASE NUMBER: 1, 2
package testPackCase1
import libPackage.plus
import libPackage.*
import libPackage.invoke

class Case() {

    class E(val plus: Inv? = null) {
        operator fun plus(value: Int) = Case()
    }

    class Inv() {
        operator fun invoke(value: Int) = Case()
    }

    fun foo(e: E) {
        operator fun E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Case()

        run {
            <!DEBUG_INFO_AS_CALL("fqName: testPackCase1.Case.E.plus; typeCall: operator function")!>e+1<!>
        }
        <!DEBUG_INFO_AS_CALL("fqName: testPackCase1.Case.E.plus; typeCall: operator function")!>e+1<!>
        <!DEBUG_INFO_AS_CALL("fqName: testPackCase1.Case.E.plus; typeCall: operator function")!>e.plus(1)<!>
        <!DEBUG_INFO_AS_CALL("fqName: testPackCase1.Case.Inv.invoke; typeCall: operator function")!>e.plus?.invoke(1)<!> //ok
    }
}

// FILE: LibCase1.kt
// TESTCASE NUMBER: 3, 4
package libPackage
import testPackCase3.Case
import testPackCase3.Case.Inv
import testPackCase3.Case.E

operator fun Case.E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Inv()
operator fun Case.Inv.<!EXTENSION_SHADOWED_BY_MEMBER!>invoke<!>(i: Int) = 1


// FILE: TestCase3.kt
// TESTCASE NUMBER: 3, 4
package testPackCase3
import libPackage.plus
import libPackage.*
import libPackage.invoke

class Case() {

    class E(val plus: Inv? = null) {
        operator fun plus(value: Int) = Case()
    }

    class Inv() {
        operator fun invoke(value: Int) = Case()
    }

    fun foo(e: E) {
        operator fun E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Case()

        run {
            operator fun E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Case()

            <!DEBUG_INFO_AS_CALL("fqName: testPackCase3.Case.E.plus; typeCall: operator function")!>e+1<!>
        }
        <!DEBUG_INFO_AS_CALL("fqName: testPackCase3.Case.E.plus; typeCall: operator function")!>e+1<!>
    }
    fun boo(e: E) {
        /*operator*/ fun E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Case()

        run {
            /*operator*/ fun E.<!EXTENSION_SHADOWED_BY_MEMBER!>plus<!>(value: Int) = Case()

            <!DEBUG_INFO_AS_CALL("fqName: testPackCase3.Case.E.plus; typeCall: operator function")!>e+1<!>
        }
        <!DEBUG_INFO_AS_CALL("fqName: testPackCase3.Case.E.plus; typeCall: operator function")!>e+1<!>
    }
}