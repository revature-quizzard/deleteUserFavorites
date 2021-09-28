package com.revature.delete_user_favorites;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * A repository-layer class used for querying DynamoDB using the DynamoDBMapper.
 */
public class UserFavoritesRepository {

    private DynamoDBMapper dbReader = new DynamoDBMapper(AmazonDynamoDBAsyncClientBuilder.defaultClient());

    public UserFavoritesRepository(DynamoDBMapper dbReader) {
        this.dbReader = dbReader;
    }

    public UserFavoritesRepository() {
    }

    /**
     * @param id - Necessary for knowing who to return.
     * @return - An object containing all of a user's data and their list of created/favorited sets.
     */
    public User findUserById(String id) {
        return dbReader.load(User.class, id);
    }

    /**
     * @param user - The user to be saved to the database.
     * @return - The user that was successfully persisted to the database.
     */
    public User saveUser(User user) {
        dbReader.save(user);
        return user;
    }
}

/**
 * The User POJO is necessary for storing the data received from DynamoDB.
 * It is very much a Data Transfer Object.
 */
@Data
@AllArgsConstructor
@DynamoDBTable(tableName = "Users")
class User {
    @DynamoDBHashKey
    @DynamoDBAttribute
    private String id;

    @DynamoDBAttribute
    private String username;

    @DynamoDBAttribute
    private List<SetDocument> favoriteSets;

    @DynamoDBAttribute
    private List<SetDocument> createdSets;

    @DynamoDBAttribute
    private String profilePicture;

    @DynamoDBAttribute
    private int points;

    @DynamoDBAttribute
    private int wins;

    @DynamoDBAttribute
    private int losses;

    @DynamoDBAttribute
    private String registrationDate;

    @DynamoDBAttribute
    private List<String> gameRecords;
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
    private List<String> tags;
    private boolean isPublic;
    private int views;
    private int plays;
    private int studies;
    private int favorites;
}