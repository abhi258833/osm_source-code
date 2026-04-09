/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.dspace.app.rest.model.QuestionMarkingDTO;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.marking.MarkingService;
import org.dspace.marking.QuestionMarkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Marking Controller for On-Screen Marking Project
 * Only System Admin or Collection Admin can assign MARKING_EDIT permission
 * 
 * @author DSpace Developer
 */
@RestController
@RequestMapping("/api/marking")
@ComponentScan(basePackages = "org.dspace.marking")
public class MarkingController {

    @Autowired
    private MarkingService markingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private EPersonService ePersonService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private QuestionMarkingService questionMarkingService;

    // ====================== ASSIGN TO EPERSON (Only Admin) ======================
    @PostMapping("/assign/eperson/{itemUuid}")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<Map<String, Object>> assignToEPerson(
            HttpServletRequest request,
            @PathVariable UUID itemUuid,
            @RequestParam UUID userUuid) {

        return assignPermission(request, itemUuid, userUuid, null, "EPerson");
    }

    // ====================== ASSIGN TO GROUP (Only Admin) ======================
    @PostMapping("/assign/group/{itemUuid}")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<Map<String, Object>> assignToGroup(
            HttpServletRequest request,
            @PathVariable UUID itemUuid,
            @RequestParam UUID groupUuid) {

        return assignPermission(request, itemUuid, null, groupUuid, "Group");
    }

    /**
     * Common method to assign permission - STRICT ADMIN CHECK
     */
    private ResponseEntity<Map<String, Object>> assignPermission(
            HttpServletRequest request, 
            UUID itemUuid, 
            UUID userUuid, 
            UUID groupUuid, 
            String type) {

        Context context = ContextUtil.obtainContext(request);
        Map<String, Object> resp = new HashMap<>();

        try {
            Item item = itemService.find(context, itemUuid);
            if (item == null) {
                resp.put("success", false);
                resp.put("message", "Item not found");
                return ResponseEntity.badRequest().body(resp);
            }

            // ==================== STRICT ADMIN CHECK ====================
            boolean isSystemAdmin = authorizeService.isAdmin(context);
            boolean isCollectionAdmin = authorizeService.authorizeActionBoolean(
                    context, item.getOwningCollection(), Constants.ADMIN);

            if (!isSystemAdmin && !isCollectionAdmin) {
                resp.put("success", false);
                resp.put("message", "Only System Administrator or Collection Administrator can assign MARKING_EDIT permission");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }
            // ============================================================

            if ("EPerson".equals(type) && userUuid != null) {
                EPerson eperson = ePersonService.find(context, userUuid);
                if (eperson == null) {
                    resp.put("success", false);
                    resp.put("message", "User not found");
                    return ResponseEntity.badRequest().body(resp);
                }
                markingService.assignMarkingEditToEPerson(context, item, eperson);
                resp.put("assignedTo", eperson.getEmail());

            } else if ("Group".equals(type) && groupUuid != null) {
                Group group = groupService.find(context, groupUuid);
                if (group == null) {
                    resp.put("success", false);
                    resp.put("message", "Group not found");
                    return ResponseEntity.badRequest().body(resp);
                }
                markingService.assignMarkingEditToGroup(context, item, group);
                resp.put("assignedToGroup", group.getName());
            }

            resp.put("success", true);
            resp.put("message", "MARKING_EDIT permission successfully assigned by Admin");
            resp.put("itemUuid", itemUuid);
            resp.put("assignedBy", context.getCurrentUser().getEmail());

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Internal error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // ====================== CHECK IF USER CAN EDIT ======================
    @GetMapping("/can-edit/{itemUuid}")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<Map<String, Object>> canEditForMarking(
            HttpServletRequest request, @PathVariable UUID itemUuid) {

        Context context = ContextUtil.obtainContext(request);
        Map<String, Object> resp = new HashMap<>();

        try {
            Item item = itemService.find(context, itemUuid);
            if (item == null) {
                resp.put("canEdit", false);
                resp.put("message", "Item not found");
                return ResponseEntity.ok(resp);
            }

            boolean canEdit = markingService.canMarkEdit(context, item);

            resp.put("canEdit", canEdit);
            resp.put("itemUuid", itemUuid);
            resp.put("currentUser", context.getCurrentUser() != null ? 
                       context.getCurrentUser().getEmail() : null);

            return ResponseEntity.ok(resp);

        } catch (SQLException e) {
            resp.put("canEdit", false);
            resp.put("message", "Database error");
            return ResponseEntity.ok(resp);
        }
    }

    // ====================== REVOKE PERMISSION (Only Admin) ======================
    @DeleteMapping("/revoke/{itemUuid}")
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ResponseEntity<Map<String, Object>> revokePermission(
            HttpServletRequest request,
            @PathVariable UUID itemUuid,
            @RequestParam UUID userUuid) {

        Context context = ContextUtil.obtainContext(request);
        Map<String, Object> resp = new HashMap<>();

        try {
            Item item = itemService.find(context, itemUuid);
            EPerson eperson = ePersonService.find(context, userUuid);

            if (item == null || eperson == null) {
                resp.put("success", false);
                resp.put("message", "Item or User not found");
                return ResponseEntity.badRequest().body(resp);
            }

            // Same strict admin check
            boolean isAdmin = authorizeService.isAdmin(context) ||
                    authorizeService.authorizeActionBoolean(context, item.getOwningCollection(), Constants.ADMIN);

            if (!isAdmin) {
                resp.put("success", false);
                resp.put("message", "Only Admin can revoke MARKING_EDIT permission");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }

            markingService.revokeMarkingPermission(context, item, eperson);

            resp.put("success", true);
            resp.put("message", "MARKING_EDIT permission revoked successfully");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
// ====================== QUESTION-WISE MARKING ======================

/**
 * Add marks + annotation for a particular question
 * POST /server/api/marking/question/{itemUuid}
 */
@PostMapping("/question/{itemUuid}")
@PreAuthorize("hasAuthority('AUTHENTICATED')")
public ResponseEntity<Map<String, Object>> addQuestionMark(
        HttpServletRequest request,
        @PathVariable UUID itemUuid,
        @RequestBody QuestionMarkingDTO dto) {

    Context context = ContextUtil.obtainContext(request);
    Map<String, Object> resp = new HashMap<>();

    try {
        Item item = itemService.find(context, itemUuid);
        if (item == null) {
            resp.put("success", false);
            resp.put("message", "Item not found");
            return ResponseEntity.badRequest().body(resp);
        }

        // Permission check - Only assigned marker can give marks
        if (!markingService.canMarkEdit(context, item)) {
            resp.put("success", false);
            resp.put("message", "You are not authorized to mark this item");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        }

        questionMarkingService.addQuestionMark(context, item, 
                dto.getQuestionNo(), dto.getMarksObtained(), 
                dto.getTotalMarks(), dto.getAnnotation());

        resp.put("success", true);
        resp.put("message", "Marks and annotation saved for Question " + dto.getQuestionNo());
        resp.put("totalObtained", questionMarkingService.calculateTotalMarksObtained(item));

        return ResponseEntity.ok(resp);

    } catch (Exception e) {
        resp.put("success", false);
        resp.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}

/**
 * Get all question-wise marks for an Item
 */
@GetMapping("/question/{itemUuid}")
@PreAuthorize("hasAuthority('AUTHENTICATED')")
public ResponseEntity<Map<String, Object>> getQuestionMarks(
        HttpServletRequest request, @PathVariable UUID itemUuid) {

    Context context = ContextUtil.obtainContext(request);
    Map<String, Object> resp = new HashMap<>();

    try {
        Item item = itemService.find(context, itemUuid);
        if (item == null) {
            resp.put("success", false);
            resp.put("message", "Item not found");
            return ResponseEntity.badRequest().body(resp);
        }

        resp.put("success", true);
        resp.put("itemUuid", itemUuid);
        resp.put("questionMarks", questionMarkingService.getAllQuestionMarks(item));
        resp.put("totalMarksObtained", questionMarkingService.calculateTotalMarksObtained(item));

        return ResponseEntity.ok(resp);

    } catch (Exception e) {
        resp.put("success", false);
        resp.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
}