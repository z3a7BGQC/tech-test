package org.hmxlabs.techtest.persistence.model;

import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.persistence.model.DataBodyEntity;
import org.hmxlabs.techtest.server.persistence.model.DataHeaderEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hmxlabs.techtest.TestDataHelper.TEST_NAME;
import static org.hmxlabs.techtest.TestDataHelper.createTestDataBodyEntity;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class DataBodyEntityTests {

    @Test
    public void assignDataBodyEntityFieldsShouldWorkAsExpected() {
        Instant expectedTimestamp = Instant.now();

        DataHeaderEntity dataHeaderEntity = new DataHeaderEntity();
        dataHeaderEntity.setName(TEST_NAME);
        dataHeaderEntity.setBlocktype(BlockTypeEnum.BLOCKTYPEA);
        dataHeaderEntity.setCreatedTimestamp(expectedTimestamp);

        DataBodyEntity dataBodyEntity = createTestDataBodyEntity(dataHeaderEntity);

        assertThat(dataBodyEntity.getDataHeaderEntity()).isNotNull();
        assertThat(dataBodyEntity.getDataBody()).isNotNull();
    }

    @Test
    public void checkTwoDataBodiesAreEqualAsExpected() {

        DataHeaderEntity dataHeaderEntity1 = new DataHeaderEntity();
        dataHeaderEntity1.setName(TEST_NAME);
        dataHeaderEntity1.setBlocktype(BlockTypeEnum.BLOCKTYPEA);
        dataHeaderEntity1.setCreatedTimestamp(Instant.now());
        DataBodyEntity dataBodyEntity1 = createTestDataBodyEntity(dataHeaderEntity1);

        DataHeaderEntity dataHeaderEntity2 = new DataHeaderEntity();
        dataHeaderEntity2.setName(TEST_NAME);
        dataHeaderEntity2.setBlocktype(BlockTypeEnum.BLOCKTYPEA);
        dataHeaderEntity2.setCreatedTimestamp(Instant.now().plusSeconds(100L));
        DataBodyEntity dataBodyEntity2 = createTestDataBodyEntity(dataHeaderEntity2);

        assertTrue(dataBodyEntity1.equals(dataBodyEntity2));

        /*
        - Why would we want to test equality?
        - This test is failing because it's testing reference equality.
        - I considered using recursive equality e.g. (assertThat().usingRecursiveComparison().isEqualTo()) but that won't work with the timestamps
        - Comparing current timestamps causes flaky tests if you do not use specific test methods e.g. AssertJ's isCloseTo()
        - Could override equals to do value equality - this has maintainance issues as if you add new fields your test will continue to pass.
        - Found Lombok's @EqualsAndHashCode annotation
            - I still have the 'problem' of the timestamps as DataBodyEntity has a DataHeaderEntity which also has a timestamp
            - Turns out excluding DataHeaderEntity.createdTimestamp means it's excluded in DataBodyEntity.dataHeaderEntity
            - TODO look up the code for that because that's cool.
         */
    }
}
