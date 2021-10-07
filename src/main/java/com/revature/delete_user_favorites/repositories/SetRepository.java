package com.revature.delete_user_favorites.repositories;

import com.revature.delete_user_favorites.exceptions.ResourceNotFoundException;
import com.revature.delete_user_favorites.models.Set;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;



/**
 * The SetRepository Class is a database repository which provides CRUD operations on the Sets table.
 */
public class SetRepository {

    private final DynamoDbTable<Set> setTable;

    public SetRepository(){
        DynamoDbClient db = DynamoDbClient.builder().httpClient(ApacheHttpClient.create()).build();

        DynamoDbEnhancedClient dbClient = DynamoDbEnhancedClient.builder().dynamoDbClient(db).build();

        setTable = dbClient.table("Sets", TableSchema.fromBean(Set.class));
    }

    public Set getSetById(String id){

        AttributeValue val = AttributeValue.builder().s(id).build();
        Expression filter = Expression.builder().expression("#a = :b") .putExpressionName("#a", "id") .putExpressionValue(":b", val).build();
        ScanEnhancedRequest request = ScanEnhancedRequest.builder().filterExpression(filter).build();
        try {
            return setTable.scan(request).stream().findFirst().orElseThrow(ResourceNotFoundException::new).items().get(0);
        }catch (Exception e){
            return null;
        }
    }

    public void updateSet(Set set){
        setTable.updateItem(set);
    }


}
