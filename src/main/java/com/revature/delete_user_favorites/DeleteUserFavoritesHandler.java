package com.revature.delete_user_favorites;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revature.delete_user_favorites.models.Set;
import com.revature.delete_user_favorites.models.SetDocument;
import com.revature.delete_user_favorites.models.User;
import com.revature.delete_user_favorites.repositories.SetRepository;
import com.revature.delete_user_favorites.repositories.UserRepository;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
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
    private final UserRepository userRepo;
    private final SetRepository setRepo;

    public DeleteUserFavoritesHandler() {
        this.userRepo = new UserRepository();
        setRepo = new SetRepository();
    }

    public DeleteUserFavoritesHandler(UserRepository userRepo, SetRepository setRepo) {
        this.userRepo = userRepo;
        this.setRepo = setRepo;
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

        // For CORS
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization");
        headers.put("Access-Control-Allow-Origin", "*");
        responseEvent.setHeaders(headers);

        LambdaLogger logger = context.getLogger();
        logger.log("RECEIVED EVENT: " + requestEvent);

        // Retrieving setDocument from body
        Map<String, String> params = requestEvent.getQueryStringParameters();
        SetDocument setDoc = mapper.fromJson(requestEvent.getBody(), SetDocument.class);
        logger.log("SET ID: " + setDoc.getId());
        if (params == null || setDoc == null) {
            logger.log("Error: No parameters, or no set found to delete!");
            responseEvent.setStatusCode(400);
            responseEvent.setBody(mapper.toJson("Error: No parameters, or no set found to delete!"));
        }

        try {
            // Retrieving set from database
            Set set = setRepo.getSetById(setDoc.getId());
            logger.log("SET FOUND : " + set.toString() + "\n");
            if (set == null){
                logger.log("No set found with provided ID.");
                responseEvent.setStatusCode(HttpStatusCode.BAD_REQUEST);
                responseEvent.setBody("Set not found");
                return responseEvent;
            }

            // Populate values of setDoc to match the document found
            setDoc = new SetDocument(set);

            // Retrieve user data
            User user = userRepo.findUserById(params.get("user_id"));
            logger.log("USER FOUND: " + user.getId()+ "\n" +
                    "WITH FAVORITES: "+ user.getFavoriteSets().toString());
            // List all of a user's favorite sets. Check if the setDoc exists in useFavorites. If not, return 400.
            List<SetDocument> sets = user.getFavoriteSets();
            if(sets.contains(setDoc))
                sets.remove(setDoc);
            else {
                responseEvent.setStatusCode(HttpStatusCode.BAD_REQUEST);
                responseEvent.setBody("Set not found in user favorites!");
                return responseEvent;
            }

            // Update set favorite count
            set.setFavorites(set.getFavorites() - 1);

            // Overwrite the former list of sets with the altered set list.
            user.setFavoriteSets(sets);

            // Update documents in DynamoDB
            setRepo.updateSet(set);
            userRepo.saveUser(user);
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
