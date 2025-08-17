package org.hmxlabs.techtest.persistence.repository;

import org.hmxlabs.techtest.client.component.Client;
import org.hmxlabs.techtest.client.component.impl.ClientImpl;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.hmxlabs.techtest.server.persistence.repository.DataHeaderRepository;
import org.hmxlabs.techtest.server.persistence.repository.DataStoreRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hmxlabs.techtest.TestDataHelper.createTestDataBodyEntity;
import static org.hmxlabs.techtest.TestDataHelper.createTestDataHeaderEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//@EntityScan("org.hmxlabs.techtest.client.component")
@DataJpaTest
@ExtendWith(SpringExtension.class)
public class DataStoreRepositoryTest {

    @MockitoBean
    private Client client; //for some reason this test wouldn't pass without this - see comments at the end of the file

    @Autowired
    private DataStoreRepository dataStoreRepository;

//    @Autowired
//    private DataHeaderRepository dataHeaderRepository;

    private DataBodyEntity testDataBody;
    private List<DataBodyEntity> testDataBodyList;

    @BeforeEach
    public void setUp() {
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        testDataBody = createTestDataBodyEntity(testDataHeaderEntity);
        dataStoreRepository.save(testDataBody);
        testDataBodyList = Collections.singletonList(testDataBody);
    }

    @AfterEach
    public void tearDown(){
        dataStoreRepository.delete(testDataBody);
    }

    @Test
    public void shouldFindDataBodyEntitiesByBlocktype() {
        List<DataBodyEntity> foundDataBodyEntities = dataStoreRepository.findDataBodyEntityByDataHeaderEntityBlocktype(BlockTypeEnum.BLOCKTYPEA);

        assertNotNull(foundDataBodyEntities);
        assertEquals(testDataBodyList, foundDataBodyEntities);
    }

/**
 * Fails due to following exceptions:
 * java.lang.IllegalStateException: Failed to load ApplicationContext for [MergedContextConfiguration@7e84a52b testClass ...
 * Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'techTestApplication': Unsatisfied dependency expressed through field 'client': No qualifying bean of type 'org.hmxlabs.techtest.client.component.Client' ...
 * Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'org.hmxlabs.techtest.client.component.Client' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
 */




}
