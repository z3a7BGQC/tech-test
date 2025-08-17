package org.hmxlabs.techtest.service;

import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.component.Server;
import org.hmxlabs.techtest.server.component.impl.ServerImpl;
import org.hmxlabs.techtest.server.mapper.ServerMapperConfiguration;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.hmxlabs.techtest.server.service.DataBodyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static org.hmxlabs.techtest.TestDataHelper.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTests {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataBodyEntity expectedDataBodyEntity2;
    private DataBodyEntity expectedDataBodyEntity3;
    private DataEnvelope testDataEnvelope;
    private DataEnvelope testDataEnvelopeInvalidChecksum;
    private DataEnvelope testDataEnvelopeNullChecksum;
    private List<DataEnvelope> testDataEnvelopeList;
    private List<DataBodyEntity> testDataBodyEntityList;
    private final String TEST_BLOCKTYPEB_LOWERCASE = "blocktypeb";

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

        testDataEnvelopeNullChecksum = createTestDataEnvelopeApiObjectNullChecksum();
        expectedDataBodyEntity3 = modelMapper.map(testDataEnvelopeNullChecksum.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity3.setDataHeaderEntity(modelMapper.map(testDataEnvelopeNullChecksum.getDataHeader(), DataHeaderEntity.class));

        testDataEnvelopeList = Collections.singletonList(testDataEnvelope);
        testDataBodyEntityList = Collections.singletonList(expectedDataBodyEntity);
        server = new ServerImpl(dataBodyServiceImplMock, modelMapper);
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected()throws IOException, NoSuchAlgorithmException {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();
        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldNotSaveDataEnvelopeInvalidChecksum() throws IOException, NoSuchAlgorithmException {
        boolean failure = server.saveDataEnvelope(testDataEnvelopeInvalidChecksum);

        assertThat(failure).isFalse();
        verify(dataBodyServiceImplMock, never()).saveDataBody(eq(expectedDataBodyEntity2));
    }

    @Test
    public void shouldNotSaveDataEnvelopeNoChecksum() throws IOException, NoSuchAlgorithmException {
        boolean failure = server.saveDataEnvelope(testDataEnvelopeNullChecksum);

        assertThat(failure).isFalse();
        verify(dataBodyServiceImplMock, never()).saveDataBody(eq(expectedDataBodyEntity3));
    }

    @Test void shouldRetrieveDataByBlockTypeAsExpected() {
        when(dataBodyServiceImplMock.getDataByBlockType(any(BlockTypeEnum.class))).thenReturn(testDataBodyEntityList);
        List<DataEnvelope> actualDataEnvelopes = server.getDataByBlockType(TEST_BLOCKTYPEA_LOWERCASE);

        assertThat(actualDataEnvelopes.equals(testDataEnvelopeList));
        verify(dataBodyServiceImplMock, times(1)).getDataByBlockType(BlockTypeEnum.BLOCKTYPEA);
    }

    @Test void shouldRetrieveNoDataByBlockTypeAsExpected() {
        List<DataEnvelope> actualDataEnvelopes = server.getDataByBlockType(TEST_BLOCKTYPEB_LOWERCASE);

        assertThat(actualDataEnvelopes).isNull();
        verify(dataBodyServiceImplMock, times(1)).getDataByBlockType(BlockTypeEnum.BLOCKTYPEB);
    }


}
