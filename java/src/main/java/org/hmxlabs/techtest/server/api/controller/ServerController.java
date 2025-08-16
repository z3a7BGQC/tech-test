package org.hmxlabs.techtest.server.api.controller;

import org.apache.coyote.Response;
import org.hmxlabs.techtest.server.api.model.DataBody;
import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.component.Server;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.service.impl.DataBodyServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/dataserver")
@RequiredArgsConstructor
@Validated
public class ServerController {

    private final Server server;

    @PostMapping(value = "/pushdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> pushData(@Valid @RequestBody DataEnvelope dataEnvelope) throws IOException, NoSuchAlgorithmException {

        log.info("Data envelope received: {}", dataEnvelope.getDataHeader().getName());
        boolean checksumPass = server.saveDataEnvelope(dataEnvelope);

        log.info("Data envelope persisted. Attribute name: {}", dataEnvelope.getDataHeader().getName());
        return ResponseEntity.ok(checksumPass); // change to CREATED status if successful checksum? FORBIDDEN if invalid? & send back Want-Content-Digest Header
    }

    @GetMapping(value = "/data/{blockType}")
    public ResponseEntity<List<DataBodyEntity>> getDataByBlockType(@PathVariable("blockType") BlockTypeEnum blockTypeEnum) {

        log.info("Getting data envelopes of block type {}", blockTypeEnum);
        return ResponseEntity.ok(server.getDataByBlockType(blockTypeEnum));
                // what if blocktypeenum is wrong?
    }

}

/* Was looking up how to transport the checksum - found the Content-MD5 header, which is now deprecated.
    Alternatives are - the Content-Digest header or making a custom content header for your service.
    Chose Content-Digest - in usage, meets the needs.

    Exercise 2 notes:
    Unsure where to do the checksum validation - passing it to ServerImpl means changing the interface, which I am suspect of
    Could keep it in Controller layer? -> Yes, makes sense to keep validation here so invalid data doesn't go further.
    It also separates concerns so that ServerImpl has the core focus of saving the data.
    This means that we will calculate the checksum twice - once in controller layer to verify and 2nd in sever layer to save and persist

    Should I change the ResponseEntity - it's currently a boolean.
    Possibilities: alternative statuses of 200 CREATED and 403 FORBIDDEN based on the checksum?
 */
