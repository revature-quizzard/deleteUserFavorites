package com.revature.delete_user_favorites;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class UserFavoritesRepository {

    private DynamoDBMapper dbReader = new DynamoDBMapper(AmazonDynamoDBAsyncClientBuilder.defaultClient());

    public UserFavoritesRepository(DynamoDBMapper dbReader) {
        this.dbReader = dbReader;
    }

    public UserFavoritesRepository() {
    }

    public User findUserById(String id) {
        return dbReader.load(User.class, id);
    }

    public User saveUser(User user) {
        dbReader.save(user);
        return user;
    }
}

@Data
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