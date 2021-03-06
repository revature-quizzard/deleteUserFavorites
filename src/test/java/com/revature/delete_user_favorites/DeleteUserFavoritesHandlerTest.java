package com.revature.delete_user_favorites;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revature.delete_user_favorites.models.Set;
import com.revature.delete_user_favorites.models.SetDocument;
import com.revature.delete_user_favorites.models.User;
import com.revature.delete_user_favorites.repositories.SetRepository;
import com.revature.delete_user_favorites.repositories.UserRepository;
import com.revature.delete_user_favorites.stubs.TestLogger;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DeleteUserFavoritesHandlerTest {

    static TestLogger testLogger;
    static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();

    DeleteUserFavoritesHandler sut;
    Context mockContext;
    UserRepository mockUserRepo;
    SetRepository mockSetRepo;

    @BeforeAll
    static void beforeAll() {
        testLogger = new TestLogger();
    }

    @AfterAll
    public static void afterAllTests() {
        testLogger.close();
    }

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockSetRepo = mock(SetRepository.class);
        sut = new DeleteUserFavoritesHandler(mockUserRepo, mockSetRepo);
        mockContext = mock(Context.class);
        when(mockContext.getLogger()).thenReturn(testLogger);
    }

    @AfterEach
    void tearDown() {
        sut = null;
        reset(mockContext, mockUserRepo);
    }

    @Test
    void handleRequest() {
        // Arrange
        List<SetDocument> testDocs = new ArrayList<>();
        SetDocument testDoc = new SetDocument();
        testDoc.setId("test");
        testDocs.add(testDoc);
        User user = new User(null, null, testDocs, null, null, 69, 4, 4, null, null);
        Set validSet = new Set();
        validSet.setId("test");

        APIGatewayProxyRequestEvent mockRequest = new APIGatewayProxyRequestEvent();
        mockRequest.withPath("/users/favorites");
        mockRequest.withHttpMethod("DELETE");
        mockRequest.withQueryStringParameters(Collections.singletonMap("user_id", "valid"));
        mockRequest.withHeaders(Collections.singletonMap("Content-type", "application/json"));
        mockRequest.withBody(mapper.toJson(testDoc));

        when(mockSetRepo.getSetById(anyString())).thenReturn(validSet);
        when(mockUserRepo.findUserById(anyString())).thenReturn(user);
        when(mockUserRepo.saveUser(any(User.class))).thenReturn(user);

        // Act
        APIGatewayProxyResponseEvent responseEvent = sut.handleRequest(mockRequest, mockContext);

        // Assert
        System.out.println(responseEvent.getBody());
        assertEquals(202, responseEvent.getStatusCode());
    }

    @Test
    void handleRequest_return400_ifParamsNull() {
        // Arrange
        List<SetDocument> testDocs = new ArrayList<>();
        SetDocument testDoc = new SetDocument();
        testDocs.add(testDoc);
        User user = new User(null, null, testDocs, null, null, 69, 4, 4, null, null);

        APIGatewayProxyRequestEvent mockRequest = new APIGatewayProxyRequestEvent();
        mockRequest.withPath("/users/favorites");
        mockRequest.withHttpMethod("DELETE");
        mockRequest.withQueryStringParameters(null);
        mockRequest.withHeaders(Collections.singletonMap("Content-type", "application/json"));
        mockRequest.withBody(mapper.toJson(testDoc));

        when(mockUserRepo.findUserById(anyString())).thenReturn(user);
        when(mockUserRepo.saveUser(any(User.class))).thenReturn(user);
        // Act
        APIGatewayProxyResponseEvent responseEvent = sut.handleRequest(mockRequest, mockContext);

        // Assert
        assertEquals(400, responseEvent.getStatusCode());
    }
}