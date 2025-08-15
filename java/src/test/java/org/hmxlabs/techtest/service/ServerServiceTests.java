package org.hmxlabs.techtest.service;

import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.component.Server;
import org.hmxlabs.techtest.server.component.impl.ServerImpl;
import org.hmxlabs.techtest.server.mapper.ServerMapperConfiguration;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.hmxlabs.techtest.server.service.DataBodyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;



import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hmxlabs.techtest.TestDataHelper.createTestDataEnvelopeApiObject;
import static org.hmxlabs.techtest.TestDataHelper.createTestDataEnvelopeApiObjectWithInvalidChecksum;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTests {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataBodyEntity expectedDataBodyEntity2;
    private DataEnvelope testDataEnvelope;
    private DataEnvelope testDataEnvelopeInvalidChecksum;

    private Server server;

    @BeforeEach
    public void setup() {
        ServerMapperConfiguration serverMapperConfiguration = new ServerMapperConfiguration();
        modelMapper = serverMapperConfiguration.createModelMapperBean();

        testDataEnvelope = createTestDataEnvelopeApiObject();
        expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        testDataEnvelopeInvalidChecksum = createTestDataEnvelopeApiObjectWithInvalidChecksum();
        expectedDataBodyEntity2 = modelMapper.map(testDataEnvelopeInvalidChecksum.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity2.setDataHeaderEntity(modelMapper.map(testDataEnvelopeInvalidChecksum.getDataHeader(), DataHeaderEntity.class));

        server = new ServerImpl(dataBodyServiceImplMock, modelMapper);
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected() {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldNotSaveDataEnvelopeInvalidChecksum() {
        boolean success = server.saveDataEnvelope(testDataEnvelopeInvalidChecksum);

        assertThat(success).isFalse();
        verify(dataBodyServiceImplMock, never()).saveDataBody(eq(expectedDataBodyEntity2));
    }
}
