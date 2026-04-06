# DSpace 9 - Item Assignment Feature Deployment Guide

## Files Added

### Service Layer
```
dspace-api/src/main/java/org/dspace/content/service/AssignedItemService.java
dspace-api/src/main/java/org/dspace/content/AssignedItemServiceImpl.java
```

### REST Controllers
```
dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignedItemsRestController.java
dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignedItemsAdminController.java
```

### Configuration
```
dspace-api/src/main/java/org/dspace/config/AssignedItemsConfiguration.java
```

### Utilities
```
dspace-api/src/main/java/org/dspace/app/util/AssignedItemIndexingUtil.java
```

### Database Migration
```
dspace-api/src/main/resources/org/dspace/storage/rdbms/sqlmigration/postgres/V9_0_2025_01_01__assigned_items_metadata.sql
```

### Tests
```
dspace-api/src/test/java/org/dspace/content/AssignedItemServiceIT.java
```

---

## Installation Steps

### 1. Copy Files to Your DSpace Installation

```bash
# Copy service files
cp dspace-api/src/main/java/org/dspace/content/service/AssignedItemService.java \
   {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/content/service/

cp dspace-api/src/main/java/org/dspace/content/AssignedItemServiceImpl.java \
   {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/content/

# Copy REST controllers
cp dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignedItemsRestController.java \
   {YOUR_DSPACE}/dspace-server-webapp/src/main/java/org/dspace/app/rest/

cp dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignedItemsAdminController.java \
   {YOUR_DSPACE}/dspace-server-webapp/src/main/java/org/dspace/app/rest/

# Copy configuration
cp dspace-api/src/main/java/org/dspace/config/AssignedItemsConfiguration.java \
   {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/config/

# Copy utility
cp dspace-api/src/main/java/org/dspace/app/util/AssignedItemIndexingUtil.java \
   {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/app/util/

# Copy database migration
cp dspace-api/src/main/resources/org/dspace/storage/rdbms/sqlmigration/postgres/V9_0_2025_01_01__assigned_items_metadata.sql \
   {YOUR_DSPACE}/dspace-api/src/main/resources/org/dspace/storage/rdbms/sqlmigration/postgres/

# Copy tests
cp dspace-api/src/test/java/org/dspace/content/AssignedItemServiceIT.java \
   {YOUR_DSPACE}/dspace-api/src/test/java/org/dspace/content/
```

### 2. Build DSpace

```bash
cd {YOUR_DSPACE}
mvn clean install -DskipTests
```

Or if you want to run tests including the new integration tests:

```bash
mvn clean install -Dskip.migreation
```

### 3. Apply Database Migration

The Liquibase migration will run automatically during startup, or you can manually apply it:

```bash
cd dspace
mvn liquibase:update
```

### 4. Verify Metadata Field Created

Check that the metadata field was created:

```bash
dspace metadata-field -l | grep "assigned"
```

Expected output:
```
...
dublin_core | assigned | user | Email of user to whom this item is assigned
...
```

### 5. Restart DSpace

```bash
cd dspace
./bin/dspace stop
./bin/dspace start
```

Or if using Docker:

```bash
docker-compose down
docker-compose up -d
```

---

## Verification

### 1. Check REST Endpoints

```bash
# Check if the controller is loaded
curl -X GET "http://localhost:8080/api/items/assigned-to-me" \
  -H "Authorization: Bearer {token}"

# Expected response: 200 OK with items or empty list
```

### 2. Check Service in Spring Context

```bash
# Check Spring logs for component registration
tail -f dspace/log/dspace.log | grep "AssignedItem"

# Should see:
# "AssignedItemService bean loaded"
# "AssignedItemsRestController registered"
# "AssignedItemsAdminController registered"
```

### 3. Test Batch Assignment

```bash
curl -X POST "http://localhost:8080/api/admin/items/{collectionId}/assign-to-users" \
  -H "Authorization: Bearer {admin-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "userEmails": ["user1@test.com", "user2@test.com"]
  }'
```

---

## Configuration Options

Add to `dspace/config/local.cfg` if needed:

```properties
# Logging level for assigned items feature
log.module.org.dspace.content.AssignedItemServiceImpl = DEBUG
log.module.org.dspace.app.rest.AssignedItemsRestController = DEBUG

# Discovery/Solr configuration (usually automatic)
discovery.index.dc.assigned.user = true
```

---

## Troubleshooting

### Issue: REST Endpoints Not Found (404)

**Solution**: 
1. Verify controllers are in correct package: `org.dspace.app.rest`
2. Check Spring is auto-scanning: Add `@ComponentScan` if needed
3. Rebuild and restart

### Issue: Metadata Field Not Found

**Solution**:
1. Run migration: `mvn liquibase:update`
2. Verify in database:
   ```sql
   SELECT * FROM metadatafieldregistry 
   WHERE element = 'assigned' AND qualifier = 'user';
   ```

### Issue: Authorization Errors

**Solution**:
1. Check token is valid: `curl http://localhost:8080/api/authn/status`
2. For admin endpoints, verify user has ADMIN role
3. Check security configuration in Spring

### Issue: Items Not Appearing in Search

**Solution**:
1. Reindex Discovery: `dspace index-discovery -b`
2. Verify Solr is running: `curl http://localhost:8983/solr/`
3. Check logs for indexing errors

---

## Rollback (If Needed)

### 1. Stop DSpace

```bash
dspace/bin/dspace stop
```

### 2. Revert Database

```bash
# Revert the migration (Liquibase will handle this)
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

### 3. Remove Files

```bash
rm {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/content/AssignedItemService*
rm {YOUR_DSPACE}/dspace-server-webapp/src/main/java/org/dspace/app/rest/AssignedItems*
rm {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/config/AssignedItemsConfiguration.java
rm {YOUR_DSPACE}/dspace-api/src/main/java/org/dspace/app/util/AssignedItemIndexingUtil.java
```

### 4. Rebuild and Restart

```bash
mvn clean install -DskipTests
dspace/bin/dspace start
```

---

## Performance Tuning

### 1. Database Indexes

For large deployments, consider adding database indexes:

```sql
-- Add index on assigned user metadata
CREATE INDEX idx_meta_assigned_user ON metadatavalue(resource_id, resource_type_id)
WHERE metadata_field_id = (
  SELECT metadata_field_id FROM metadatafieldregistry
  WHERE element = 'assigned' AND qualifier = 'user'
);
```

### 2. Solr Configuration

Ensure dc.assigned.user is properly indexed in `solrconfig.xml`:

```xml
<field name="dc.assigned.user" type="string" indexed="true" stored="true"/>
```

### 3. Pagination

Always use pagination for large result sets:

```bash
# Good
curl "http://localhost:8080/api/items/assigned-to-me?page=0&size=50"

# Avoid loading all results at once
curl "http://localhost:8080/api/items/assigned-to-me"
```

---

## Monitoring

### Check Health

```bash
# Monitor logs
tail -f dspace/log/dspace.log

# Monitor database
# Monitor Solr indexing
# Monitor REST API response times
```

### Metrics to Track

- Average response time for `/api/items/assigned-to-me`
- Total assigned items count
- Batch assignment performance (items/second)
- Database query times

---

## Support

For issues or questions:
1. Check logs in `dspace/log/dspace.log`
2. Review this guide
3. Consult DSpace community: https://groups.google.com/a/dspace.org/g/dspace-community
4. Create GitHub issue: https://github.com/DSpace/DSpace/issues

---

## Version Compatibility

- **DSpace**: 9.0+
- **Java**: 11+
- **Spring Boot**: 2.7+
- **Database**: PostgreSQL 11+, MySQL 8.0+

---

## Change Log

### Version 1.0 (2025-01-01)

- Initial release
- Implemented AssignedItemService
- Created REST endpoints
- Added batch assignment functionality
- Full integration tests
- Comprehensive documentation
