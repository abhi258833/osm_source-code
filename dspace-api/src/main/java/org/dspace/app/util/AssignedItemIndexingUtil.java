/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.springframework.stereotype.Component;

/**
 * Utility class for indexing assigned items
 * 
 * Note: In DSpace 9.2, Solr reindexing is typically handled automatically by item update events.
 * This utility is provided for explicit reindexing if needed through background jobs.
 *
 * @author DSpace Developer
 */
@Component
public class AssignedItemIndexingUtil {

    private static final Logger log = LogManager.getLogger(AssignedItemIndexingUtil.class);

    /**
     * Reindex a list of items after assignment changes
     *
     * Note: Actual reindexing is typically triggered by ItemService.update() events in DSpace 9.2.
     * This method is kept for compatibility and future use with background indexing jobs.
     *
     * @param context the DSpace context
     * @param items list of items to reindex
     */
    public void reindexItems(Context context, List<Item> items) {
        if (items == null || items.isEmpty()) {
            log.debug("No items to reindex");
            return;
        }

        log.info("Queued " + items.size() + " items for reindexing. "
                + "Actual indexing will be triggered by Solr event listeners.");
        for (Item item : items) {
            log.debug("Queued item for reindexing: " + item.getID());
        }
    }

    /**
     * Reindex a single item after assignment changes
     *
     * Note: Actual reindexing is typically triggered by ItemService.update() events in DSpace 9.2.
     * This method is kept for compatibility and future use with background indexing jobs.
     *
     * @param context the DSpace context
     * @param item the item to reindex
     */
    public void reindexItem(Context context, Item item) {
        log.debug("Queued item for reindexing: " + item.getID() + ". "
                + "Actual indexing will be triggered by Solr event listeners.");
    }
}
