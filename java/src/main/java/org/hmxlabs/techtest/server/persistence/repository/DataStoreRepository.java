package org.hmxlabs.techtest.server.persistence.repository;

import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataStoreRepository extends JpaRepository<DataBodyEntity, Long> {

    @Transactional//??
    List<DataBodyEntity> findDataBodyEntityByDataHeaderEntityBlocktype(BlockTypeEnum blockTypeEnum);

    Optional<DataBodyEntity> findDataBodyEntityByDataHeaderEntityName(String name);
}
