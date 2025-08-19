package org.hmxlabs.techtest.server.component.impl;

import lombok.SneakyThrows;
import org.hmxlabs.techtest.server.api.model.DataBody;
import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.api.model.DataHeader;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.hmxlabs.techtest.server.service.DataBodyService;
import org.hmxlabs.techtest.server.component.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;
    public static final String DIGEST_ALGORITHM = "MD5";

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    @SneakyThrows
    public boolean saveDataEnvelope(DataEnvelope envelope) {
        boolean checksumValid = isChecksumValid(envelope);

        if (checksumValid) {
            persist(envelope);
            log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        }
        return checksumValid;
    }

    public List<DataEnvelope> getDataByBlockType(String blockType){
        BlockTypeEnum blockTypeEnum = convertStringToEnum(blockType);
        if (blockTypeEnum == null) {
            return Collections.emptyList();
        }
        List<DataBodyEntity> dataBodyEntityList = dataBodyServiceImpl.getDataByBlockType(blockTypeEnum);

        if (dataBodyEntityList.isEmpty()) {
            log.info("No data of block type {} found in data store", blockType);
            return Collections.emptyList();
        }
        log.info("Retrieved data of block type {} from data store", blockType);
        List<DataEnvelope> dataEnvelopes = packageDataBodyEntitiesIntoDataEnvelopes(dataBodyEntityList);

        return dataEnvelopes.isEmpty() ? Collections.emptyList() : dataEnvelopes;
    }

    public boolean updateDataBlockType(String blockName, String newBlockType) {
        return true;
    }

    private List<DataEnvelope> packageDataBodyEntitiesIntoDataEnvelopes(List<DataBodyEntity> dataBodyEntityList) {
        ArrayList<DataEnvelope> dataEnvelopes = new ArrayList<>();
        for ( DataBodyEntity i : dataBodyEntityList) {
            DataHeader dataHeader = modelMapper.map(i.getDataHeaderEntity(), DataHeader.class);
            DataBody dataBody = modelMapper.map(i, DataBody.class);
            DataEnvelope dataEnvelope = new DataEnvelope(dataHeader, dataBody);
            dataEnvelopes.add(dataEnvelope);
        }
        log.info("Successfully packaged envelope data {} from data store", dataEnvelopes.getFirst().getDataHeader().getName());
        return dataEnvelopes;
    }

    private BlockTypeEnum convertStringToEnum(String blockType) {
        log.info("Attempting to convert string {} to BlockTypeEnum", blockType);
        try {
            return BlockTypeEnum.valueOf(blockType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.info("Failed to convert string {} to BlockTypeEnum", blockType);
            return null;
        }
    }

    private boolean isChecksumValid(DataEnvelope dataEnvelope) throws NoSuchAlgorithmException {
        log.info("Validating checksum for data with attribute name: {}", dataEnvelope.getDataHeader().getName());
        if (dataEnvelope.getDataHeader().getMd5Checksum() == null) {
            return false;
        } else {
            byte[] dataBodyBytes = dataEnvelope.getDataBody().getDataBody().getBytes();
            byte[] hash = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(dataBodyBytes);
            String arrivedDataBodyChecksum = new BigInteger(1, hash).toString(16);
            return dataEnvelope.getDataHeader().getMd5Checksum().equals(arrivedDataBodyChecksum);
        }
    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }


}
 /*
    I realised in order to persist the checksum I had a few options:
       - worst: calculate the checksum twice in the controller and server layers
       - better: add checksum to DTO - this should be client side as well so the DTOs are the same
               - so I don't need the checksums in the header
              - make sense as the checksum is not just metadata - its business data
     I added the checksum to the DataHeader.
  */