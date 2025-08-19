package org.hmxlabs.techtest.api.controller;

import lombok.SneakyThrows;
import org.hmxlabs.techtest.TestDataHelper;
import org.hmxlabs.techtest.server.api.controller.ServerController;
import org.hmxlabs.techtest.server.api.model.DataEnvelope;
import org.hmxlabs.techtest.server.api.model.DataPatchBlockTypeDto;
import org.hmxlabs.techtest.server.component.Server;
import org.hmxlabs.techtest.server.exception.HadoopClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hmxlabs.techtest.TestDataHelper.TEST_BLOCKTYPEA_LOWERCASE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final String URI_GETDATA = "http://localhost:8090/dataserver/data/{blockType}";
	public static final String URI_PATCHDATA = "http://localhost:8090/dataserver/update/{blockName}/blockType";

	@Mock
	private Server serverMock;

	private DataEnvelope testDataEnvelope;
	private List<DataEnvelope> testDataEnvelopeList;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;

	@BeforeEach
	@SneakyThrows
	public void setUp() throws HadoopClientException, NoSuchAlgorithmException, IOException {
		serverController = new ServerController(serverMock);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();

		testDataEnvelopeList = Collections.singletonList(testDataEnvelope);
	}

	@Test
	public void testPushDataPostCallWorksAsExpected() throws Exception {

		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);

		MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(checksumPass).isTrue();
	}

	@Test
	public void testPushDataPostCallWorksAsExpectedInvalidChecksum() throws Exception {
		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(false);

		MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
						.content(testDataEnvelopeJson)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(checksumPass).isFalse();
	}

	@Test
	public void testGetDataByBlockTypeCallWorksAsExpected() throws Exception {
		String testDataEnvelopeListJson = objectMapper.writeValueAsString(testDataEnvelopeList);

		when(serverMock.getDataByBlockType(any(String.class))).thenReturn(testDataEnvelopeList);

		MvcResult mvcResult = mockMvc.perform(get(URI_GETDATA, TEST_BLOCKTYPEA_LOWERCASE)
						.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().json(testDataEnvelopeListJson))
				.andReturn();
	}

	@Test
	public void testUpdateDataBlockTypeCallWorksAsExpected() throws Exception {
		DataPatchBlockTypeDto testPatchValidDto = TestDataHelper.createTestDataPatchBlockyTypeDto("BLOCKTYPEB");
		String testPatchInvalidDtoJson = objectMapper.writeValueAsString(testPatchValidDto);

		when(serverMock.updateDataBlockType(any(String.class), any(DataPatchBlockTypeDto.class))).thenReturn(true);

		MvcResult mvcResult = mockMvc.perform(patch(URI_PATCHDATA, "Test")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(testPatchInvalidDtoJson)
						.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		boolean blockTypePatched = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());

		assertThat(blockTypePatched).isTrue();
	}

	@Test
	public void testUpdateDataBlockTypeFailsInvalidEnumString() throws Exception {
		DataPatchBlockTypeDto testPatchInvalidDto = TestDataHelper.createTestDataPatchBlockyTypeDto("blocktypec");
		String testPatchInvalidDtoJson = objectMapper.writeValueAsString(testPatchInvalidDto);
//
		MvcResult mvcResult = mockMvc.perform(patch(URI_PATCHDATA, "Test")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(testPatchInvalidDtoJson)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		boolean blockTypePatched = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(blockTypePatched).isFalse();
	}

//	{
//		"op":"replace",
//			"path":"/blockType",
//			"value":"blocktypeb"
//	}

	/** Test Scenarios
	 *
	 *  For controller tests:
	 *  - check Post call works as expected
	 *  	- returns ok
	 * 		- check data is persisted when a checksum is valid <- not to be tested at this layer
	 *  - check Post call fails when checksum invalid
	 *  	- verify data is not persisted when a checksum is invalid
	 *  	- returns another status code - 403? 422? 418? -> 403, also return Want-Content-Digest header back
	 *  - check malformed request fails (500) ??
	 *  	- header malformed, data envelope malformed
	 *  - test database transaction rollbacks? (other tests) <- not to be tested at this layer
	 */

}
