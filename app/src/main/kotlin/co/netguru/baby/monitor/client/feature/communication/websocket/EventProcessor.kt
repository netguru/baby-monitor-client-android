package co.netguru.baby.monitor.client.feature.communication.websocket

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import timber.log.Timber

class EventProcessor(private val gson: Gson) {

    fun process(message: String?): MessageAction? {
        message ?: return null

        return try {
            val messageCommand: MessageCommand = gson.fromJson(message, MessageCommand::class.java)
            messageCommand.action
        } catch (exception: JsonSyntaxException) {
            Timber.e("Exception when processing event: $exception")
            null
        }
    }

}
