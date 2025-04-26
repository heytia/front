<template>
  <div class="review-item">
    <!-- 評價頭部信息 -->
    <div class="review-header">
      <div class="user-info">
        <div class="user-avatar">{{ review.userName ? review.userName.charAt(0) : 'U' }}</div>
        <div>
          <h5 class="user-name">{{ review.userName || '匿名用戶' }}</h5>
          <p class="review-date">{{ formatDate(review.createdAt) }}</p>
        </div>
      </div>
      
      <div class="review-rating">
        <span v-for="i in 5" :key="i" class="star" :class="i <= review.overallRating ? 'filled' : 'empty'">★</span>
        <span class="rating-value">{{ review.overallRating }}</span>
      </div>
    </div>
    
    <!-- 評價內容 -->
    <div class="review-content">
      <p>{{ review.reviewText }}</p>
    </div>
    
    <!-- 評價圖片 -->
    <div class="review-images" v-if="review.imageUrls && review.imageUrls.length > 0">
      <div v-for="(url, index) in review.imageUrls.slice(0, 3)" :key="index" class="image-item">
        <div class="image-wrapper">
          <img 
            :src="processImageUrl(url)" 
            :alt="`評價圖片 ${index + 1}`" 
            @click="viewImage(index)"
            @load="handleImageLoaded(index)"
            @error="(event) => handleImageError(event, index)"
            class="review-image"
          />
          <div class="image-loading" v-if="imageLoading[index]">
            <div class="spinner-border spinner-sm text-secondary" role="status">
              <span class="visually-hidden">載入中...</span>
            </div>
          </div>
          <div class="image-overlay" @click.stop="viewImage(index)">
            <i class="bi bi-zoom-in"></i>
          </div>
        </div>
      </div>
      <div v-if="review.imageUrls.length > 3" class="more-images" @click="viewImage(3)">
        +{{ review.imageUrls.length - 3 }}
      </div>
    </div>
    
    <!-- 營地回覆 -->
    <div class="reply-section" v-if="review.replyText">
      <h4>營地方回覆：</h4>
      <p>{{ review.replyText }}</p>
    </div>
    
    <!-- 評價操作按鈕 -->
    <div class="review-actions">
      <!-- 作者可見的編輯和刪除按鈕 -->
      <div class="author-actions" v-if="isAuthor">
        <button class="action-btn edit-btn" @click="$emit('edit', review.id)">編輯</button>
        <button class="action-btn delete-btn" @click="$emit('delete', review.id)">刪除</button>
      </div>
      
      <!-- 所有人可見的檢舉按鈕 -->
      <button class="action-btn report-btn" @click="$emit('report', review.id)">檢舉</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  review: {
    type: Object,
    required: true
  },
  isAuthor: {
    type: Boolean,
    default: false
  },
  currentUserId: {
    type: [Number, String],
    default: null
  }
});

const emit = defineEmits(['edit', 'delete', 'report', 'view-image']);

// 處理圖片載入狀態
const imageLoading = ref({});

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
  } catch (e) {
    return dateString;
  }
};

// 處理圖片 URL，確保可以正確顯示
const processImageUrl = (url) => {
  if (!url) return '';
  
  // 如果已經是完整 URL（包含 http 或 https）
  if (url.startsWith('http')) {
    return url;
  }
  
  // 如果是相對路徑，需要確保路徑正確
  return url.startsWith('/') ? url : `/${url}`;
};

// 處理圖片載入開始
const handleImageLoadStart = (index) => {
  imageLoading.value[index] = true;
};

// 處理圖片載入完成
const handleImageLoaded = (index) => {
  imageLoading.value[index] = false;
};

// 處理圖片載入錯誤
const handleImageError = (event, index) => {
  const url = props.review.imageUrls?.[index];
  console.error(`圖片載入失敗: ${url}`);
  console.error(`處理後的URL: ${processImageUrl(url)}`);
  event.target.src = '/img/image-placeholder.jpg';
  imageLoading.value[index] = false;
};

// 查看大圖
const viewImage = (index) => {
  if (!props.review.imageUrls || props.review.imageUrls.length === 0) return;
  emit('view-image', props.review.imageUrls, index);
};
</script>

<style scoped>
.review-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  position: relative;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #2A5D3C;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 10px;
}

.user-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.review-date {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.review-rating {
  display: flex;
  align-items: center;
}

.star {
  color: #FFCB45;
  font-size: 18px;
  margin-right: 2px;
}

.star.filled {
  opacity: 1;
}

.star.empty {
  opacity: 0.3;
}

.rating-value {
  margin-left: 5px;
  font-weight: bold;
  color: #333;
}

.review-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.image-wrapper {
  width: 120px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  background-color: #f8f8f8;
}

.review-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-wrapper:hover .review-image {
  transform: scale(1.05);
}

.image-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0,0,0,0.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-overlay i {
  font-size: 24px;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.more-images {
  min-width: 50px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  color: #666;
}

.more-images:hover {
  background-color: #e0e0e0;
}

.reply-section {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
  border-left: 3px solid #2A5D3C;
}

.reply-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #2A5D3C;
  font-size: 16px;
}

.reply-section p {
  margin: 0;
}

.review-actions {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 15px;
}

.author-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  font-size: 14px;
  transition: all 0.2s;
}

.edit-btn {
  background-color: #4CAF50;
  color: white;
}

.edit-btn:hover {
  background-color: #45a049;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.report-btn {
  background-color: #ff9800;
  color: white;
}

.report-btn:hover {
  background-color: #e68a00;
}

.spinner-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.15em;
}
</style>