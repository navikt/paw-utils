package no.nav.paw.config.kafka

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory

class NonBlockingProducer<K, V>(
    private val kafkaProducerClient: Producer<K, V>
) {
    fun send(record: ProducerRecord<K, V>): Deferred<RecordMetadata> {
        val deferred = CompletableDeferred<RecordMetadata>()
        kafkaProducerClient.send(record) { metadata, exception ->
            if (exception != null) {
                deferred.completeExceptionally(exception)
            } else {
                deferred.complete(metadata)
            }
        }
        return deferred
    }
}

private val logger = LoggerFactory.getLogger(NonBlockingProducer::class.java)

suspend fun Deferred<RecordMetadata>.awaitAndLog(messageDescription: String) {
    val recordMetadata = await()
    logger.trace(
        "Sendte meldingen: beskrivelse={}, topic={}, partition={}, offset={}",
        messageDescription,
        recordMetadata.topic(),
        recordMetadata.partition(),
        recordMetadata.offset()
    )
}
