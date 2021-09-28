package com.revature.delete_user_favorites;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revature.delete_user_favorites.stubs.TestLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class DeleteUserFavoritesHandlerTest {

    static TestLogger testLogger;
    private Gson mapper = new GsonBuilder().setPrettyPrinting().create();

    DeleteUserFavoritesHandler sut;
    Context mockContext;
    UserFavoritesRepository mockUserRepo;

    @BeforeAll
    static void beforeAll() {
        testLogger = new TestLogger();
    }

    @BeforeEach
    void setUp() {
        mockContext = mock(Context.class);
        mockUserRepo = mock(UserFavoritesRepository.class);

        sut = new DeleteUserFavoritesHandler(mockUserRepo);

        when(mockContext.getLogger()).thenReturn(testLogger);
    }

    @AfterEach
    void tearDown() {
        sut = null;
        mockContext = null;
        mockUserRepo = null;
    }

    @Test
    void handleRequest() {
        // Arrange
        List<SetDocument> testDocs = new ArrayList<>();
        SetDocument testDoc = new SetDocument(null, null, null, true, 12, 4, 2, 69);
        testDocs.add(testDoc);
        User user = new User(null, null, testDocs, null, null, 69, 4, 4, null, null);

        APIGatewayProxyRequestEvent mockRequest = new APIGatewayProxyRequestEvent();
        mockRequest.withPath("/users/favorites");
        mockRequest.withHttpMethod("DELETE");
        mockRequest.withQueryStringParameters(Collections.singletonMap("user_id", "valid"));
        mockRequest.withHeaders(Collections.singletonMap("Content-type", "application/json"));
        mockRequest.withBody(mapper.toJson(testDoc));

        when(mockUserRepo.findUserById(anyString())).thenReturn(user);
        when(mockUserRepo.saveUser(any(User.class))).thenReturn(user);
        // Act
        APIGatewayProxyResponseEvent responseEvent = sut.handleRequest(mockRequest, mockContext);

        // Assert
        assertEquals(202, responseEvent.getStatusCode());
    }

    @Test
    void handleRequest_return400_ifParamsNull() {
        // Arrange
        List<SetDocument> testDocs = new ArrayList<>();
        SetDocument testDoc = new SetDocument(null, null, null, true, 12, 4, 2, 69);
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