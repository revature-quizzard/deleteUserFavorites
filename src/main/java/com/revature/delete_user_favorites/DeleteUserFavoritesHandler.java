package com.revature.delete_user_favorites;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

public class DeleteUserFavoritesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();
    private UserFavoritesRepository userFavRepo = new UserFavoritesRepository();

    public DeleteUserFavoritesHandler(UserFavoritesRepository userFavRepo) {
        this.userFavRepo = userFavRepo;
    }

    public DeleteUserFavoritesHandler() {
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        LambdaLogger logger = context.getLogger();
        logger.log("RECEIVED EVENT: " + requestEvent);

        Map<String, String> params = requestEvent.getQueryStringParameters();
        SetDocument set = mapper.fromJson(requestEvent.getBody(), SetDocument.class);

        if (params == null || set == null) {
            logger.log("Error: No parameters, or no set found to delete!");
            responseEvent.setStatusCode(400);
            responseEvent.setBody(mapper.toJson("Error: No parameters, or no set found to delete!"));
        }

        try {
            // Grab the complete user, loading them into memory
            User user = userFavRepo.findUserById(params.get("user_id"));

            // List all of the user's favorited sets. Removes the set found in the body of the request.
            List<SetDocument> sets = user.getFavoriteSets();
            sets.remove(set);

            // Overwrites the former list of sets with the altered set list.
            user.setFavoriteSets(sets);

            // Ship the user back to dynamoDB
            userFavRepo.saveUser(user);
            logger.log("User successfully updated!");

            // Update the response event to indicate success.
            responseEvent.setStatusCode(202);
            responseEvent.setBody(mapper.toJson("User favorite successfully deleted."));
        } catch (NullPointerException npe) {
            logger.log("Exception occurred!" + npe.getMessage());
            responseEvent.setStatusCode(400);
            responseEvent.setBody(mapper.toJson("No valid user_id parameter found."));
        }

        return responseEvent;
    }
}
