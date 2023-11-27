package no.nav.paw.kafka

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.Serializer
import java.util.*

data class KafkaConfig(
    val brokers: String,
    val authentication: KafkaAuthenticationConfig? = null,
    val schemaRegistry: SchemaRegistryConfig? = null
)

data class KafkaAuthenticationConfig(
    val truststorePath: String,
    val keystorePath: String,
    val credstorePsw: String
)

data class SchemaRegistryConfig(
    val url: String,
    val username: String?,
    val password: String?,
    val autoRegisterSchema: Boolean = true,
    val kafkaAvroSpecificReaderConfig: Boolean = true
)

class KafkaFactory(private val config: KafkaConfig) {
    val baseProperties =
        Properties().apply {
            this[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = config.brokers
            this[ProducerConfig.ACKS_CONFIG] = "all"
            this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            config.authentication?.let { putAll(authenticationConfig(it)) }
            config.schemaRegistry?.let { putAll(schemaRegistryConfig(it)) }
        }

    fun <K : Any, V : Any> createProducer(
        clientId: String,
        keySerializer: Serializer<K>,
        valueSerializer: Serializer<V>
    ): Producer<K, V> =
        KafkaProducer(
            baseProperties +
                mapOf(
                    ProducerConfig.CLIENT_ID_CONFIG to clientId,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to keySerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to valueSerializer::class.java
                ),
            keySerializer,
            valueSerializer
        )

    private fun authenticationConfig(config: KafkaAuthenticationConfig): Map<String, Any> =
        mapOf(
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to config.truststorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to config.credstorePsw,
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to config.keystorePath,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to config.credstorePsw,
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to config.credstorePsw,
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to ""
        )

    private fun schemaRegistryConfig(config: SchemaRegistryConfig): Map<String, Any> =
        mapOf(
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to config.url,
            SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            SchemaRegistryClientConfig.USER_INFO_CONFIG to "${config.username}:${config.password}",
            KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS to config.autoRegisterSchema,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to config.kafkaAvroSpecificReaderConfig
        ).apply {
            config.username?.let {
                SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO"
                SchemaRegistryClientConfig.USER_INFO_CONFIG to "${config.username}:${config.password}"
            }
        }
}

operator fun Properties.plus(other: Map<String, Any>): Properties =
    Properties().apply {
        putAll(this@plus)
        putAll(other)
    }
