package org.hmxlabs.techtest.server.component;

import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.api.model.DataPatchBlockTypeDto;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface Server {
    Mono<String> saveDataEnvelope(DataEnvelope envelope) throws IOException, NoSuchAlgorithmException;

    List<DataEnvelope> getDataByBlockType(String blockType);

    boolean  updateDataBlockType(String blockName, DataPatchBlockTypeDto newBlockTypeDto);
}
