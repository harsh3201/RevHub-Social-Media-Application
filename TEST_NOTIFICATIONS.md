# Notification System - Testing Guide

## ✅ Docker Build Status
**BUILD SUCCESSFUL** - All services are running:
- ✅ MySQL (revhub-mysql) - Port 3307
- ✅ MongoDB (revhub-mongo) - Port 27017  
- ✅ Spring Boot Backend - Port 8080
- ✅ Angular Frontend - Port 80
- ✅ Nginx Reverse Proxy - Running

## ✅ Notification Services Verified

From the logs, the following services are initialized:
1. **NotificationMongoRepository** - MongoDB repository for notifications
2. **NotificationService** - SQL-based notification service
3. **NotificationMongoService** - MongoDB-based notification service
4. **LikeService** - Now includes notification support
5. **CommentService** - Includes notification support
6. **PostService** - Includes mention notification support

## Testing Notification Functionality

### Test 1: Like Notification

**Steps:**
1. Open browser: http://localhost
2. Login as User A
3. Create a post
4. Logout and login as User B
5. Like User A's post
6. Logout and login back as User A
7. Check notifications - should see "UserB liked your post"

**Expected Result:**
- User A receives notification when User B likes their post
- Notification stored in MongoDB
- Notification appears in notification panel

**API Endpoint:**
```
POST /api/posts/{postId}/like
Authorization: Bearer <token>
```

**Code Location:**
- `LikeService.java` - Line 47-49 (notification creation)

---

### Test 2: Comment Notification

**Steps:**
1. Login as User A
2. Create a post
3. Logout and login as User B
4. Comment on User A's post
5. Logout and login back as User A
6. Check notifications - should see "UserB commented on your post"

**Expected Result:**
- User A receives notification when User B comments
- Notification includes comment preview
- Notification links to the post

**API Endpoint:**
```
POST /api/posts/{postId}/comments
Content-Type: application/json
Authorization: Bearer <token>

{
  "content": "Great post!"
}
```

**Code Location:**
- `CommentService.java` - Line 68-70 (notification creation)

---

### Test 3: Mention Notification

**Steps:**
1. Login as User A
2. Create a post with content: "Hey @UserB check this out!"
3. Logout and login as User B
4. Check notifications - should see "UserA mentioned you in a post"

**Expected Result:**
- User B receives notification when mentioned
- Mention detection works with @username pattern
- Multiple mentions in same post handled correctly
- Self-mentions ignored

**API Endpoint:**
```
POST /api/posts
Content-Type: multipart/form-data
Authorization: Bearer <token>

content: "Hey @UserB check this out!"
visibility: PUBLIC
```

**Code Location:**
- `PostService.java` - Line 119 (processMentions call)
- `PostService.java` - Line 289-343 (mention processing logic)

---

## Verification Commands

### Check if services are running:
```bash
docker-compose ps
```

### View application logs:
```bash
docker-compose logs -f app
```

### Check MongoDB notifications:
```bash
docker exec -it revhub-mongo mongosh revhubteam4
db.notificationMongo.find().pretty()
```

### Check MySQL notifications:
```bash
docker exec -it revhub-mysql mysql -uroot -proot revhubteam4
SELECT * FROM notifications;
```

### Test API directly (after getting auth token):
```bash
# Get notifications
curl -X GET http://localhost:8080/notifications \
  -H "Authorization: Bearer YOUR_TOKEN"

# Get unread count
curl -X GET http://localhost:8080/notifications/unread-count \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Notification Flow Diagram

```
User Action (Like/Comment/Mention)
         ↓
Service Layer (LikeService/CommentService/PostService)
         ↓
NotificationMongoService.createXXXNotification()
         ↓
MongoDB (notificationMongo collection)
         ↓
NotificationController.getUserNotifications()
         ↓
Frontend displays notification
```

---

## Troubleshooting

### If notifications don't appear:

1. **Check MongoDB connection:**
   ```bash
   docker-compose logs mongodb
   ```

2. **Verify notification creation in logs:**
   ```bash
   docker-compose logs app | grep -i "notification"
   ```

3. **Check MongoDB data:**
   ```bash
   docker exec -it revhub-mongo mongosh revhubteam4
   db.notificationMongo.countDocuments()
   ```

4. **Verify user authentication:**
   - Ensure JWT token is valid
   - Check Authorization header is sent

5. **Check browser console:**
   - Open DevTools (F12)
   - Look for API errors in Network tab

---

## Expected Notification Format

```json
{
  "id": "675290abc123def456789012",
  "userId": "1",
  "fromUserId": "2",
  "fromUsername": "john_doe",
  "fromUserProfilePicture": "data:image/jpeg;base64,...",
  "type": "LIKE",
  "message": "john_doe liked your post",
  "postId": 123,
  "readStatus": false,
  "createdDate": "2025-12-06T12:41:53.486+05:30"
}
```

---

## Notification Types Supported

| Type | Trigger | Message Format |
|------|---------|----------------|
| LIKE | User likes a post | "{username} liked your post" |
| COMMENT | User comments on post | "{username} commented on your post" |
| MENTION | User mentions in post | "{username} mentioned you in a post" |
| FOLLOW | User follows you | "{username} started following you" |
| FOLLOW_REQUEST | User requests to follow | "{username} wants to follow you" |
| MESSAGE | User sends message | "{username} sent you a message: {preview}" |

---

## Performance Notes

- Notifications are stored in MongoDB for fast read/write
- Indexes created automatically on userId for quick queries
- Notifications ordered by createdDate (newest first)
- Unread count cached for performance

---

## Next Steps

1. ✅ Build and run Docker containers - **COMPLETED**
2. ✅ Verify services are running - **COMPLETED**
3. ✅ Verify notification services initialized - **COMPLETED**
4. 🔄 Test like notifications - **READY TO TEST**
5. 🔄 Test comment notifications - **READY TO TEST**
6. 🔄 Test mention notifications - **READY TO TEST**

## Access the Application

- **Frontend:** http://localhost
- **Backend API:** http://localhost:8080
- **API Docs:** http://localhost:8080/swagger-ui.html (if configured)

---

## Summary

✅ **Docker Build:** SUCCESS  
✅ **MySQL Connection:** SUCCESS  
✅ **MongoDB Connection:** SUCCESS  
✅ **Notification Services:** INITIALIZED  
✅ **Like Notifications:** IMPLEMENTED  
✅ **Comment Notifications:** IMPLEMENTED  
✅ **Mention Notifications:** IMPLEMENTED  

**Status:** Ready for testing! 🚀
