package no.nav.paw.config.kafka.streams

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.paw.config.kafka.KafkaAuthenticationConfig
import no.nav.paw.config.kafka.KafkaConfig
import no.nav.paw.config.kafka.KafkaSchemaRegistryConfig
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.streams.StreamsConfig
import java.util.*

class KafkaStreamsFactory(applicationIdSuffix: String, config: KafkaConfig) {
    private val schemaRegistry = config.schemaRegistry?.let { schemaRegistryConfig(it) }.orEmpty()
    private val authentication = config.authentication?.let { authenticationConfig(it) }.orEmpty()
    private val baseProperties =
        mapOf(
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to config.brokers,
            StreamsConfig.APPLICATION_ID_CONFIG to ("${config.applicationIdPrefix}_$applicationIdSuffix")
        ) +
            schemaRegistry +
            authentication

    val properties =
        Properties().apply {
            putAll(baseProperties)
        }

    fun <T : SpecificRecord> createSpecificAvroSerde(): SpecificAvroSerde<T> =
        SpecificAvroSerde<T>().apply {
            configure(schemaRegistry, false)
        }

    private fun authenticationConfig(config: KafkaAuthenticationConfig): Map<String, Any> =
        mapOf(
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to config.truststorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to config.credstorePassword,
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to config.keystorePath,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to config.credstorePassword,
            SslConfigs.SSL_KEY_PASSWORD_CONFIG to config.credstorePassword,
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to ""
        )

    private fun schemaRegistryConfig(config: KafkaSchemaRegistryConfig): Map<String, Any> =
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
