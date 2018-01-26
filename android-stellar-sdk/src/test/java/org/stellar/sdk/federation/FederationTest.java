package org.stellar.sdk.federation;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.StatusLine;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class FederationTest extends TestCase {
    @Mock
    private OkHttpClient mockClient;
    @Mock
    private Response mockResponse;
    @Mock
    private ResponseBody mockEntity;

    private final StatusLine httpOK = new StatusLine(Protocol.HTTP_1_1, 200, "OK");
    private final String stellarToml =
            "FEDERATION_SERVER = \"https://api.stellar.org/federation\"";
    private final String successResponse =
            "{\"stellar_address\":\"bob*stellar.org\",\"account_id\":\"GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY\"}";

    @Before
    public void setUp() throws URISyntaxException, IOException {
        MockitoAnnotations.initMocks(this);
        FederationServer.setHttpClient(mockClient);
        when(mockResponse.body()).thenReturn(mockEntity);
        when(mockClient.newCall((Request) any()).execute()).thenReturn(mockResponse);
    }

    @Test
    public void testResolveSuccess() throws IOException {
        InputStream stellarTomlresponse = new ByteArrayInputStream(stellarToml.getBytes(StandardCharsets.UTF_8));
        InputStream federationResponse = new ByteArrayInputStream(successResponse.getBytes(StandardCharsets.UTF_8));

        when(StatusLine.get(mockResponse)).thenReturn(httpOK, httpOK);
        when(mockEntity.byteStream()).thenReturn(stellarTomlresponse, federationResponse);

        FederationResponse response = Federation.resolve("bob*stellar.org");
        assertEquals(response.getStellarAddress(), "bob*stellar.org");
        assertEquals(response.getAccountId(), "GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY");
        assertNull(response.getMemoType());
        assertNull(response.getMemo());
    }

    @Test
    public void testMalformedAddress() {
        try {
            FederationResponse response = Federation.resolve("bob*stellar.org*test");
            fail("Expected exception");
        } catch (MalformedAddressException e) {
            //
        }
    }
}
