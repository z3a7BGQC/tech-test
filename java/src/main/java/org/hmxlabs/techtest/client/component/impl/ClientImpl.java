package org.hmxlabs.techtest.client.component.impl;

import org.hmxlabs.techtest.client.api.model.DataEnvelope;
import org.hmxlabs.techtest.client.component.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriTemplate;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
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



    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);

        RestClient restClient = RestClient.create();

        ResponseEntity<Void> response = restClient.post()
                .uri(URI_PUSHDATA)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dataEnvelope)
                .retrieve()
                .toBodilessEntity();

        /* Chose between RestClient and WebClient (and RestTemplate, but that's considered deprecated now).
            For long-term scalability I would use WebClient, but I am less familiar with asynchronous functionality,
            so for the sake of time I am sticking with RestTemplate.
            I am also inclined to get something working, even if simpler and it turns out to be under-architected,
            rather than overcomplicate code and make it less maintainable if that functionality is not needed.
            TODO come back and try out a WebClient implementation
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


}
