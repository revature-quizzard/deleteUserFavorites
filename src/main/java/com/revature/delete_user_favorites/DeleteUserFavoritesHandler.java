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

/**
 * DeleteUserFavoritesHandler is a Request Handler that takes in a path parameter bearing the user_id and
 * removes a certain set of favorite cards from the user's record. This is a soft delete and actually has no influence
 * on the list of SetDocuments upon their table. No referential integrity is intended by design.
 *
 * @author John Callahan (the heraldOfMechanus)
 */
public class DeleteUserFavoritesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();
    private UserFavoritesRepository userFavRepo;

    public DeleteUserFavoritesHandler(UserFavoritesRepository userFavRepo) {
        this.userFavRepo = userFavRepo;
    }

    public DeleteUserFavoritesHandler() {
        this.userFavRepo = new UserFavoritesRepository();
    }

    /**
     * @param requestEvent - An APIGatewayProxyRequestEvent. Holds a JSON body bearing a set that they wish to have removed,
     *                    as well as a path parameter that tells the server whom the favorited set belongs to.
     * @param context - Holds the logger and is a necessary part of this application as an auxiliary source of information
     *               regarding any surrounding circumstances of the request.
     * @return - The JSON reply bearing the message and status code of the outcome, if any.
     */
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
