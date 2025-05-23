package com.example.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "order_id")
    private Integer orderId;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "camp_site_id")
    private Integer campSiteId;
    
    @Column(name = "coupons_id")
    private Integer couponsId;
    
    @Column(name = "review_text", columnDefinition = "text")
    private String reviewText;
    
    @Column(name = "review_is_visible", columnDefinition = "bit")
    private Boolean reviewIsVisible;
    
    @Column(name = "reply_text", columnDefinition = "text")
    private String replyText;
    
    @Column(name = "reply_is_visible", columnDefinition = "bit")
    private Boolean replyIsVisible;
    
    @Column(name = "overall_rating")
    private Integer overallRating;
    
    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;
    
    @Column(name = "convenience_rating")
    private Integer convenienceRating;
    
    @Column(name = "friendliness_rating")
    private Integer friendlinessRating;
    
    @Column(name = "pros", columnDefinition = "text")
    private String pros;
    
    @Column(name = "cons", columnDefinition = "text")
    private String cons;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Transient
    private List<String> imageUrls;
    
    @Transient
    private Long likeCount;
    
    @Transient
    private Integer userLikeStatus;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getCampSiteId() {
        return campSiteId;
    }
    
    public void setCampSiteId(Integer campSiteId) {
        this.campSiteId = campSiteId;
    }
    
    public Integer getCouponsId() {
        return couponsId;
    }
    
    public void setCouponsId(Integer couponsId) {
        this.couponsId = couponsId;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    
    public Boolean getReviewIsVisible() {
        return reviewIsVisible;
    }
    
    public void setReviewIsVisible(Boolean reviewIsVisible) {
        this.reviewIsVisible = reviewIsVisible;
    }
    
    public String getReplyText() {
        return replyText;
    }
    
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
    
    public Boolean getReplyIsVisible() {
        return replyIsVisible;
    }
    
    public void setReplyIsVisible(Boolean replyIsVisible) {
        this.replyIsVisible = replyIsVisible;
    }
    
    public Integer getOverallRating() {
        return overallRating;
    }
    
    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }
    
    public Integer getCleanlinessRating() {
        return cleanlinessRating;
    }
    
    public void setCleanlinessRating(Integer cleanlinessRating) {
        this.cleanlinessRating = cleanlinessRating;
    }
    
    public Integer getConvenienceRating() {
        return convenienceRating;
    }
    
    public void setConvenienceRating(Integer convenienceRating) {
        this.convenienceRating = convenienceRating;
    }
    
    public Integer getFriendlinessRating() {
        return friendlinessRating;
    }
    
    public void setFriendlinessRating(Integer friendlinessRating) {
        this.friendlinessRating = friendlinessRating;
    }
    
    public String getPros() {
        return pros;
    }
    
    public void setPros(String pros) {
        this.pros = pros;
    }
    
    public String getCons() {
        return cons;
    }
    
    public void setCons(String cons) {
        this.cons = cons;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getUserLikeStatus() {
        return userLikeStatus;
    }
    
    public void setUserLikeStatus(Integer userLikeStatus) {
        this.userLikeStatus = userLikeStatus;
    }
}