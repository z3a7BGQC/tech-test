package org.hmxlabs.techtest.client.component.impl;

import lombok.SneakyThrows;
import org.hmxlabs.techtest.client.api.model.DataEnvelope;
import org.hmxlabs.techtest.client.component.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriTemplate;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.util.SerializationUtils;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");
    public static final String DIGEST_ALGORITHM = "MD5";


    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        RestClient restClient = RestClient.create();
        String md5ChecksumHeaderValue = "md5=" + generateMd5Checksum(dataEnvelope);
        ResponseEntity<Void> response = restClient.post()
                .uri(URI_PUSHDATA)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Digest", md5ChecksumHeaderValue)
                .body(dataEnvelope)
                .retrieve()
                .toBodilessEntity();

        /* **Notes for Exercise 1**
            Chose between RestClient and WebClient (and RestTemplate, but that's considered deprecated now).
            For long-term scalability I would use WebClient, but I am less familiar with asynchronous
            functionality/reactive programming,so for the sake of time I am sticking with RestTemplate.
            I am also inclined to get something working, even if simpler and it turns out to be under-architected,
            rather than overcomplicate code and make it less maintainable if that functionality is not needed.
            TODO come back and try out a WebClient implementation?
         */

    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        log.info("Query for data with header block type {}", blockType);
        return null;
    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        return true;
    }

    @SneakyThrows // used SneakyThrows because as I am passing the constant digest algorithm from a constant it is very unlikely to throw this error
    public String generateMd5Checksum(DataEnvelope dataEnvelope) {
        log.info("Generating MD5 checksum for {}", dataEnvelope.getDataHeader().getName());
        byte[] dataEnvelopeBytes = SerializationUtils.serialize(dataEnvelope);
        byte[] hash = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(dataEnvelopeBytes);
        return new BigInteger(1, hash).toString(16);
    }


}
