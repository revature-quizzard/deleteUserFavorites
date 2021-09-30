package com.revature.delete_user_favorites.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tags is a necessary pojo for persisting data from the DynamoDB to access
 * the users from the database. Without the Tags, it is impossible to handle
 * user data safely.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class Tag {
    private String tagName;
    private String tagColor;
}
