package com.revature.delete_user_favorites;

import com.revature.delete_user_favorites.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.List;

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

/**
 * The User POJO is necessary for storing the data received from DynamoDB.
 * It is very much a Data Transfer Object.
 */
@Data
@DynamoDbBean
class User {
    private String id;
    private String username;
    private List<SetDocument> favoriteSets;
    private List<SetDocument> createdSets;
    private String profilePicture;
    private int points;
    private int wins;
    private int losses;
    private String registrationDate;
    private List<String> gameRecords;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}

/**
 * SetDocument is necessary because the User returned from DynamoDB has a
 * list of SetDocuments attached to them describing the user's favorite cards
 * and their created list of cards. This is what we are altering.
 */
@Data
@AllArgsConstructor
class SetDocument {
    private String id;
    private String name;
    private List<Tags> tags;
    private boolean isPublic;
    private int views;
    private int plays;
    private int studies;
    private int favorites;
}

/**
 * Tags is a necessary pojo for persisting data from the DynamoDB to access
 * the users from the database. Without the Tags, it is impossible to handle
 * user data safely.
 */
@Data
@AllArgsConstructor
class Tags {
    private String tagName;
    private String tagColor;
}

