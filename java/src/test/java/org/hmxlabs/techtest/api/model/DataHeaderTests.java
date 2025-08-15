package org.hmxlabs.techtest.api.model;

import org.hmxlabs.techtest.server.api.model.DataHeader;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hmxlabs.techtest.TestDataHelper.M5D_CHECKSUM;
import static org.hmxlabs.techtest.TestDataHelper.TEST_NAME;

@ExtendWith(MockitoExtension.class)
public class DataHeaderTests {

    @Test
    public void assignDataHeaderFieldsShouldWorkAsExpected() {
        DataHeader dataHeader = new DataHeader(TEST_NAME, BlockTypeEnum.BLOCKTYPEA, M5D_CHECKSUM);

        assertThat(dataHeader.getName()).isEqualTo(TEST_NAME);
        assertThat(dataHeader.getBlockType()).isEqualTo(BlockTypeEnum.BLOCKTYPEA);
        assertThat(dataHeader.getMd5Checksum()).isEqualTo(M5D_CHECKSUM);
    }
}
