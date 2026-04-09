/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.marking;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * MarkingService for On-Screen Marking Project - DSpace 9.2
 * 
 * @author DSpace Developer
 */
@Service
public class MarkingService {

    @Autowired
    private ResourcePolicyService resourcePolicyService;

    @Autowired
    private AuthorizeService authorizeService;

    public void assignMarkingEditToEPerson(Context context, Item item, EPerson eperson)
            throws SQLException, AuthorizeException {

        cleanOldMarkingPolicies(context, item, eperson.getID(), null);

        ResourcePolicy policy = resourcePolicyService.create(context, eperson, null);

        policy.setdSpaceObject(item);
        policy.setAction(Constants.WRITE);
        policy.setRpType("MARKING_EDIT");
        policy.setRpName("Marking Edit Permission");
        policy.setRpDescription("Assigned for On-Screen Marking by Admin");

        resourcePolicyService.update(context, policy);
        context.commit();
    }

    public void assignMarkingEditToGroup(Context context, Item item, Group group)
            throws SQLException, AuthorizeException {

        cleanOldMarkingPolicies(context, item, null, group.getID());

        ResourcePolicy policy = resourcePolicyService.create(context, null, group);

        policy.setdSpaceObject(item);
        policy.setAction(Constants.WRITE);
        policy.setRpType("MARKING_EDIT");
        policy.setRpName("Marking Edit Permission (Group)");

        resourcePolicyService.update(context, policy);
        context.commit();
    }

    public boolean canMarkEdit(Context context, Item item) throws SQLException {
        if (context.getCurrentUser() == null) {
            return false;
        }
        return authorizeService.authorizeActionBoolean(context, item, Constants.WRITE, false);
    }

    public void revokeMarkingPermission(Context context, Item item, EPerson eperson)
            throws SQLException, AuthorizeException {

        List<ResourcePolicy> policies = resourcePolicyService.find(context, item);

        for (ResourcePolicy rp : policies) {
            if ("MARKING_EDIT".equals(rp.getRpType()) &&
                rp.getEPerson() != null &&
                rp.getEPerson().getID().equals(eperson.getID())) {

                resourcePolicyService.delete(context, rp);
            }
        }
        context.commit();
    }

    private void cleanOldMarkingPolicies(Context context, Item item, UUID epersonId, UUID groupId)
            throws SQLException {

        List<ResourcePolicy> policies = resourcePolicyService.find(context, item);

        for (ResourcePolicy rp : policies) {
            if ("MARKING_EDIT".equals(rp.getRpType())) {
                boolean shouldDelete = false;

                if (epersonId != null && rp.getEPerson() != null &&
                    rp.getEPerson().getID().equals(epersonId)) {
                    shouldDelete = true;
                }
                if (groupId != null && rp.getGroup() != null &&
                    rp.getGroup().getID().equals(groupId)) {
                    shouldDelete = true;
                }

                if (shouldDelete) {
                    try {
                        resourcePolicyService.delete(context, rp);
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (AuthorizeException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}