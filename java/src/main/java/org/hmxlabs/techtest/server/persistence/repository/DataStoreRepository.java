package org.hmxlabs.techtest.server.persistence.repository;

import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DataStoreRepository extends JpaRepository<DataBodyEntity, Long> {

    @Transactional//??
    public List<DataBodyEntity> findDataBodyEntityByDataHeaderEntityBlocktype(BlockTypeEnum blockTypeEnum);

}
