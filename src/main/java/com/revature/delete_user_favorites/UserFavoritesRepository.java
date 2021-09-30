package com.revature.delete_user_favorites;

import com.revature.delete_user_favorites.exceptions.ResourceNotFoundException;
import com.revature.delete_user_favorites.models.User;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * A repository-layer class used for querying DynamoDB using the DynamoDBMapper.
 */
public class UserFavoritesRepository {

    private final DynamoDbTable<User> userTable;

    public UserFavoritesRepository(DynamoDbTable<User> testTable) {
        userTable = testTable;
    }

    public UserFavoritesRepository() {
        DynamoDbClient dbReader = DynamoDbClient.builder().httpClient(ApacheHttpClient.create()).build();
        DynamoDbEnhancedClient dbClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dbReader).build();
        userTable = dbClient.table("Users", TableSchema.fromBean(User.class));
    }

    /**
     * @param id - Necessary for knowing who to return.
     * @return - An object containing all of a user's data and their list of created/favorited sets.
     */
    public User findUserById(String id) {
        AttributeValue val = AttributeValue.builder().s(id).build();
        Expression filter = Expression.builder().expression("#a = :b") .putExpressionName("#a", "id").putExpressionValue(":b", val).build();
        ScanEnhancedRequest request = ScanEnhancedRequest.builder().filterExpression(filter).build();

        User user = userTable.scan(request).stream().findFirst().orElseThrow(ResourceNotFoundException::new).items().get(0);
        System.out.println("USER WITH ID: " + user);
        return user;
    }

    /**
     * @param user - The user to be saved to the database.
     * @return - The user that was successfully persisted to the database.
     */
    public User saveUser(User user) {
        return userTable.updateItem(user);
    }
}

