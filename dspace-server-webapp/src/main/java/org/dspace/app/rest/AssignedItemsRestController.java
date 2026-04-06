/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for retrieving items assigned to the current logged-in user
 *
 * @author DSpace Developer
 */
@RestController
@RequestMapping("/api/items")
public class AssignedItemsRestController {

    private static final Logger log = LogManager.getLogger(AssignedItemsRestController.class);
    private static final String ASSIGNED_USER_METADATA = "dc.assigned.user";

    @Autowired
    private ItemService itemService;

    @Autowired
    private ConverterService converter;

    @Autowired
    private Utils utils;

    /**
     * Get all items assigned to the current logged-in user.
     * Queries database directly for items with dc.assigned.user metadata.
     *
     * GET /api/items/assigned-to-me
     *
     * @param request the HTTP servlet request
     * @param pageable pagination information
     * @return paginated list of items assigned to the user
     */
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<Page<ItemRest>> getAssignedItems(
            HttpServletRequest request,
            Pageable pageable) {
        try {
            Context context = ContextUtil.obtainContext(request);

            // Get current logged-in user
            if (context.getCurrentUser() == null) {
                log.warn("No authenticated user found in context");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = context.getCurrentUser().getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                log.warn("Current user has no email set");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            log.debug("Fetching items assigned to user: " + userEmail);

            // Query items directly by metadata from database (bypassing Solr)
            List<Item> allItems = new ArrayList<>();
            List<Item> pageItems = new ArrayList<>();

            log.info("Querying database for items with metadata: " + userEmail);

            try {
                // Query all items with the assigned user metadata
                Iterator<Item> itemIterator = itemService.findByMetadataField(
                        context, "dc", "assigned", "user", userEmail);

                int offset = (int) pageable.getOffset();
                int pageSize = pageable.getPageSize();
                int count = 0;
                int added = 0;

                log.debug("Iterating through items, offset=" + offset + ", pageSize=" + pageSize);

                while (itemIterator.hasNext()) {
                    Item item = itemIterator.next();
                    allItems.add(item);
                    log.debug("Found item in DB: " + item.getID());

                    if (count >= offset && added < pageSize) {
                        pageItems.add(item);
                        added++;
                        log.debug("Added to result page: " + item.getID());
                    }
                    count++;
                }

                long totalCount = (long) allItems.size();
                log.info("Database found " + totalCount + " total items, returning " + pageItems.size()
                        + " for page (offset: " + offset + ", pageSize: " + pageSize + ")");

            } catch (Exception e) {
                log.error("Error querying items by metadata field", e);
                log.debug("Full exception: ", e);
            }

            // Convert to REST
            List<ItemRest> itemRests = new ArrayList<>();
            for (Item item : pageItems) {
                try {
                    itemRests.add(converter.toRest(item, utils.obtainProjection()));
                    log.debug("Added item to response: " + item.getID());
                } catch (Exception e) {
                    log.warn("Could not convert item to REST: " + item.getID());
                }
            }

            // Create page with total count
            Page<ItemRest> page = new PageImpl<>(itemRests, pageable, allItems.size());

            log.info("Retrieved " + itemRests.size() + " assigned items for user: " + userEmail
                    + " (total: " + allItems.size() + ")");

            return ResponseEntity.ok(page);

        } catch (Exception e) {
            log.error("Error while fetching assigned items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get count of items assigned to the current user
     *
     * GET /api/items/assigned-to-me/count
     *
     * @param request the HTTP servlet request
     * @return count of assigned items
     */
    @GetMapping("/assigned-to-me/count")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<?> countAssignedItems(HttpServletRequest request) {
        try {
            Context context = ContextUtil.obtainContext(request);

            if (context.getCurrentUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = context.getCurrentUser().getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Query directly from database for count
            log.debug("Querying database for count of assigned items: " + userEmail);

            int count = 0;
            try {
                Iterator<Item> itemIterator = itemService.findByMetadataField(
                        context, "dc", "assigned", "user", userEmail);

                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    count++;
                }

                log.info("Database count of assigned items for user: " + count);
            } catch (Exception e) {
                log.error("Error counting assigned items from database", e);
                count = 0;
            }

            return ResponseEntity.ok(new CountResponse(count));

        } catch (Exception e) {
            log.error("Error while counting assigned items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Response class for count endpoint
     */
    public static class CountResponse {
        public int count;

        public CountResponse(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
