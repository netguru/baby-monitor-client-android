package co.netguru.baby.monitor

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock

object TestUtils {
    inline fun <reified T : Any, reified R : Any> mockBuilder(mockResult: R) =
        mock<T>(defaultAnswer = AnswerWithPair(
            T::class.java,
            R::class.java to mockResult
        )
        )

    fun <T : Any> verifyInOrder(mock: T, body: T.() -> Unit) =
        with(inOrder(mock)) { verify(mock).body() }
}
