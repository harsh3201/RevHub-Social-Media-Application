# Notification System Implementation

## Overview
Your RevHub application now has a complete notification system that alerts users when:
1. Someone **likes** their post
2. Someone **comments** on their post
3. Someone **mentions** them in a post (using @username)

## Implementation Details

### 1. Like Notifications ✓
**File Updated:** `LikeService.java`
- When a user likes a post, the post owner receives a notification
- Self-likes are ignored (no notification if you like your own post)
- Notification is created via `NotificationMongoService.createLikeNotification()`

### 2. Comment Notifications ✓
**File:** `CommentService.java` (Already implemented)
- When a user comments on a post, the post owner receives a notification
- Self-comments are ignored
- Notification is created via `NotificationMongoService.createCommentNotification()`

### 3. Mention Notifications ✓
**File:** `PostService.java` (Already implemented)
- When a user mentions another user with @username in a post, the mentioned user receives a notification
- Uses regex pattern to detect @username mentions
- Self-mentions are ignored
- Duplicate mentions in the same post are handled (only one notification per user)
- Notifications are created in both MongoDB and SQL databases

## Notification Types
The system supports the following notification types:
- `LIKE` - Someone liked your post
- `COMMENT` - Someone commented on your post
- `MENTION` - Someone mentioned you in a post
- `FOLLOW` - Someone started following you
- `FOLLOW_REQUEST` - Someone wants to follow you
- `MESSAGE` - Someone sent you a message

## How It Works

### Like Flow:
1. User clicks like on a post
2. `LikeService.toggleLike()` is called
3. If it's a new like (not unlike):
   - Like is saved to database
   - Post likes count is incremented
   - If liker ≠ post owner → notification is created

### Comment Flow:
1. User adds a comment to a post
2. `CommentService.addComment()` is called
3. Comment is saved to database
4. Post comments count is incremented
5. If commenter ≠ post owner → notification is created

### Mention Flow:
1. User creates a post with @username
2. `PostService.createPost()` is called
3. Post is saved to database
4. `processMentions()` scans content for @username patterns
5. For each valid username found:
   - User is looked up in database
   - If user exists and ≠ post author → notification is created

## Database Storage
Notifications are stored in **MongoDB** via the `NotificationMongo` entity with fields:
- `userId` - Who receives the notification
- `fromUserId` - Who triggered the notification
- `fromUsername` - Username of the trigger user
- `type` - Type of notification (LIKE, COMMENT, MENTION, etc.)
- `message` - Human-readable notification message
- `postId` - Related post ID (if applicable)
- `readStatus` - Whether the notification has been read
- `createdDate` - When the notification was created

## API Endpoints
Your application should have these notification endpoints (via `NotificationController`):
- `GET /api/notifications` - Get all notifications for current user
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `GET /api/notifications/unread-count` - Get count of unread notifications
- `DELETE /api/notifications/{id}` - Delete a notification

## Testing
To test the notifications:

1. **Like Notification:**
   - User A creates a post
   - User B likes the post
   - User A should receive a notification: "UserB liked your post"

2. **Comment Notification:**
   - User A creates a post
   - User B comments on the post
   - User A should receive a notification: "UserB commented on your post"

3. **Mention Notification:**
   - User A creates a post with content: "Hey @UserB check this out!"
   - User B should receive a notification: "UserA mentioned you in a post"

## Notes
- Notifications are only created when the action is performed by a different user (no self-notifications)
- The system uses MongoDB for notification storage for better scalability
- Notifications include profile pictures and usernames for rich UI display
- All notifications are timestamped for chronological ordering
