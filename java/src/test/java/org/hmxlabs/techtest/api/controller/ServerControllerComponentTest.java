package org.hmxlabs.techtest.api.controller;

import lombok.SneakyThrows;
import org.hmxlabs.techtest.TestDataHelper;
import org.hmxlabs.techtest.server.api.controller.ServerController;
import org.hmxlabs.techtest.server.api.model.DataEnvelope;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class ServerControllerComponentTest {

	public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
	public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
	public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");

	@Mock
	private Server serverMock;

	private DataEnvelope testDataEnvelope;
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;
	private ServerController serverController;
	private String testDataEnvelopeChecksumHeader;

	@BeforeEach
	@SneakyThrows
	public void setUp() throws HadoopClientException, NoSuchAlgorithmException, IOException {
		serverController = new ServerController(serverMock);
		mockMvc = standaloneSetup(serverController).build();
		objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.build();

		testDataEnvelope = TestDataHelper.createTestDataEnvelopeApiObject();

		testDataEnvelopeChecksumHeader = TestDataHelper.generateMd5ChecksumHeader(testDataEnvelope);

		when(serverMock.saveDataEnvelope(any(DataEnvelope.class))).thenReturn(true);
	}

	@Test
	public void testPushDataPostCallWorksAsExpected() throws Exception {

		String testDataEnvelopeJson = objectMapper.writeValueAsString(testDataEnvelope);

		MvcResult mvcResult = mockMvc.perform(post(URI_PUSHDATA)
				.content(testDataEnvelopeJson)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
						.header("Content-Digest", testDataEnvelopeChecksumHeader))
				.andExpect(status().isCreated())
				.andReturn();

		boolean checksumPass = Boolean.parseBoolean(mvcResult.getResponse().getContentAsString());
		assertThat(checksumPass).isTrue();
//		assertThat() test for header?
	}

	@Test
	public void testSaveDataEnvelopeValidChecksum() {

	}

	@Test
	public void testSaveDataEnvelopeInvalidChecksum() {

	}

	/** Test Scenarios
	 *  - check Post call works as expected
	 *  	- returns CREATED
	 * 		- check data is persisted when a checksum is valid
	 *  - check Post call fails when checksum ivnvalid
	 *  	- verify data is not persisted when a checksum is invalid
	 *  	- returns another status code - 403? 422? 418? -> 403, also return Want-Content-Digest header back
	 *  - check malformed request fails (500)
	 *  	- header malformed, data envelope malformed
	 *  - test database transaction rollbacks? (other tests)
	 */
}
