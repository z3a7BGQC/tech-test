package org.hmxlabs.techtest.server.component.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hmxlabs.techtest.server.api.model.DataBody;
import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.api.model.DataHeader;
import org.hmxlabs.techtest.server.api.model.DataPatchBlockTypeDto;
import org.hmxlabs.techtest.server.component.Server;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.hmxlabs.techtest.server.service.DataBodyService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;
    private ObjectMapper objectMapper;
    private final WebClient webClient;

    private static final String DIGEST_ALGORITHM = "MD5";

    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    @SneakyThrows
    public Mono<String> saveDataEnvelope(DataEnvelope envelope) {
        boolean checksumValid = isChecksumValid(envelope);
        if (!checksumValid) { // TODO add checksum validation to @PostMapping validation
            return null;
        }
        DataBodyEntity dataBodyEntity = unpackDataEvelopeAndMaptoDataBodyEntity(envelope);
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        saveData(dataBodyEntity);
        log.info("Data persisted successfully, now pushing data to Hadoop, data name: {}", envelope.getDataHeader().getName());
        return pushToHadoop(dataBodyEntity);
//        log.info("Data successfully pushed data to Hadoop, data name: {}", envelope.getDataHeader().getName());
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

    public boolean updateDataBlockType(String blockName, DataPatchBlockTypeDto newBlockTypeDto) {
        log.info("Attempting to retrieve data of block name {} from data store", blockName);
        Optional<DataBodyEntity> existingDataBlock = dataBodyServiceImpl.getDataByBlockName(blockName);
        if (existingDataBlock.isEmpty()) {
            log.info("No data of block name {} found in data store", blockName);
            return false;
        }
        DataBodyEntity dataBodyEntityToUpdate = existingDataBlock.get();
        dataBodyEntityToUpdate.getDataHeaderEntity().setBlocktype(convertStringToEnum(newBlockTypeDto.getBlockType()));
        saveData(dataBodyEntityToUpdate);
        log.info("Successfully updated data block {} to block type {}", blockName, newBlockTypeDto.getBlockType());
        return true;
    }

    private Mono<String> pushToHadoop(DataBodyEntity dataBodyEntity) throws JsonProcessingException {
        objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .build();
        String payload = objectMapper.writeValueAsString(dataBodyEntity);
        return hadoopPost(payload);
    }

    private Mono<String> hadoopPost(String payload) {
////        Retry indefiniteRetry = Retry.indefinitely()
////                .filter(throwable -> throwable instanceof RuntimeException);
        LocalDateTime now = LocalDateTime.now();
        log.info("Sending payload to Hadoop at {}", now);
        log.info( "payload for Hadoop {}", payload);
        return webClient.post()
                        .uri("/pushbigdata")
                    .httpRequest(httpRequest -> {
                    HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofSeconds(10));
                            }
                    )
                        .bodyValue(payload)
                        .retrieve()
                .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals, response -> {
                    // Handle the 504 error
                    return response.bodyToMono(String.class)
                            .flatMap(msg -> Mono.error(new WebClientException(msg) {
                            }));
                }) // need this to handle the errors, but it currently blocks retries
                        .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(5, Duration.ofSeconds(3))
                            .doBeforeRetry(retrySignal -> {
                        log.info("Retry attempt: " + retrySignal.totalRetries());
                            }));
//                .bodyToMono(String.class)); // retries are for temporarily unavailable services
//                                .filter(throwable -> throwable instanceof ReadTimeoutException));
//                        .onErrorComplete();

        /**
         * different errors encountered:
         * finishConnect(..) failed: Connection refused
         *  -
         * Caused by: io.netty.handler.timeout.ReadTimeoutException: null
         *  -
         * to stop my application erroring I used .onErrorComplete()
         * - ideally I need to handle that error and try again
         *
         * I actually seemed to be getting better responses earlier (when my POST was simpler?)
         *
         * I tried using a curl and that gave me the gateway error - my app seems to be failing immediately - sort out timeouts??
         *  TODO look at timeouts, see if rest of app can continue while we wait for the data to process?
         * the only issue with that is db calls - so data could become out of sync
         *  FIX:  Turns out because I'd changed the webClient I'd removed the default base url -added that back :))
         *
         *  I can directly handle the GATEWAY TIMEOUT error
         *
         */
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

    private DataBodyEntity unpackDataEvelopeAndMaptoDataBodyEntity(DataEnvelope envelope) {
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        return dataBodyEntity;
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