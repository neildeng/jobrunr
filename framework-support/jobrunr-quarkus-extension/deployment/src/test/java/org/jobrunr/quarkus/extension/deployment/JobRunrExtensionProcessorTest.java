package org.jobrunr.quarkus.extension.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;
import org.jobrunr.quarkus.autoconfigure.JobRunrConfiguration;
import org.jobrunr.quarkus.autoconfigure.JobRunrProducer;
import org.jobrunr.quarkus.autoconfigure.JobRunrStarter;
import org.jobrunr.quarkus.autoconfigure.health.JobRunrHealthCheck;
import org.jobrunr.quarkus.autoconfigure.storage.JobRunrElasticSearchStorageProviderProducer;
import org.jobrunr.quarkus.autoconfigure.storage.JobRunrInMemoryStorageProviderProducer;
import org.jobrunr.quarkus.autoconfigure.storage.JobRunrMongoDBStorageProviderProducer;
import org.jobrunr.quarkus.autoconfigure.storage.JobRunrSqlStorageProviderProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JobRunrExtensionProcessorTest {

    @Mock
    Capabilities capabilities;

    @Mock
    JobRunrConfiguration jobRunrConfiguration;

    JobRunrExtensionProcessor jobRunrExtensionProcessor;

    @BeforeEach
    void setUpExtensionProcessor() {
        jobRunrExtensionProcessor = new JobRunrExtensionProcessor();
        lenient().when(capabilities.isPresent(Capability.JSONB)).thenReturn(true);
    }

    @Test
    void producesJobRunrProducer() {
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrProducer.class.getName())
                .contains(JobRunrStarter.class.getName())
                .contains(JobRunrInMemoryStorageProviderProducer.class.getName())
                .contains(JobRunrProducer.JobRunrJsonBJsonMapperProducer.class.getName());
    }

    @Test
    void producesJobRunrProducerUsesJSONBIfCapabilityPresent() {
        Mockito.reset(capabilities);
        lenient().when(capabilities.isPresent(Capability.JSONB)).thenReturn(true);
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrProducer.JobRunrJsonBJsonMapperProducer.class.getName());
    }

    @Test
    void producesJobRunrProducerUsesJacksonIfCapabilityPresent() {
        Mockito.reset(capabilities);
        lenient().when(capabilities.isPresent(Capability.JACKSON)).thenReturn(true);
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrProducer.JobRunrJacksonJsonMapperProducer.class.getName());
    }

    @Test
    void producesJobRunrProducerUsesSqlStorageProviderIfAgroalCapabilityIsPresent() {
        lenient().when(capabilities.isPresent(Capability.AGROAL)).thenReturn(true);
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrSqlStorageProviderProducer.class.getName());
    }

    @Test
    void producesJobRunrProducerUsesMongoDBStorageProviderIfMongoDBClientCapabilityIsPresent() {
        lenient().when(capabilities.isPresent(Capability.MONGODB_CLIENT)).thenReturn(true);
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrMongoDBStorageProviderProducer.class.getName());
    }

    @Test
    void producesJobRunrProducerUsesElasticSearchStorageProviderIfElasticSearchRestHighLevelClientCapabilityIsPresent() {
        lenient().when(capabilities.isPresent(Capability.ELASTICSEARCH_REST_HIGH_LEVEL_CLIENT)).thenReturn(true);
        final AdditionalBeanBuildItem additionalBeanBuildItem = jobRunrExtensionProcessor.produce(capabilities);

        assertThat(additionalBeanBuildItem.getBeanClasses())
                .contains(JobRunrElasticSearchStorageProviderProducer.class.getName());
    }

    @Test
    void addHealthCheckAddsHealthBuildItemIfSmallRyeHealthCapabilityIsPresent() {
        lenient().when(capabilities.isPresent(Capability.SMALLRYE_HEALTH)).thenReturn(true);
        final HealthBuildItem healthBuildItem = jobRunrExtensionProcessor.addHealthCheck(capabilities, jobRunrConfiguration);

        assertThat(healthBuildItem.getHealthCheckClass())
                .isEqualTo(JobRunrHealthCheck.class.getName());
    }

    @Test
    void addHealthCheckDoesNotAddHealthBuildItemIfSmallRyeHealthCapabilityIsNotPresent() {
        lenient().when(capabilities.isPresent(Capability.SMALLRYE_HEALTH)).thenReturn(false);
        final HealthBuildItem healthBuildItem = jobRunrExtensionProcessor.addHealthCheck(capabilities, jobRunrConfiguration);

        assertThat(healthBuildItem).isNull();
    }

}