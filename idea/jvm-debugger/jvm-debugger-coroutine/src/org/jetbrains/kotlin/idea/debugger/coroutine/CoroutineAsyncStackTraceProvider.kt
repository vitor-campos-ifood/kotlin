/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.debugger.coroutine

import com.intellij.debugger.engine.*
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.jdi.StackFrameProxyImpl
import com.intellij.xdebugger.frame.XSuspendContext
import com.sun.jdi.*
import org.jetbrains.kotlin.codegen.coroutines.CONTINUATION_VARIABLE_NAME
import org.jetbrains.kotlin.idea.debugger.*
import org.jetbrains.kotlin.idea.debugger.coroutine.data.CoroutineStackFrameItem
import org.jetbrains.kotlin.idea.debugger.coroutine.proxy.AsyncStackTraceContext
import org.jetbrains.kotlin.idea.debugger.evaluate.ExecutionContext

class CoroutineAsyncStackTraceProvider : AsyncStackTraceProvider {

    override fun getAsyncStackTrace(stackFrame: JavaStackFrame, suspendContext: SuspendContextImpl): List<CoroutineStackFrameItem>? {
        val stackFrameList = hopelessAware { getAsyncStackTraceSafe(stackFrame.stackFrameProxy, suspendContext) }
        return if (stackFrameList == null || stackFrameList.isEmpty()) null else stackFrameList
    }

    fun getAsyncStackTraceSafe(frameProxy: StackFrameProxyImpl, suspendContext: XSuspendContext): List<CoroutineStackFrameItem> {
        var continuationContext = asyncStackTraceContext(frameProxy, suspendContext)
        if (continuationContext is AsyncStackTraceContext)
            return continuationContext.getAsyncStackTraceIfAny()
        return emptyList();
    }

    fun asyncStackTraceContext(
        frameProxy: StackFrameProxyImpl,
        suspendContext: XSuspendContext?
    ): AsyncStackTraceContext? {
        if(suspendContext is XSuspendContext)
            return ContinuationContext(frameProxy, suspendContext).findAsyncContext()
        return null
    }
}

class ContinuationContext(val frameProxy: StackFrameProxyImpl, val suspendContext: XSuspendContext) {

    fun findAsyncContext(): AsyncStackTraceContext? {
        val location = frameProxy.location()
        if (!location.isInKotlinSources())
            return null

        val method = location.safeMethod() ?: return null
        val threadReference = frameProxy.threadProxy().threadReference

        if (threadReference == null || !threadReference.isSuspended || !canRunEvaluation(suspendContext))
            return null

        val evaluationContext = EvaluationContextImpl(suspendContext as SuspendContextImpl, frameProxy)
        val context = ExecutionContext(evaluationContext, frameProxy)

        val continuation = locateContinuation(context, method)
        if (continuation is ObjectReference)
            return AsyncStackTraceContext(context, method, continuation)
        return null
    }

    private fun canRunEvaluation(suspendContext: XSuspendContext) =
        (suspendContext as SuspendContextImpl).debugProcess.canRunEvaluation

    private fun locateContinuation(context: ExecutionContext, method: Method): ObjectReference? {
        val continuation: ObjectReference?
        if (isInvokeSuspendMethod(method)) {
            continuation = context.frameProxy.thisObject() ?: return null
            if (!isSuspendLambda(continuation.referenceType()))
                return null
        } else if (isContinuationProvider(method)) {
            val frameProxy = context.frameProxy
            val continuationVariable = frameProxy.safeVisibleVariableByName(CONTINUATION_VARIABLE_NAME) ?: return null
            continuation = frameProxy.getValue(continuationVariable) as? ObjectReference ?: return null
            context.keepReference(continuation)
        } else {
            continuation = null
        }
        return continuation
    }

    private fun isInvokeSuspendMethod(method: Method): Boolean =
        method.name() == "invokeSuspend" && method.signature() == "(Ljava/lang/Object;)Ljava/lang/Object;"

    private fun isContinuationProvider(method: Method): Boolean =
        "Lkotlin/coroutines/Continuation;)" in method.signature()

    private fun isSuspendLambda(referenceType: ReferenceType): Boolean =
        SUSPEND_LAMBDA_CLASSES.any { referenceType.isSubtype(it) }

}
