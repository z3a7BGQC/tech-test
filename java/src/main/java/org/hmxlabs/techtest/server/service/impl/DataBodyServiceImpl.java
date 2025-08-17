package org.hmxlabs.techtest.server.service.impl;

import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.repository.DataStoreRepository;
import org.hmxlabs.techtest.server.service.DataBodyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataBodyServiceImpl implements DataBodyService {

    private final DataStoreRepository dataStoreRepository;

    @Override
    public void saveDataBody(DataBodyEntity dataBody) {
        dataStoreRepository.save(dataBody);
    }

    @Override
    public List<DataBodyEntity> getDataByBlockType(BlockTypeEnum blocktype) {
        return dataStoreRepository.findDataBodyEntityByDataHeaderEntityBlocktype(blocktype);
    }

    @Override
    public Optional<DataBodyEntity> getDataByBlockName(String blockName) {
        return null;
    }
}
