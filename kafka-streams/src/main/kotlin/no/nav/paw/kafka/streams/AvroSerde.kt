package no.nav.paw.kafka.streams

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.streams.StreamsConfig
import java.util.*

data class KafkaStreamsConfig(
    val brokers: String,
    val applicationId: String,
    val authentication: KafkaStreamsAuthenticationConfig? = null,
    val schemaRegistry: KafkaStreamsSchemaRegistryConfig? = null
)

data class KafkaStreamsSchemaRegistryConfig(
    val url: String,
    val username: String?,
    val password: String?,
    val autoRegisterSchema: Boolean = true,
    val avroSpecificReaderConfig: Boolean = true
)

data class KafkaStreamsAuthenticationConfig(
    val truststorePath: String,
    val keystorePath: String,
    val credstorePsw: String
)

class KafkaFactory(private val config: KafkaStreamsConfig) {
    val schemaRegistry = config.schemaRegistry?.let { schemaRegistryConfig(it) }.orEmpty()
    val authentication = config.authentication?.let { authenticationConfig(it) }.orEmpty()
    private val baseProperties =
        mapOf(
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to config.brokers,
            StreamsConfig.APPLICATION_ID_CONFIG to config.applicationId
        ) +
            schemaRegistry +
            authentication

    val properties = Properties().apply {
        putAll(baseProperties)
    }

    fun <T : SpecificRecord> createSpecificAvroSerde(): SpecificAvroSerde<T> =
        SpecificAvroSerde<T>().apply {
            configure(schemaRegistry, false)
        }

    private fun authenticationConfig(config: KafkaStreamsAuthenticationConfig): Map<String, Any> =
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

    private fun schemaRegistryConfig(config: KafkaStreamsSchemaRegistryConfig): Map<String, Any> =
        mapOf(
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to config.url,
            SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            SchemaRegistryClientConfig.USER_INFO_CONFIG to "${config.username}:${config.password}",
            KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS to config.autoRegisterSchema,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to config.avroSpecificReaderConfig
        ).apply {
            config.username?.let {
                SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO"
                SchemaRegistryClientConfig.USER_INFO_CONFIG to "${config.username}:${config.password}"
            }
        }
}
