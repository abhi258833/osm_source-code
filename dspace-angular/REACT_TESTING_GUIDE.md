# React UI Testing Guide - Item Assignment Feature

This guide explains how to test the assigned items feature from the React UI.

## Prerequisites

- React 16.8+ (for hooks support)
- TypeScript 4.0+
- Node.js 14+
- DSpace 9.2 backend running with the new item assignment APIs
- Bootstrap 5 (for styling)

## Component Structure

```
dspace-angular/src/app/
├── contexts/
│   └── AuthContext.tsx          # Authentication state management
├── hooks/
│   └── useAssignedItems.ts      # API integration hooks
└── components/
    ├── LoginForm.tsx             # User login
    ├── AssignedItemsList.tsx      # View assigned items
    ├── BatchAssignmentForm.tsx    # Admin batch assignment
    └── AdminDashboard.tsx         # Main admin interface
```

## Component Usage

### 1. Wrap Your App with AuthProvider

```typescript
import React from 'react';
import { AuthProvider } from './contexts/AuthContext';
import AdminDashboard from './components/AdminDashboard';

function App() {
  return (
    <AuthProvider>
      <AdminDashboard />
    </AuthProvider>
  );
}

export default App;
```

### 2. Using the Components

#### LoginForm
- Displays email/password login fields
- Calls `/api/authn/login` endpoint
- Stores authentication token in localStorage
- Shows "Already logged in" if user is authenticated

```typescript
import LoginForm from './components/LoginForm';

<LoginForm />
```

#### AssignedItemsList
- Shows all items assigned to the current user
- Displays item title, handle, assignment email, and last modified date
- Includes pagination controls
- Shows count of assigned items
- Allows unassigning individual items

```typescript
import AssignedItemsList from './components/AssignedItemsList';

<AssignedItemsList />
```

#### BatchAssignmentForm
- Admin-only component for batch assignment
- Takes collection ID and list of user emails
- Distributes items equally among users
- Shows success/error messages

```typescript
import BatchAssignmentForm from './components/BatchAssignmentForm';

<BatchAssignmentForm collectionId="123e4567-e89b-12d3-a456-426614174000" />
```

#### AdminDashboard
- Complete admin interface with tabbed navigation
- Includes login, item list, batch assignment, and profile
- Requires admin role to access batch assignment tab

```typescript
import AdminDashboard from './components/AdminDashboard';

<AdminDashboard />
```

## Testing Scenarios

### Scenario 1: User Login and View Assigned Items

**Steps:**
1. Navigate to the React app
2. Enter your DSpace user email and password
3. Click "Login"
4. You should see your assigned items list
5. The count should display the total number of items assigned to you

**Expected API Calls:**
```
POST /api/authn/login
  Headers: Content-Type: application/json
  Body: { email: "user@example.com", password: "password" }
  Response: { email, name, token, roles }

GET /api/items/assigned-to-me/count
  Headers: Authorization: Bearer <token>
  Response: { count: 5 }

GET /api/items/assigned-to-me?page=0&limit=10
  Headers: Authorization: Bearer <token>
  Response: {
    content: [
      { id: "uuid1", title: "Item 1", handle: "123/456", assignedTo: "user@example.com", lastModified: "2024-01-15" },
      ...
    ],
    totalElements: 5,
    totalPages: 1,
    currentPage: 0,
    pageSize: 10
  }
```

**Verification:**
- Login form disappears after successful login
- Items table displays with correct data
- Item count matches displayed items
- Pagination works (if more than 10 items)

### Scenario 2: Admin Batch Assignment

**Prerequisites:**
- Logged in user must have admin role
- Target collection must have at least 3 items
- Target users must exist in DSpace

**Steps:**
1. Login with admin account
2. Navigate to "Batch Assignment" tab
3. Enter a collection UUID
4. Enter 2-3 user email addresses (one per line)
5. Click "Assign Items"
6. Observe success message with count of assigned items

**Expected API Call:**
```
POST /api/admin/items/{collectionId}/assign-to-users
  Headers: 
    Authorization: Bearer <admin-token>
    Content-Type: application/json
  Body: {
    userEmails: ["user1@example.com", "user2@example.com"]
  }
  Response: {
    status: "success",
    message: "Assigned X items to 2 users",
    itemsAssigned: X
  }
```

**Verification:**
- Form shows success message
- Count matches expected distribution (items / users with remainder handling)
- No items appear unassigned
- Search index is updated

### Scenario 3: Pagination

**Steps:**
1. Create 25+ items and assign to a user
2. Navigate to assigned items page
3. Click through pagination controls
4. Verify different items appear on each page

**Expected Behavior:**
- First page shows 10 items by default
- Next button disabled on last page
- Previous button disabled on first page
- Total page count calculated correctly

### Scenario 4: Error Handling

**Test Cases:**
1. **Invalid login:**
   - Enter wrong email/password
   - Should show error message: "Login failed with status 401"

2. **Unauthorized access to admin feature:**
   - Login with non-admin user
   - Try to access batch assignment tab
   - Should show: "Admin access required to perform batch assignments"

3. **Network error:**
   - Disable internet/mock failed response
   - Should show appropriate error message
   - Users can retry without page reload

4. **Invalid collection ID:**
   - Enter non-existent collection UUID
   - Click assign
   - Should show error from backend

## Manual Testing Checklist

- [ ] User can login with valid credentials
- [ ] User cannot login with invalid credentials
- [ ] Logged-in user sees their assigned items
- [ ] Item count displays correctly
- [ ] Pagination works with multiple pages
- [ ] User can unassign individual items
- [ ] Admin can batch assign items
- [ ] Items distributed equally among users
- [ ] Remainders go to first users
- [ ] Success message shows item count
- [ ] Logout clears token and session
- [ ] Form validation prevents empty submissions
- [ ] Loading spinners display during API calls
- [ ] Error messages are user-friendly
- [ ] Token persists on page refresh (if within expiry)

## Integration with Your DSpace Angular UI

### Option 1: Standalone Dashboard Route

```typescript
// In your routing module
const routes: Routes = [
  {
    path: 'admin/assigned-items',
    component: AdminDashboard,
    canActivate: [AuthenticationGuard]
  }
];
```

### Option 2: Add Tab to Existing Admin Panel

```typescript
// In your existing admin component
import AdminDashboard from './AdminDashboard';

<div class="admin-tabs">
  <div class="tab">
    <button (click)="activeTab = 'assigned-items'">Assigned Items</button>
  </div>
  <div class="tab-content">
    <app-admin-dashboard *ngIf="activeTab === 'assigned-items'"></app-admin-dashboard>
  </div>
</div>
```

### Option 3: Use Hooks in Your Existing Components

```typescript
// In your existing component
import { useAssignedItems, useAssignedItemsCount } from '../hooks/useAssignedItems';

export function MyExistingComponent() {
  const { data, loading, error } = useAssignedItems(0, 10);
  const { count } = useAssignedItemsCount();

  return (
    <div>
      <h3>My Items: {count}</h3>
      {/* Display data */}
    </div>
  );
}
```

## Debugging

### Check Browser Console

Look for:
- Network errors (Status 401, 403, 404, 500)
- TypeScript/JavaScript errors
- Token not being sent in headers

### Enable Debug Logging

Add to components:
```typescript
console.log('Auth state:', { user, isAuthenticated, token: localStorage.getItem('dspace_token') });
console.log('API Response:', data);
console.log('Error:', error);
```

### Network Inspector (DevTools)

Check each API request:

1. **Login Request**
   - URL: `POST /api/authn/login`
   - Headers: `Content-Type: application/json`
   - Status: 200 with token in response

2. **Assigned Items Request**
   - URL: `GET /api/items/assigned-to-me?page=0&limit=10`
   - Headers: `Authorization: Bearer <token>`
   - Status: 200 with items array

3. **Admin Assignment Request**
   - URL: `POST /api/admin/items/{collectionId}/assign-to-users`
   - Headers: `Authorization: Bearer <admin-token>`
   - Status: 200 with success message

## Common Issues & Solutions

### Issue: "User not authenticated" error

**Cause:** Token not stored or expired
**Solution:** 
- Check localStorage for `dspace_token`
- Re-login
- Verify token format is correct

### Issue: CORS errors

**Cause:** Backend not configured for CORS
**Solution:**
- Add CORS headers to DSpace backend
- Ensure Authorization header is allowed
- Check browser console for specific error

### Issue: Empty items list despite assignments

**Cause:** Search index not updated or different user
**Solution:**
- Verify you're logged in as correct user
- Manually trigger Solr reindex from DSpace admin
- Check metadata field name matches `dc.assigned.user`

### Issue: Batch assignment shows 0 items

**Cause:** Collection empty or UUID incorrect
**Solution:**
- Verify collection UUID is correct
- Check collection has items using DSpace admin UI
- Verify collection ID format (should be UUID)

## Performance Notes

- Items loaded with pagination (10 per page by default)
- Count fetched separately and cached
- No automatic refresh - user must click "Refetch" or navigate
- Token stored in localStorage for convenience (consider using secure cookies in production)

## Security Considerations

⚠️ **For Testing/Development Only**

For production:
- [ ] Use HTTPS for all API calls
- [ ] Store tokens in secure HttpOnly cookies instead of localStorage
- [ ] Implement token refresh mechanism
- [ ] Add CSRF protection
- [ ] Validate all user inputs
- [ ] Implement rate limiting on auth endpoint
- [ ] Add audit logging for batch operations

## Next Steps

1. Start DSpace 9.2 backend with item assignment APIs
2. Update API base URL in components (currently assumes same domain)
3. Deploy React components to your DSpace Angular UI
4. Create admin route or add to existing admin panel
5. Test with real DSpace data
6. Collect user feedback and iterate
