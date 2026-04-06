# Quick Setup Guide - React UI for Item Assignment

Get the React UI up and running in 10 minutes!

## Installation

### 1. Copy Files to Your Project

```bash
# Copy React components
cp -r dspace-angular/src/app/components/* your-project/src/app/components/
cp -r dspace-angular/src/app/contexts/* your-project/src/app/contexts/
cp -r dspace-angular/src/app/hooks/* your-project/src/app/hooks/
cp -r dspace-angular/src/app/modules/* your-project/src/app/modules/
```

### 2. Install Dependencies

```bash
npm install
# or
yarn install
```

**Required Dependencies:**
- React 16.8+ (for hooks)
- React Router (for navigation)
- Bootstrap 5 (for styling)

```bash
npm install react@latest react-dom@latest bootstrap@latest react-router-dom@latest
```

### 3. Add Bootstrap CSS

In your `index.tsx` or `App.tsx`:

```typescript
import 'bootstrap/dist/css/bootstrap.min.css';
import './modules/AssignedItemsModule.css';
```

## Basic Usage

### Option 1: Use Complete Module (Easiest)

```typescript
// App.tsx
import React from 'react';
import AssignedItemsModule from './app/modules/AssignedItemsModule';

function App() {
  return (
    <div>
      <header>Your App Header</header>
      <AssignedItemsModule />
    </div>
  );
}

export default App;
```

### Option 2: Add to Routing

```typescript
// App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AdminDashboard from './app/components/AdminDashboard';
import { AuthProvider } from './app/contexts/AuthContext';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/admin/assigned-items" element={<AdminDashboard />} />
          {/* Your other routes */}
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
```

### Option 3: Use Individual Components

```typescript
import React from 'react';
import { AuthProvider } from './app/contexts/AuthContext';
import LoginForm from './app/components/LoginForm';
import AssignedItemsList from './app/components/AssignedItemsList';

function MyComponent() {
  return (
    <AuthProvider>
      <div className="container mt-5">
        <LoginForm />
        <AssignedItemsList />
      </div>
    </AuthProvider>
  );
}

export default MyComponent;
```

## Configuration

### Set API Base URL

Edit the API calls in `src/app/hooks/useAssignedItems.ts`:

**Current (localhost):**
```typescript
const response = await fetch('/api/items/assigned-to-me?page=0&limit=10', {
```

**For production server:**
```typescript
const API_BASE = 'https://your-dspace-server.edu';
const response = await fetch(`${API_BASE}/api/items/assigned-to-me?page=0&limit=10`, {
```

## Testing Locally

### Start DSpace Backend

```bash
cd dspace
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend will run on: `http://localhost:8080`

### Start React Dev Server

```bash
npm start
```

React will run on: `http://localhost:3000`

### Test Flow

1. **Navigate to** `http://localhost:3000`
2. **Login with** your DSpace user:
   - Email: `your-user@example.com`
   - Password: Your DSpace password
3. **View assigned items** in the list
4. **If admin user**, navigate to "Batch Assignment" tab
5. **Enter a collection ID** and user emails
6. **Click "Assign Items"**

## Test Data Setup

### Create Test Collection

In DSpace Admin UI:
1. Create a new collection named "Test Items"
2. Copy its UUID (e.g., `550e8400-e29b-41d4-a716-446655440000`)
3. Upload 10-20 test items

### Create Test Users

In DSpace Admin UI or via API:
```bash
# Create users (use your preferred method)
# user1@example.com
# user2@example.com
# user3@example.com
```

### Assign Admin Role (if needed)

```bash
# Via DSpace admin UI:
# 1. Find your user
# 2. Add ADMIN role
```

## Common Issues & Fixes

### Issue: "Failed to fetch" or CORS Error

**Cause:** Backend doesn't have CORS enabled

**Fix:**
1. Add to `dspace/config/local.cfg`:
```properties
cors.allowed-origins = http://localhost:3000
cors.allowed-methods = GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers = Content-Type,Authorization
```

2. Restart DSpace

### Issue: "User not authenticated" after login

**Cause:** Token not being stored or sent

**Fix:**
1. Check browser DevTools > Application > Local Storage
2. Verify `dspace_token` key exists
3. Check Network tab - verify `Authorization` header in requests

### Issue: "Cannot read property 'getIndexableObjects'" 

**Cause:** Backend API changed

**Fix:**
1. Verify DSpace 9.2 is running
2. Check `AssignedItemServiceImpl` has latest code
3. Rebuild backend: `mvn clean install -DskipTests`

### Issue: Login works but no items appear

**Cause:** No items assigned or query not working

**Fix:**
1. Use DSpace admin UI to verify items have `dc.assigned.user` metadata
2. Run Solr reindex: Visit `/admin/search/reindex` in DSpace UI
3. Check browser console for API errors

## Browser DevTools Debugging

### Step 1: Check Console for Errors
```
F12 → Console
Look for red error messages
```

### Step 2: Check Network Requests
```
F12 → Network
Make a request (e.g., click "Login")
Click on each request:
  - Status should be 200 or 401 (but not 0 or error)
  - Response should have valid JSON
```

### Step 3: Check Local Storage
```
F12 → Application → Local Storage
Look for:
  - dspace_token (should be a JWT)
  - dspace_user (should be JSON with email, name, isAdmin)
```

### Step 4: Check Token Validity
```javascript
// Paste in console:
const token = localStorage.getItem('dspace_token');
if (token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  console.log('Token payload:', payload);
  console.log('Expires:', new Date(payload.exp * 1000));
}
```

## Deployment

### Build for Production

```bash
npm run build
```

This creates an optimized build in `build/` directory.

### Deploy to Web Server

```bash
# Copy to your web server
cp -r build/* /var/www/html/assigned-items/

# Or deploy with Docker:
docker build -t assigned-items-ui .
docker run -p 80:3000 assigned-items-ui
```

### Update Backend Configuration

Ensure DSpace knows about your React UI origin:

In `dspace/config/local.cfg`:
```properties
# For production
cors.allowed-origins = https://your-domain.edu
```

## Customization

### Change Colors/Branding

Edit `src/app/modules/AssignedItemsModule.css` to customize colors:

```css
/* Primary color */
.btn-primary, .list-group-item.active {
  background-color: #your-color;
}

/* Navbar */
.navbar {
  background-color: #your-color;
}
```

### Add Your Logo

In `AdminDashboard.tsx`:

```typescript
<span className="navbar-brand mb-0 h1">
  <img src="/your-logo.png" width="30" height="30" className="d-inline-block align-text-top mr-2" />
  DSpace Item Assignment
</span>
```

### Customize Item Display

In `AssignedItemsList.tsx`, modify table columns:

```typescript
<thead className="table-dark">
  <tr>
    <th>Title</th>
    <th>Handle</th>
    <th>Assigned To</th>
    <th>Your Custom Column</th>  {/* Add new columns */}
    <th>Last Modified</th>
    <th>Actions</th>
  </tr>
</thead>
```

## Next Steps

1. ✅ Set up React project
2. ✅ Configure API endpoints
3. ✅ Test with local DSpace
4. 📋 Customize styling to match your brand
5. 📋 Add additional features (export, bulk actions)
6. 📋 Deploy to production

## Support & Documentation

- **API Reference**: See `REACT_API_DOCUMENTATION.md`
- **Testing Guide**: See `REACT_TESTING_GUIDE.md`
- **Backend Setup**: See `DEPLOYMENT_GUIDE.md` in dspace-api folder

## Performance Tips

1. **Pagination**: Items loaded 10 at a time by default
2. **Lazy Loading**: Consider lazy loading components for large lists
3. **Memoization**: Use `React.memo()` for list item components
4. **Caching**: Implement API response caching for frequently accessed data

```typescript
// Example memoization
const ItemRow = React.memo(({ item, onUnassign }: ItemRowProps) => (
  <tr>
    <td>{item.title}</td>
    {/* ... */}
  </tr>
));
```

## Security Checklist

- [ ] Token stored securely (consider httpOnly cookies)
- [ ] CORS properly configured
- [ ] HTTPS enabled in production
- [ ] Input validation on all forms
- [ ] Error messages don't leak sensitive info
- [ ] Audit logging enabled for admin operations
- [ ] Rate limiting configured
- [ ] Token refresh implemented if needed

## Troubleshooting Checklist

Before reporting issues:
- [ ] Backend running on expected port
- [ ] CORS enabled
- [ ] Browser console has no errors
- [ ] Network requests show correct status codes
- [ ] Local storage has token
- [ ] Token not expired
- [ ] Collection ID format is UUID
- [ ] Email addresses are valid
- [ ] Users exist in DSpace
- [ ] Admin user has ADMIN role

## Need Help?

1. Check browser console (F12)
2. Review error messages carefully
3. Check API responses in Network tab
4. Try the cURL examples in API docs
5. Review DSpace server logs
6. Check DSpace admin UI for data consistency
