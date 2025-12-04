import { Component, OnInit, HostListener } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ThemeService } from './core/services/theme.service';
import { FeedService, Post } from './core/services/feed.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule, FormsModule, CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  activeTab = 'feed';
  feedType = 'universal';
  currentPage = 0;
  hasMorePosts = true;
  isLoading = false;
  isDarkTheme = false;
  isEditingProfile = false;
  profileName = 'Akram';
  profileUsername = 'akramShaik';
  newPostContent = '';
  posts: any[] = [
    {
      id: 1,
      author: 'Akram',
      content: 'This is a sample post content.',
      timestamp: '2 hours ago',
      likes: 5,
      comments: 2,
      shares: 1,
      liked: false,
      media: null,
      mediaType: '',
      commentsList: [
        { id: 1, author: 'Karthik', content: 'Great post!', timestamp: '1 hour ago' },
        { id: 2, author: 'Akram', content: 'Thanks!', timestamp: '30 min ago' }
      ]
    }
  ];
  
  selectedFile: File | null = null;
  selectedFileType = '';
  showComments: { [key: number]: boolean } = {};
  newComment = '';
  selectedPostId: number | null = null;
  replyingTo: { postId: string, commentId: number } | null = null;
  replyContent = '';
  postVisibility = 'public';
  
  selectedChat: string | null = null;
  newMessage = '';
  contacts = ['Karthik', 'Sai'];
  messages: { [key: string]: any[] } = {};

  constructor(private themeService: ThemeService, private feedService: FeedService) {}

  ngOnInit() {
    this.themeService.isDarkTheme$.subscribe(isDark => {
      this.isDarkTheme = isDark;
    });
    this.loadFeeds();
  }

  loadFeeds() {
    this.feedService.resetPagination();
    this.currentPage = 0;
    this.hasMorePosts = true;
    
    if (this.feedType === 'universal') {
      this.feedService.getGlobalFeed().subscribe(posts => {
        this.posts = posts;
      });
    } else {
      const followingNames = this.followingList.map(f => f.name);
      this.posts = this.feedService.getFollowingFeed(followingNames, 0);
    }
  }

  switchFeedType(type: string) {
    this.feedType = type;
    this.loadFeeds();
  }

  loadMorePosts() {
    if (this.isLoading || !this.hasMorePosts || this.feedType === 'universal') return;
    
    this.isLoading = true;
    const followingNames = this.followingList.map(f => f.name);
    const morePosts = this.feedService.loadMorePosts(followingNames);
    
    if (morePosts.length > 0) {
      this.posts = [...this.posts, ...morePosts];
    } else {
      this.hasMorePosts = false;
    }
    
    this.isLoading = false;
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'feed') {
      this.showSuggestions = true;
    }
  }

  toggleTheme() {
    this.themeService.toggleTheme();
  }

  editProfile() {
    this.isEditingProfile = true;
  }

  saveProfile() {
    this.isEditingProfile = false;
  }

  cancelEdit() {
    this.isEditingProfile = false;
  }

  createPost() {
    if (this.newPostContent.trim()) {
      this.checkForMentions(this.newPostContent);
      const newPost: Post = {
        id: Date.now().toString(),
        author: this.profileName,
        content: this.newPostContent,
        timestamp: 'Just now',
        likes: 0,
        comments: 0,
        shares: 0,
        liked: false,
        media: this.selectedFile ? URL.createObjectURL(this.selectedFile) : undefined,
        mediaType: this.selectedFileType as 'image' | 'video' | undefined,
        commentsList: [],
        visibility: this.postVisibility as 'public' | 'followers'
      };
      this.feedService.addPost(newPost);
      this.newPostContent = '';
      this.selectedFile = null;
      this.selectedFileType = '';
      this.postVisibility = 'public';
      this.setActiveTab('feed');
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      if (file.type.startsWith('image/')) {
        this.selectedFileType = 'image';
      } else if (file.type.startsWith('video/')) {
        this.selectedFileType = 'video';
      }
    }
  }

  likePost(post: any) {
    post.liked = !post.liked;
    post.likes += post.liked ? 1 : -1;
  }

  commentPost(post: any) {
    this.showComments[post.id] = !this.showComments[post.id];
    this.selectedPostId = this.showComments[post.id] ? post.id : null;
  }

  sharePost(post: any) {
    const shareData = {
      title: 'RevHub Post',
      text: `Check out this post by ${post.author}: ${post.content}`,
      url: window.location.href
    };

    if (navigator.share) {
      navigator.share(shareData).then(() => {
        post.shares += 1;
      }).catch((error) => {
        console.log('Error sharing:', error);
        this.fallbackShare(post);
      });
    } else {
      this.fallbackShare(post);
    }
  }

  fallbackShare(post: any) {
    const text = `Check out this post by ${post.author}: ${post.content}`;
    const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(text)}`;
    window.open(whatsappUrl, '_blank');
    post.shares += 1;
  }

  formatPostContent(content: string): string {
    return content
      .replace(/#(\w+)/g, '<span class="hashtag">#$1</span>')
      .replace(/@(\w+)/g, '<span class="mention">@$1</span>');
  }

  addComment(post: any) {
    if (this.newComment.trim()) {
      const comment = {
        id: Date.now(),
        author: this.profileName,
        content: this.newComment,
        timestamp: 'Just now'
      };
      post.commentsList.push(comment);
      post.comments = post.commentsList.length;
      this.newComment = '';
    }
  }

  deleteComment(post: any, commentId: number) {
    post.commentsList = post.commentsList.filter((c: any) => c.id !== commentId);
    post.comments = post.commentsList.length;
  }

  canDeleteComment(comment: any, post: any): boolean {
    return comment.author === this.profileName || post.author === this.profileName;
  }

  replyToComment(post: any, comment: any) {
    this.replyingTo = { postId: post.id, commentId: comment.id };
  }

  addReply(post: any, parentComment: any) {
    if (this.replyContent.trim()) {
      const reply = {
        id: Date.now(),
        author: this.profileName,
        content: this.replyContent,
        timestamp: 'Just now',
        isReply: true,
        parentId: parentComment.id
      };
      if (!parentComment.replies) {
        parentComment.replies = [];
      }
      parentComment.replies.push(reply);
      this.replyContent = '';
      this.replyingTo = null;
    }
  }

  cancelReply() {
    this.replyingTo = null;
    this.replyContent = '';
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.comment-section') && !target.closest('.comment-btn')) {
      this.showComments = {};
      this.selectedPostId = null;
    }
  }

  followUser(user: any) {
    this.followingList.push(user);
    this.suggestedUsers = this.suggestedUsers.filter(u => u.name !== user.name);
  }

  followFromList(user: any) {
    if (!this.followingList.some(f => f.name === user.name)) {
      this.followingList.push(user);
    }
  }

  unfollowUser(user: any) {
    this.followingList = this.followingList.filter(f => f.name !== user.name);
  }

  isFollowing(user: any): boolean {
    return this.followingList.some(f => f.name === user.name);
  }

  closeSuggestions() {
    this.showSuggestions = false;
  }

  selectChat(contact: string) {
    this.selectedChat = contact;
  }

  sendMessage() {
    if (this.newMessage.trim() && this.selectedChat) {
      if (!this.messages[this.selectedChat]) {
        this.messages[this.selectedChat] = [];
      }
      this.messages[this.selectedChat].push({
        sender: this.profileName,
        content: this.newMessage,
        timestamp: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
      });
      this.newMessage = '';
    }
  }

  backToContacts() {
    this.selectedChat = null;
  }

  get userPosts() {
    return this.posts.filter(post => post.author === this.profileName);
  }

  searchQuery = '';
  searchType = 'users';
  allUsers = ['Akram', 'Karthik', 'Sai', 'Priya', 'Arjun', 'Rohit'];
  showFollowersList = false;
  showFollowingList = false;
  followersList = [
    { name: 'Karthik', username: 'karthik_dev', followers: 245 },
    { name: 'Sai', username: 'sai_tech', followers: 189 },
    { name: 'Priya', username: 'priya_design', followers: 567 },
    { name: 'Arjun', username: 'arjun_photo', followers: 123 },
    { name: 'Ananya', username: 'ananya_art', followers: 345 },
    { name: 'Rohit', username: 'rohit_code', followers: 456 },
    { name: 'Kavya', username: 'kavya_write', followers: 234 }
  ];
  followingList = [
    { name: 'Karthik', username: 'karthik_dev', followers: 245 },
    { name: 'Sai', username: 'sai_tech', followers: 189 },
    { name: 'Priya', username: 'priya_design', followers: 567 },
    { name: 'Rohit', username: 'rohit_code', followers: 456 },
    { name: 'Neha', username: 'neha_music', followers: 678 }
  ];
  suggestedUsers = [
    { name: 'Vikram', username: 'vikram_dev', bio: 'Full Stack Developer', followers: 432 },
    { name: 'Shreya', username: 'shreya_design', bio: 'UI/UX Designer', followers: 298 },
    { name: 'Aditya', username: 'aditya_tech', bio: 'Tech Enthusiast', followers: 156 }
  ];
  showSuggestions = true;
  notifications = [
    {
      id: 1,
      type: 'follow',
      message: 'Priya started following you',
      timestamp: '2 hours ago',
      read: false
    },
    {
      id: 2,
      type: 'like',
      message: 'Rohit liked your post',
      timestamp: '5 hours ago',
      read: false
    },
    {
      id: 3,
      type: 'mention',
      message: 'Karthik mentioned you in a post',
      timestamp: '1 day ago',
      read: true
    }
  ];
  
  get followersCount() {
    return this.followersList.length;
  }
  
  get followingCount() {
    return this.followingList.length;
  }
  
  get filteredUsers() {
    if (!this.searchQuery.trim() || this.searchType !== 'users') return [];
    return this.allUsers.filter(user => 
      user.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  get filteredPosts() {
    if (!this.searchQuery.trim() || this.searchType !== 'posts') return [];
    const query = this.searchQuery.toLowerCase();
    return this.posts.filter(post => 
      post.content.toLowerCase().includes(query) ||
      post.content.toLowerCase().includes('#' + query) ||
      post.author.toLowerCase().includes(query)
    );
  }

  switchSearchType(type: string) {
    this.searchType = type;
  }

  deletePost(post: any) {
    this.posts = this.posts.filter(p => p.id !== post.id);
    this.feedService.deletePost(post.id);
  }

  canDeletePost(post: any): boolean {
    return post.author === this.profileName;
  }

  editingPost: any = null;
  editPostContent = '';

  editPost(post: any) {
    this.editingPost = post;
    this.editPostContent = post.content;
  }

  saveEditPost() {
    if (this.editPostContent.trim() && this.editingPost) {
      this.editingPost.content = this.editPostContent;
      this.feedService.updatePost(this.editingPost);
      this.cancelEditPost();
    }
  }

  cancelEditPost() {
    this.editingPost = null;
    this.editPostContent = '';
  }

  get postsCount() {
    return this.userPosts.length;
  }

  showFollowers() {
    this.showFollowersList = true;
    this.showFollowingList = false;
  }

  showFollowing() {
    this.showFollowingList = true;
    this.showFollowersList = false;
  }

  hideUserLists() {
    this.showFollowersList = false;
    this.showFollowingList = false;
  }

  checkForMentions(content: string) {
    const mentionRegex = /@(\w+)/g;
    const mentions = content.match(mentionRegex);
    if (mentions) {
      mentions.forEach(mention => {
        const username = mention.substring(1);
        this.addMentionNotification(username);
      });
    }
  }

  addMentionNotification(username: string) {
    const notification = {
      id: Date.now(),
      type: 'mention',
      message: `${this.profileName} mentioned you in a post`,
      timestamp: 'Just now',
      read: false
    };
    this.notifications.unshift(notification);
  }
}