# Assignment Workflow Component

## Overview

The Assignment Workflow Component provides a 4-step UI for DSpace administrators to assign items from collections to users. Rather than manually entering UUIDs, administrators navigate through a familiar community/subcommunity/collection hierarchy and select recipients using checkboxes.

## Directory Structure

```
admin-assignment-workflow/
├── assignment-workflow.component.tsx       # Main React component (4-step UI)
├── assignment-workflow.component.scss      # Component styles
├── assignment-workflow.module.ts           # Angular module definition
└── README.md                               # This file
```

## Component Description

### AssignmentWorkflowComponent

**Type:** React Functional Component  
**Framework:** Angular with React support (or pure React)  
**State Management:** React Hooks (useState, useEffect)  

### Workflow Steps

#### Step 1: Community Selection
- Displays all root communities
- Shows community names and handles
- User clicks to select a community
- Fetches subcommunities if available
- Transitions to Step 2

#### Step 2: Collection Selection  
- Shows subcommunities (if any) for further drilling down
- Lists all collections in the community/subcommunity
- User selects a collection
- Transitions to Step 3

#### Step 3: User Selection
- Displays all EPerson users in the system
- Shows user name and email
- Users can check/uncheck boxes for selection
- Summary shows selected user count
- Transitions to Step 4

#### Step 4: Confirmation & Execution
- Summary of collection and selected users
- Explains equal distribution
- "Confirm Assignment" button executes the workflow
- Shows success/error message
- Returns to Step 1 or home on completion

## Component State

```typescript
// Navigation
- step: 1 | 2 | 3 | 4             // Current workflow step

// Data Collections
- communities: Community[]         // Root communities
- subcommunities: Community[]      // Subcommunities of selected community
- collections: Collection[]        // Collections in community
- users: EPerson[]                 // All available users

// Selections
- selectedCommunity: Community      // Selected root community
- selectedSubcommunity: Community   // Selected subcommunity (optional)
- selectedCollection: Collection    // Selected collection
- selectedUsers: string[]           // Array of selected user IDs

// UI State
- loading: boolean                 // Loading indicator
- error: string | null             // Error message
- success: string | null           // Success message
```

## API Endpoints Used

### GET /api/assignment-workflow/communities
Fetches root communities with their subcommunities.

**Expected Response:**
```json
[
  {
    "id": "uuid",
    "name": "Research",
    "handle": "123456/1",
    "subcommunities": [
      {
        "id": "uuid",
        "name": "Physics",
        "handle": "123456/2"
      }
    ]
  }
]
```

### GET /api/assignment-workflow/communities/{communityId}/subcommunities
Fetches subcommunities of a given community.

### GET /api/assignment-workflow/communities/{communityId}/collections
Fetches collections within a community.

**Expected Response:**
```json
[
  {
    "id": "uuid",
    "name": "Experimental Data",
    "handle": "123456/3"
  }
]
```

### GET /api/assignment-workflow/users
Fetches all available users.

**Expected Response:**
```json
[
  {
    "id": "uuid",
    "email": "user@example.com",
    "name": "John Doe",
    "checked": false
  }
]
```

### POST /api/assignment-workflow/assign
Executes the item assignment.

**Request Body:**
```json
{
  "collectionId": "uuid",
  "selectedUserIds": ["uuid1", "uuid2", "uuid3"]
}
```

**Expected Response:**
```json
{
  "status": "success",
  "message": "Items in collection have been assigned to 3 users"
}
```

## Key Methods

### fetchCommunities()
```typescript
private async fetchCommunities(): Promise<void>
```
Loads root communities on component initialization.

### fetchSubcommunities(communityId: string)
Loads subcommunities when a root community is selected.

### fetchCollections(communityId: string)
Loads collections when a community or subcommunity is selected.

### fetchUsers()
Loads all available users when transitioning to Step 3.

### handleCommunitySelect(community: Community)
Handles community selection:
- Sets selectedCommunity state
- Clears previous selections
- Fetches subcommunities or collections

### handleSubcommunitySelect(subcommunity: Community)
Handles subcommunity selection:
- Sets selectedSubcommunity state
- Fetches collections

### handleCollectionSelect(collection: Collection)
Handles collection selection:
- Sets selectedCollection state
- Transitions to Step 3 (user selection)
- Fetches user list

### handleUserToggle(userId: string)
Toggles a user's selection status.

### handleAssign()
Executes the assignment:
- Validates inputs
- Calls POST /api/assignment-workflow/assign
- Shows success/error message
- Resets form on success

### resetForm()
Resets all state to initial values for new workflow.

## Styling

### Color Scheme
- **Primary:** `#007bff` (Blue) - For primary selections and actions
- **Secondary:** `#6c757d` (Gray) - For secondary buttons
- **Success:** `#28a745` (Green) - For success states
- **Warning:** `#ffc107` (Yellow) - For confirmation messages
- **Danger:** `#dc3545` (Red) - For cancel/error actions
- **Info:** `#17a2b8` (Teal) - For subcommunities

### Component Layout
- **Max Width:** 900px (responsive down to mobile)
- **Grid Layout:** Used for community/collection cards
- **Responsive Breakpoints:** 768px for mobile layouts

### Key CSS Classes
- `.assignment-workflow-container` - Root container
- `.workflow-step` - Each step section
- `.community-list` / `.collection-list` / `.subcommunity-list` - Card grids
- `.user-list` - User checkbox list with scroll
- `.selected-users-summary` - Summary of selected users
- `.confirmation-summary` - Final review section
- `.workflow-buttons` - Navigation buttons

## Dependencies

### Angular/React
- React Hooks (useState, useEffect)
- Basic HTML/CSS
- Fetch API for HTTP requests

### External Libraries
- None (uses native browser APIs)

### DSpace Dependencies  
- Authentication (Bearer token in localStorage)
- Authorization headers passed to API

## Error Handling

### Component-level Errors
```typescript
try {
  // API call
} catch (error) {
  setError(error.message);
  console.error('Specific error context:', error);
}
```

### User-facing Error Messages
- "Collection not found" - Collection doesn't exist or is inaccessible
- "At least one user must be selected" - No users checked
- "Failed to load [communities|users|collections]" - API failure
- "Database error occurred" - Server-side SQL error
- "An unexpected error occurred" - Unknown error

### Recovery
- Users can go back and try again
- Form can be reset with "Cancel" button
- Error alerts are dismissible
- Successful operations show completion message

## Security Considerations

- ✅ All API calls include `Authorization: Bearer` header
- ✅ Admin role required (enforced server-side)
- ✅ No sensitive data stored in component state
- ✅ Auth token should be httpOnly and secure
- ✅ All user input validated server-side

## Performance Optimizations

1. **Lazy Loading:** Subcommunities and collections fetched on demand
2. **State Caching:** Communities cached on initial load
3. **User List:** All users fetched once on Step 3
4. **Async Operations:** All API calls are async to prevent UI blocking

## Testing

### Unit Test Example
```typescript
describe('AssignmentWorkflowComponent', () => {
  it('should load communities on init', async () => {
    // Mock API response
    // Render component
    // expect(communities.length).toBeGreaterThan(0);
  });

  it('should toggle user selection', () => {
    // Select a user
    // Verify checkbox state
    // Verify selectedUsers array updated
  });

  it('should execute assignment with valid inputs', async () => {
    // Select community, collection, users
    // Click confirm
    // Expect success message
    // Verify API called with correct data
  });
});
```

## Browser Compatibility

- ✅ Chrome/Edge (latest)
- ✅ Firefox (latest)
- ✅ Safari (latest)
- ✅ Mobile browsers
- ⚠️ IE11 (not supported, requires polyfills)

## Accessibility

- ✅ Semantic HTML structure
- ✅ Label associations for checkboxes
- ✅ ARIA labels on interactive elements
- ✅ Keyboard navigation support
- ✅ Color not the only indicator

## Future Enhancements

- [ ] Search/filter for communities and collections
- [ ] Typed search for users
- [ ] Bulk actions (select all/none)
- [ ] Drag-and-drop reordering
- [ ] Advanced distribution options (weighted, random, etc.)
- [ ] Email preview before sending
- [ ] Assignment history modal
- [ ] Undo/rollback functionality

## Troubleshooting

### Component doesn't render
- Check that AssignmentWorkflowModule is imported in AdminModule
- Verify route is correctly configured
- Check browser console for JavaScript errors

### No communities loading
- Verify backend API is running
- Check network tab in DevTools
- Ensure admin token is valid
- Check server logs for errors

### Users not appearing
- Verify EPersonService is configured correctly
- Check that users exist in database
- Review error message in UI for hints

### Assignment fails silently
- Check browser console for error messages
- Review network tab for failed requests
- Check DSpace server logs
- Verify collection ID is valid

## Contributing

When modifying this component:
1. Follow existing code style and conventions
2. Add appropriate error handling
3. Update this README with significant changes
4. Test all workflow steps
5. Verify accessibility features
6. Update related documentation

## Files Related to This Component

- Integration Guide: `../../ASSIGNMENT_WORKFLOW_INTEGRATION.md`
- Quick Start Guide: `../../ASSIGNMENT_WORKFLOW_QUICK_START.md`
- Implementation Summary: `../../ASSIGNMENT_WORKFLOW_IMPLEMENTATION.md`
- Backend Controller: `../../dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignmentWorkflowController.java`
- Service Layer: `../../dspace-api/src/main/java/org/dspace/content/AssignedItemService.java`

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Apr 2026 | Initial implementation with 4-step workflow |

---

**Last Updated:** April 2026  
**Status:** Ready for Production  
**Maintainer:** DSpace Team
