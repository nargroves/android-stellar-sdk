package org.stellar.sdk.federation;

import com.google.common.net.InternetDomainName;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.StatusLine;
import okio.BufferedSource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FederationServerTest extends TestCase {

    private OkHttpClient mockClient;

    private Request mockRequest;

    private Response mockResponse;

    private ResponseBody mockEntity;

    private FederationServer server;

    private final StatusLine httpOK = new StatusLine(Protocol.HTTP_1_1, 200, "OK");
    private final String successResponse =
            "{\"stellar_address\":\"bob*stellar.org\",\"account_id\":\"GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY\"}";
    private final String successResponseWithMemo =
            "{\"stellar_address\":\"bob*stellar.org\",\"account_id\":\"GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY\", \"memo_type\": \"text\", \"memo\": \"test\"}";

    private final StatusLine httpNotFound = new StatusLine(Protocol.HTTP_1_1, 404, "Not Found");
    private final String notFoundResponse =
            "{\"code\":\"not_found\",\"message\":\"Account not found\"}";

    private final String stellarToml =
            "FEDERATION_SERVER = \"https://api.stellar.org/federation\"";

    @Before
    public void setUp() throws URISyntaxException, IOException {
        MockitoAnnotations.initMocks(this);
        server = new FederationServer(
                "https://api.stellar.org/federation",
                InternetDomainName.from("stellar.org")
        );
        FederationServer.setHttpClient(mockClient);

        mockClient = new OkHttpClient.Builder().build();
        mockRequest = new Request.Builder().url("https://api.stellar.org/federation").build();
        mockResponse = new Response.Builder().build();
        mockEntity = new ResponseBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public BufferedSource source() {
                return null;
            }
        };


        when(mockResponse.body()).thenReturn(mockEntity);
        when(mockClient.newCall(mockRequest).execute()).thenReturn(mockResponse);
    }

    @Test
    public void testCreateForDomain() throws IOException {
        InputStream response = new ByteArrayInputStream(stellarToml.getBytes(StandardCharsets.UTF_8));
        when(StatusLine.get(mockResponse)).thenReturn(httpOK);
        when(mockEntity.byteStream()).thenReturn(response);

        FederationServer server = FederationServer.createForDomain(InternetDomainName.from("stellar.org"));
        assertEquals(server.getServerUri().toString(), "https://api.stellar.org/federation");
        assertEquals(server.getDomain().toString(), "stellar.org");

        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
//        Mockito.verify(mockClient).execute(argument.capture(), (Request) any());
        Mockito.verify(mockClient).newCall(argument.capture()).execute();
        assertEquals(URI.create("https://stellar.org/.well-known/stellar.toml"), argument.getValue().url().uri());
    }

    @Test
    public void testNameFederationSuccess() throws IOException {
        InputStream jsonResponse = new ByteArrayInputStream(successResponse.getBytes(StandardCharsets.UTF_8));
        when(StatusLine.get(mockResponse)).thenReturn(httpOK);
        when(mockEntity.byteStream()).thenReturn(jsonResponse);

        FederationResponse response = server.resolveAddress("bob*stellar.org");
        assertEquals(response.getStellarAddress(), "bob*stellar.org");
        assertEquals(response.getAccountId(), "GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY");
        assertNull(response.getMemoType());
        assertNull(response.getMemo());
    }

    @Test
    public void testNameFederationSuccessWithMemo() throws IOException {
        InputStream jsonResponse = new ByteArrayInputStream(successResponseWithMemo.getBytes(StandardCharsets.UTF_8));
        when(StatusLine.get(mockResponse)).thenReturn(httpOK);
        when(mockEntity.byteStream()).thenReturn(jsonResponse);

        FederationResponse response = server.resolveAddress("bob*stellar.org");
        assertEquals(response.getStellarAddress(), "bob*stellar.org");
        assertEquals(response.getAccountId(), "GCW667JUHCOP5Y7KY6KGDHNPHFM4CS3FCBQ7QWDUALXTX3PGXLSOEALY");
        assertEquals(response.getMemoType(), "text");
        assertEquals(response.getMemo(), "test");
    }

    @Test
    public void testNameFederationNotFound() throws IOException {
        InputStream jsonResponse = new ByteArrayInputStream(notFoundResponse.getBytes(StandardCharsets.UTF_8));
        when(StatusLine.get(mockResponse)).thenReturn(httpNotFound);
        when(mockEntity.byteStream()).thenReturn(jsonResponse);

        try {
            FederationResponse response = server.resolveAddress("bob*stellar.org");
            fail("Expected exception");
        } catch (NotFoundException e) {
        }
    }
}
