package no.nav.paw.config.kafka

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory

class NonBlockingKafkaProducer<K, V>(
    private val kafkaProducerClient: KafkaProducer<K, V>
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

private val logger = LoggerFactory.getLogger(NonBlockingKafkaProducer::class.java)

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
