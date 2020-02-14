/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.commonizer.utils

import java.util.WeakHashMap

internal class NonThreadSafeInterner<T : Any> {
    private val pool = WeakHashMap<T, T>()
    private var invocations: Long = 0
    private var internments: Long = 0

    val internmentRatio: Double
        get() = if (invocations > 0) internments.toDouble() / invocations else .0

    fun intern(value: T): T {
        val interned = pool.computeIfAbsent(value) { value }
        invocations++
        if (interned !== value) internments++

        return interned
    }
}
