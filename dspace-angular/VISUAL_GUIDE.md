# Visual Guide - How to Use the React UI

This is a visual walkthrough of the item assignment feature UI.

## 📱 UI Screens

### Screen 1: Login Page

```
┌─────────────────────────────────┐
│      DSpace Login               │
├─────────────────────────────────┤
│                                 │
│  Email:  [user@example.com  ]   │
│  Password: [••••••••••••••]    │
│                                 │
│  [Login Button]                 │
│                                 │
└─────────────────────────────────┘
```

**What it does:**
- Enter your DSpace email
- Enter your DSpace password
- Click Login
- Redirected to dashboard after success

**Error scenarios:**
- Wrong password → "Login failed with status 401"
- Network error → "Failed to fetch"

---

### Screen 2: Admin Dashboard

```
┌────────────────────────────────────────────────────────┐
│ 🏛️ DSpace | Logged in: admin@example.edu [Logout]     │
├────────────────────────────────────────────────────────┤
│                                                        │
│ ┌──────────┐  ┌────────────────────────────────────┐ │
│ │📋 Items  │  │  My Assigned Items (42)            │ │
│ │          │  │                                    │ │
│ │📦 Batch  │  │ Title  | Handle | Email | Date   │ │
│ │Assign    │  │--------|--------|-------|----    │ │
│ │          │  │ Item1  | 123/1  | ...  | 1/15   │ │
│ │👤 Profile│  │ Item2  | 123/2  | ...  | 1/14   │ │
│ │          │  │ Item3  | 123/3  | ...  | 1/13   │ │
│ │          │  │                                    │ │
│ │          │  │ [Prev] 1 2 3 [Next]               │ │
│ └──────────┘  └────────────────────────────────────┘ │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Three tabs:**
1. **Items** - View assigned items with pagination
2. **Batch Assign** - Admin-only bulk assignment
3. **Profile** - User info and role

---

### Screen 3: Assigned Items Tab

```
┌────────────────────────────────────────────────────────┐
│ My Assigned Items (42)                                 │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Title              Handle    Assigned To Last Modified │
│ ──────────────────────────────────────────────────── │
│ Research Paper 1   123/001   admin@uni.edu  1/15/2024 │ [Unassign]
│ Dataset A          123/002   admin@uni.edu  1/14/2024 │ [Unassign]
│ Image Collection   123/003   admin@uni.edu  1/13/2024 │ [Unassign]
│ Technical Manual   123/004   admin@uni.edu  1/12/2024 │ [Unassign]
│                                                        │
│ [Prev] 1 2 3 4 5 [Next]                              │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Features:**
- Shows items assigned to you
- Click item title to view details
- Click handle to open in DSpace
- Click "Unassign" to remove from you
- Pagination for browsing
- Last modified date shown

---

### Screen 4: Batch Assignment Tab (Admin Only)

```
┌────────────────────────────────────────────────────────┐
│ Batch Assign Items to Users                            │
│ Distribute all items in collection equally among users │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Collection ID:                                         │
│ [550e8400-e29b-41d4-a716-446655440000          ]     │
│                                                        │
│ User Emails (one per line):                           │
│ ┌──────────────────────────────────┐                 │
│ │user1@university.edu              │                 │
│ │user2@university.edu              │                 │
│ │user3@university.edu              │                 │
│ │                                  │                 │
│ │                                  │                 │
│ └──────────────────────────────────┘                 │
│                                                        │
│ [  ⏳ Assigning Items...  ]                            │
│                                                        │
│ ✅ Successfully assigned 150 items to 3 users       │
│    - User 1: 50 items                                 │
│    - User 2: 50 items                                 │
│    - User 3: 50 items                                 │
│                                                        │
│ ℹ️ How it works:                                       │
│ • Items distributed equally                          │
│ • Remainders go to first users                       │
│ • Each item updated with new metadata                │
│ • Search index automatically updated                 │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**How to use:**
1. Enter the collection UUID (copy from DSpace)
2. List user emails (one per line)
3. Click "Assign Items"
4. See success message with distribution details

**Distribution example:**
- 100 items, 3 users
- User 1: 34 items
- User 2: 33 items
- User 3: 33 items

---

### Screen 5: Profile Tab

```
┌────────────────────────────────────────────────────────┐
│ User Profile                                           │
├────────────────────────────────────────────────────────┤
│                                                        │
│ Email:    admin@university.edu                        │
│                                                        │
│ Name:     Admin User                                   │
│                                                        │
│ Role:     [Admin]                                      │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**Shows:**
- Your email address
- Your full name
- Your role (Admin or Regular User)

---

## 🔄 User Workflows

### Workflow 1: Regular User Viewing Items

```
┌─────────────────────────────────────────────┐
│ Step 1: Navigate to App                     │
│ ↓                                           │
│ Step 2: Login with your email/password      │
│ ↓                                           │
│ Step 3: See "My Assigned Items" tab         │
│ ↓                                           │
│ Step 4: View items in paginated table       │
│ ↓                                           │
│ Step 5: Click item to view or unassign      │
│                                             │
│ Result: You can see all items assigned to   │
│ you and manage them                        │
└─────────────────────────────────────────────┘
```

### Workflow 2: Admin Batch Assignment

```
┌─────────────────────────────────────────────┐
│ Step 1: Login as admin user                 │
│ ↓                                           │
│ Step 2: Go to "Batch Assignment" tab        │
│ (Only visible for admins)                   │
│ ↓                                           │
│ Step 3: Enter collection UUID               │
│ (Get from DSpace collection page)           │
│ ↓                                           │
│ Step 4: Enter user emails (one per line)    │
│ ↓                                           │
│ Step 5: Click "Assign Items"                │
│ ↓                                           │
│ Step 6: See success message with counts     │
│ ↓                                           │
│ Result: Items distributed equally among     │
│ all users                                    │
└─────────────────────────────────────────────┘
```

### Workflow 3: Unassigning Items

```
┌─────────────────────────────────────────────┐
│ Step 1: View your assigned items            │
│ ↓                                           │
│ Step 2: Find item in table                  │
│ ↓                                           │
│ Step 3: Click "Unassign" button             │
│ ↓                                           │
│ Step 4: Confirm removal                     │
│ ↓                                           │
│ Step 5: Item removed from your list         │
│                                             │
│ Result: You're no longer assigned to that   │
│ item                                         │
└─────────────────────────────────────────────┘
```

---

## 🎨 UI States

### Loading State
```
┌────────────────────────────┐
│ 🔄 Loading...             │
│    (Spinning wheel)       │
└────────────────────────────┘
```

### Error State
```
┌────────────────────────────┐
│ ❌ Error: Failed to fetch  │
│    Please try again        │
└────────────────────────────┘
```

### Success State
```
┌────────────────────────────────────────┐
│ ✅ Successfully assigned 50 items to   │
│    3 users                             │
└────────────────────────────────────────┘
```

### Empty State
```
┌────────────────────────────────────────┐
│ ℹ️ No items assigned to you yet       │
│                                        │
│ Contact admin to get items assigned    │
└────────────────────────────────────────┘
```

---

## 🎯 Quick Reference Card

### Login Form
- **Email field**: Your DSpace email
- **Password field**: Your DSpace password
- **Result**: JWT token stored in browser

### Items Tab
- **Table shows**: Title, Handle, Assigned To, Last Modified
- **Actions**: Click title to view, unassign button
- **Pagination**: 10 items per page
- **Count**: Total items shown in title

### Batch Assignment Tab (Admin)
- **Collection ID**: UUID from DSpace collection
- **User Emails**: One email per line
- **Result**: Items distributed equally
- **Remainders**: First users get extra items

### Profile Tab
- **Email**: Your logged-in email
- **Name**: Your full name
- **Role**: ADMIN or AUTHENTICATED

---

## 🔗 Navigation Map

```
App Entry
  ↓
Login Page
  ├─ Success → Admin Dashboard
  │            ├─ Tab 1: Items List
  │            ├─ Tab 2: Batch Assign (if admin)
  │            └─ Tab 3: Profile
  │
  └─ Failure → Error message, retry login
```

---

## 📊 Data Flow Diagram

```
User Actions (UI)
     ↓
React Components
     ↓
Custom Hooks (API calls)
     ↓
REST API (Backend)
     ↓
DSpace Services
     ↓
Solr Search Index
     ↓
Database
```

---

## 🎓 Example User Sessions

### Session 1: Regular User
```
1. Go to http://localhost:3000
2. See login form
3. Enter: user@example.com / password
4. Click Login
5. See AdminDashboard with "Items" tab
6. See 5 items in table
7. Click "Unassign" on first item
8. Confirm removal
9. See 4 items now
10. Done!
```

### Session 2: Admin User
```
1. Go to http://localhost:3000
2. See login form
3. Enter: admin@example.com / admin-password
4. Click Login
5. See AdminDashboard with 3 tabs
6. Click "Batch Assignment" tab
7. Enter collection UUID
8. Enter 2 user emails
9. Click "Assign Items"
10. See success message
11. Navigate to "Items" tab
12. See your assigned items (if any)
13. Done!
```

---

## 🔍 Troubleshooting Visual Guide

### Issue: Can't Login

```
❌ Problem:
Login button not responding

✅ Solution:
1. Check DSpace backend is running (http://localhost:8080)
2. Verify email/password in DSpace admin
3. Check browser console (F12) for errors
4. Try different credentials
5. Check CORS is enabled
```

### Issue: No Items Show

```
❌ Problem:
Items list is empty but items were assigned

✅ Solution:
1. Log in with correct user
2. Check that items are assigned to this email
3. Run Solr reindex (DSpace admin → Search)
4. Refresh page (F5)
5. Try again
```

### Issue: Admin Features Not Showing

```
❌ Problem:
"Batch Assignment" tab missing

✅ Solution:
1. Verify you're logged in as admin
2. Check your roles in DSpace admin UI
3. You should see "Admin" badge in profile
4. Re-login if needed
5. Check server logs for authorization errors
```

---

## 📱 Mobile View

On mobile devices, the UI adapts:

```
┌──────────────────┐
│ 🏛️ DSpace        │ ← Navbar collapses
│ [≡] [Logout]     │
├──────────────────┤
│                  │
│ Stack Layout:    │
│ ┌──────────────┐ │
│ │📋 Items      │ │ ← Navigation buttons stack
│ ├──────────────┤ │
│ │📦 Batch      │ │
│ ├──────────────┤ │
│ │👤 Profile    │ │
│ └──────────────┘ │
│                  │
│ ┌──────────────┐ │
│ │ Items table  │ │
│ │ scrolls      │ │
│ │ horizontally │ │
│ └──────────────┘ │
│                  │
└──────────────────┘
```

---

## 🎮 Keyboard Shortcuts

- **Tab** - Navigate between form fields
- **Enter** - Submit form / Click active button
- **F12** - Open developer console
- **Ctrl+R** - Refresh page

---

## 💡 Pro Tips

1. **Collection ID**: Copy from DSpace collection edit page
2. **User Emails**: Check spelling carefully (no validation yet)
3. **Pagination**: Jump to any page using page buttons
4. **Unassign**: Can't undo - items stay in DSpace, just unassigned
5. **Batch Assign**: Test with small collection first
6. **Token Expiry**: Login again if session expires
7. **Performance**: Items load 10 at a time to be fast

---

## ✅ Feature Checklist

In the UI, you can:
- [x] Login with email/password
- [x] View your assigned items
- [x] See total count
- [x] Paginate through items
- [x] Unassign items
- [x] As admin: Batch assign items
- [x] See success/error messages
- [x] View your profile
- [x] Logout
- [x] Responsive on mobile

---

## 🎓 Learning by Doing

**Try this sequence:**

1. **Login** - Enter your email/password
2. **Explore** - Look at items, counts, table
3. **Paginate** - Click through pages
4. **Unassign** - Try removing an item
5. **If Admin**:
   - Go to Batch Assign
   - Enter a test collection ID
   - Enter one email address
   - Assign and see results

---

## 📞 Getting Help

- **"What's this button for?"** - Hover over it, check label
- **"How do I do X?"** - Check corresponding tab/form
- **"Error occurred"** - Check browser console (F12)
- **"Still stuck?"** - Read REACT_TESTING_GUIDE.md

---

**Ready to try it?** Follow QUICK_SETUP_GUIDE.md to get running in 10 minutes!
