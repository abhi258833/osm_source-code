/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.service.AssignedItemService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for the assignment workflow
 * Provides hierarchical community/collection selection and user selection for item assignment
 *
 * @author DSpace Developer
 */
@RestController
@RequestMapping("/api/assignment-workflow")
public class AssignmentWorkflowController {

    private static final Logger log = LogManager.getLogger(AssignmentWorkflowController.class);

    @Autowired
    private CommunityService communityService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private EPersonService ePersonService;

    @Autowired
    private AssignedItemService assignedItemService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Get all root communities for hierarchical selection
     *
     * GET /api/assignment-workflow/communities
     *
     * @param request the HTTP servlet request
     * @return list of root communities with hierarchical structure
     */
    @GetMapping("/communities")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllCommunities(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            List<Community> rootCommunities = communityService.findAllTop(context);
            ArrayNode communityArray = mapper.createArrayNode();

            for (Community community : rootCommunities) {
                ObjectNode communityNode = buildCommunityNode(context, community);
                communityArray.add(communityNode);
            }

            context.restoreAuthSystemState();
            context.complete();

            return ResponseEntity.ok(communityArray);

        } catch (SQLException e) {
            log.error("Database error retrieving communities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "Failed to retrieve communities"));
        } catch (Exception e) {
            log.error("Unexpected error retrieving communities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get subcommunities for a given community
     *
     * GET /api/assignment-workflow/communities/{communityId}/subcommunities
     *
     * @param request the HTTP servlet request
     * @param communityId the UUID of the community
     * @return list of subcommunities
     */
    @GetMapping("/communities/{communityId}/subcommunities")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getSubcommunities(
            HttpServletRequest request,
            @PathVariable UUID communityId) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            Community community = communityService.find(context, communityId);
            if (community == null) {
                throw new DSpaceBadRequestException("Community not found with ID: " + communityId);
            }

            List<Community> subcommunities = community.getSubcommunities();
            ArrayNode subcommunityArray = mapper.createArrayNode();

            for (Community subcommunity : subcommunities) {
                ObjectNode subcommunityNode = buildCommunityNode(context, subcommunity);
                subcommunityArray.add(subcommunityNode);
            }

            context.restoreAuthSystemState();
            context.complete();

            return ResponseEntity.ok(subcommunityArray);

        } catch (DSpaceBadRequestException e) {
            log.warn("Bad request retrieving subcommunities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Database error retrieving subcommunities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "Failed to retrieve subcommunities"));
        } catch (Exception e) {
            log.error("Unexpected error retrieving subcommunities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get collections for a given community or subcommunity
     *
     * GET /api/assignment-workflow/communities/{communityId}/collections
     *
     * @param request the HTTP servlet request
     * @param communityId the UUID of the community/subcommunity
     * @return list of collections in the community
     */
    @GetMapping("/communities/{communityId}/collections")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getCollections(
            HttpServletRequest request,
            @PathVariable UUID communityId) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            Community community = communityService.find(context, communityId);
            if (community == null) {
                throw new DSpaceBadRequestException("Community not found with ID: " + communityId);
            }

            List<Collection> collections = community.getCollections();
            ArrayNode collectionArray = mapper.createArrayNode();

            for (Collection collection : collections) {
                ObjectNode collectionNode = mapper.createObjectNode();
                collectionNode.put("id", collection.getID().toString());
                collectionNode.put("name", collection.getName());
                collectionNode.put("handle", collection.getHandle());
                collectionArray.add(collectionNode);
            }

            context.restoreAuthSystemState();
            context.complete();

            return ResponseEntity.ok(collectionArray);

        } catch (DSpaceBadRequestException e) {
            log.warn("Bad request retrieving collections: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Database error retrieving collections", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "Failed to retrieve collections"));
        } catch (Exception e) {
            log.error("Unexpected error retrieving collections", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get all users for checkbox selection
     *
     * GET /api/assignment-workflow/users
     *
     * @param request the HTTP servlet request
     * @return list of all EPerson users
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            List<EPerson> users = ePersonService.findAll(context, EPerson.EMAIL);
            ArrayNode userArray = mapper.createArrayNode();

            for (EPerson user : users) {
                ObjectNode userNode = mapper.createObjectNode();
                userNode.put("id", user.getID().toString());
                userNode.put("email", user.getEmail());
                userNode.put("name", user.getFullName());
                userNode.put("checked", false);
                userArray.add(userNode);
            }

            context.restoreAuthSystemState();
            context.complete();

            return ResponseEntity.ok(userArray);

        } catch (SQLException e) {
            log.error("Database error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "Failed to retrieve users"));
        } catch (Exception e) {
            log.error("Unexpected error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("error", "An unexpected error occurred"));
        }
    }

    /**
     * Assign items in a collection to selected users
     *
     * POST /api/assignment-workflow/assign
     *
     * Request body example:
     * {
     *   "collectionId": "550e8400-e29b-41d4-a716-446655440000",
     *   "selectedUserIds": ["user1-id", "user2-id", "user3-id"]
     * }
     *
     * @param request the HTTP servlet request
     * @param requestBody the request containing collectionId and selected user IDs
     * @return response with assignment status
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> assignItemsToSelectedUsers(
            HttpServletRequest request,
            jakarta.servlet.ServletInputStream requestBody) {
        try {
            Context context = ContextUtil.obtainContext(request);
            context.turnOffAuthorisationSystem();

            // Parse request body
            JsonNode jsonNode = mapper.readTree(requestBody);

            String collectionId = jsonNode.get("collectionId").asText();
            if (collectionId == null || collectionId.isEmpty()) {
                throw new DSpaceBadRequestException("collectionId is required");
            }

            List<String> selectedUserIds = mapper.convertValue(
                    jsonNode.get("selectedUserIds"),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            if (selectedUserIds == null || selectedUserIds.isEmpty()) {
                throw new DSpaceBadRequestException("At least one user must be selected");
            }

            // Get collection
            Collection collection = collectionService.find(context, UUID.fromString(collectionId));
            if (collection == null) {
                throw new DSpaceBadRequestException("Collection not found with ID: " + collectionId);
            }

            // Convert user IDs to emails
            List<String> userEmails = new ArrayList<>();
            for (String userId : selectedUserIds) {
                try {
                    EPerson user = ePersonService.find(context, UUID.fromString(userId));
                    if (user != null) {
                        userEmails.add(user.getEmail());
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid user ID format: " + userId);
                }
            }

            if (userEmails.isEmpty()) {
                throw new DSpaceBadRequestException("No valid users found for the provided user IDs");
            }

            log.info("Starting assignment of items in collection " + collectionId +
                    " to " + userEmails.size() + " selected users");

            // Perform assignment
            assignedItemService.assignItemsToUsers(context, collection, userEmails);

            context.restoreAuthSystemState();
            context.complete();

            log.info("Successfully completed assignment to " + userEmails.size() + " users");

            return ResponseEntity.ok(new AssignmentResponse(
                    "success",
                    "Items in collection have been assigned to " + userEmails.size() + " users"
            ));

        } catch (DSpaceBadRequestException e) {
            log.warn("Bad request in assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AssignmentResponse("error", e.getMessage()));
        } catch (SQLException e) {
            log.error("Database error during assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AssignmentResponse("error", "Database error occurred"));
        } catch (Exception e) {
            log.error("Unexpected error during assignment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AssignmentResponse("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Build a community node with subcommunities
     *
     * @param context the DSpace context
     * @param community the community to build the node from
     * @return ObjectNode representing the community
     * @throws SQLException if a database error occurs
     */
    private ObjectNode buildCommunityNode(Context context, Community community) throws SQLException {
        ObjectNode communityNode = mapper.createObjectNode();
        communityNode.put("id", community.getID().toString());
        communityNode.put("name", community.getName());
        communityNode.put("handle", community.getHandle());

        List<Community> subcommunities = community.getSubcommunities();
        if (!subcommunities.isEmpty()) {
            ArrayNode subcommunityArray = mapper.createArrayNode();
            for (Community subcommunity : subcommunities) {
                ObjectNode subNode = mapper.createObjectNode();
                subNode.put("id", subcommunity.getID().toString());
                subNode.put("name", subcommunity.getName());
                subNode.put("handle", subcommunity.getHandle());
                subcommunityArray.add(subNode);
            }
            communityNode.set("subcommunities", subcommunityArray);
        }

        return communityNode;
    }

    /**
     * Response class for assignment operations
     */
    public static class AssignmentResponse {
        public String status;
        public String message;

        public AssignmentResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Response class for error responses
     */
    public static class ErrorResponse {
        public String status;
        public String message;

        public ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
