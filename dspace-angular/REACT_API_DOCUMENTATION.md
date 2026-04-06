# React UI - REST API Integration Guide

This document describes how the React UI components interact with the DSpace backend REST APIs for item assignment.

## Base Configuration

All API requests should be made to the DSpace server base URL. By default:
```
Base URL: http://localhost:8080/server
```

Update this in your component configuration if different.

## Authentication API

### POST /api/authn/login

Authenticate a user and obtain a JWT token.

**Request:**
```http
POST /api/authn/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "roles": ["AUTHENTICATED"]
}
```

**Response (401 Unauthorized):**
```json
{
  "error": "Invalid email or password"
}
```

**Usage in React:**
```typescript
const response = await fetch('/api/authn/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const data = await response.json();
localStorage.setItem('dspace_token', data.token);
```

## Items APIs

### GET /api/items/assigned-to-me

Retrieve items assigned to the currently authenticated user.

**Request:**
```http
GET /api/items/assigned-to-me?page=0&limit=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | integer | No | Page number (0-indexed), default 0 |
| limit | integer | No | Items per page, default 10 |

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Sample Item Title",
      "handle": "123456789/1",
      "assignedTo": "user@example.com",
      "lastModified": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10
}
```

**Response (401 Unauthorized):**
```json
{
  "error": "User not authenticated"
}
```

**Usage in React:**
```typescript
const token = localStorage.getItem('dspace_token');
const response = await fetch('/api/items/assigned-to-me?page=0&limit=10', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const items = await response.json();
```

### GET /api/items/assigned-to-me/count

Get the total count of items assigned to the current user.

**Request:**
```http
GET /api/items/assigned-to-me/count
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "count": 42
}
```

**Usage in React:**
```typescript
const token = localStorage.getItem('dspace_token');
const response = await fetch('/api/items/assigned-to-me/count', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const data = await response.json();
console.log('Total items:', data.count);
```

## Admin APIs

### POST /api/admin/items/{collectionId}/assign-to-users

Perform batch assignment of all items in a collection to multiple users.

**Request:**
```http
POST /api/admin/items/550e8400-e29b-41d4-a716-446655440000/assign-to-users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userEmails": [
    "user1@example.com",
    "user2@example.com",
    "user3@example.com"
  ]
}
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| collectionId | UUID | Yes | UUID of the collection in URL path |
| userEmails | string[] | Yes | Array of user email addresses |

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Successfully assigned 30 items to 3 users",
  "itemsAssigned": 30,
  "usersAssigned": 3,
  "itemsPerUser": [10, 10, 10]
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Collection not found"
}
```

**Response (403 Forbidden):**
```json
{
  "error": "User does not have admin privileges"
}
```

**Usage in React:**
```typescript
const token = localStorage.getItem('dspace_token');
const collectionId = '550e8400-e29b-41d4-a716-446655440000';
const userEmails = ['user1@example.com', 'user2@example.com'];

const response = await fetch(
  `/api/admin/items/${collectionId}/assign-to-users`,
  {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ userEmails })
  }
);

const result = await response.json();
console.log(`Assigned ${result.itemsAssigned} items to ${result.usersAssigned} users`);
```

## Item Operations

### DELETE /api/items/{itemId}/unassign

Unassign an item from the current user.

**Request:**
```http
DELETE /api/items/550e8400-e29b-41d4-a716-446655440001/unassign
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Item unassigned successfully"
}
```

**Response (404 Not Found):**
```json
{
  "error": "Item not found"
}
```

**Usage in React:**
```typescript
const token = localStorage.getItem('dspace_token');
const itemId = '550e8400-e29b-41d4-a716-446655440001';

const response = await fetch(
  `/api/items/${itemId}/unassign`,
  {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);

if (response.ok) {
  console.log('Item unassigned');
}
```

## Error Handling

All endpoints return standard HTTP status codes:

| Status | Meaning | Example |
|--------|---------|---------|
| 200 | Success | Item fetched or assigned |
| 400 | Bad Request | Invalid collection ID format |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | Non-admin user trying admin endpoint |
| 404 | Not Found | Collection or item doesn't exist |
| 500 | Server Error | Backend error during processing |

**Error Response Format:**
```json
{
  "error": "Description of what went wrong",
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400
}
```

**Handling Errors in React:**
```typescript
try {
  const response = await fetch('/api/items/assigned-to-me', {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 401) {
    // Token expired or invalid
    localStorage.removeItem('dspace_token');
    // Redirect to login
  } else if (response.status === 403) {
    // User doesn't have permission
    console.error('Access denied');
  } else if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  const data = await response.json();
  return data;
} catch (error) {
  console.error('API Error:', error.message);
  throw error;
}
```

## Request/Response Examples

### Complete Flow: Login → View Items → Batch Assign

**1. Login**
```javascript
// Request
POST /api/authn/login
{ "email": "admin@example.com", "password": "admin123" }

// Response
{
  "email": "admin@example.com",
  "token": "eyJhbGc...",
  "roles": ["ADMIN"]
}

// Save token
localStorage.setItem('dspace_token', 'eyJhbGc...');
```

**2. View Current User's Items**
```javascript
// Request
GET /api/items/assigned-to-me?page=0&limit=5
Authorization: Bearer eyJhbGc...

// Response
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "title": "Item 1",
      "handle": "123/001",
      "assignedTo": "admin@example.com",
      "lastModified": "2024-01-15T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

**3. Batch Assign Items**
```javascript
// Request
POST /api/admin/items/550e8400-e29b-41d4-a716-446655440000/assign-to-users
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "userEmails": ["user1@example.com", "user2@example.com"]
}

// Response
{
  "status": "success",
  "message": "Successfully assigned 50 items to 2 users",
  "itemsAssigned": 50,
  "usersAssigned": 2,
  "itemsPerUser": [25, 25]
}
```

## CORS Configuration

The React UI requires CORS headers from the backend. Ensure your DSpace server includes:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, DELETE, PUT, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 3600
```

## Rate Limiting

The backend may implement rate limiting on sensitive endpoints:
- Login endpoint: 5 requests per minute per IP
- Batch assignment: 10 requests per hour per user

Handle 429 (Too Many Requests) responses:
```typescript
if (response.status === 429) {
  const retryAfter = response.headers.get('Retry-After');
  console.log(`Rate limited. Retry after ${retryAfter} seconds`);
}
```

## Token Expiration

JWTs typically expire after 24 hours. Check token expiration:

```typescript
function isTokenExpired(token: string): boolean {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
}

// Check before each API call
if (isTokenExpired(token)) {
  localStorage.removeItem('dspace_token');
  // Redirect to login
}
```

## Testing with cURL

```bash
# Login
curl -X POST http://localhost:8080/server/api/authn/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Get assigned items
curl -X GET "http://localhost:8080/server/api/items/assigned-to-me?page=0&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Batch assign
curl -X POST "http://localhost:8080/server/api/admin/items/COLLECTION_ID/assign-to-users" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"userEmails":["user1@example.com","user2@example.com"]}'
```

## Production Considerations

- **HTTPS Only**: Always use HTTPS in production
- **Token Storage**: Consider using httpOnly cookies instead of localStorage
- **CORS**: Whitelist specific origins in production
- **Rate Limiting**: Implement stricter limits for batch operations
- **Logging**: Log all admin operations for audit trail
- **Validation**: Validate email addresses and collection IDs client-side before sending
