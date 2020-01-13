package co.netguru.baby.monitor

import org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

class AnswerWithPair<T : Any>(
    private val clazz: Class<*>,
    private val pair: Pair<Class<T>, T>
) : Answer<Any> {
    private val delegate = ReturnsEmptyValues()

    @Throws(Throwable::class)
    override fun answer(invocation: InvocationOnMock): Any = invocation.apply {
        return when (method.returnType) {
            clazz -> mock
            pair.first -> pair.second
            else -> delegate.answer(this)
        }
    }
}
