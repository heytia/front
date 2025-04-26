<!-- src/components/ReviewList.vue -->
<template>
  <div class="review-list-container">
    <!-- 載入中 -->
    <div v-if="loading" class="loading-state text-center py-5">
      <div class="spinner-border text-danger" role="status">
        <span class="visually-hidden">載入中...</span>
      </div>
      <p class="mt-2 text-forest">評價載入中，請稍候...</p>
    </div>

    <!-- 錯誤訊息 -->
    <div v-else-if="error" class="alert-box alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      <div>{{ error }}</div>
      <div class="mt-2">
        <button @click="retryConnection" class="btn btn-outline-primary btn-sm ms-2">
          <i class="bi bi-arrow-clockwise"></i> 重試連接
        </button>
      </div>
    </div>

    <!-- 無評價提示 -->
    <div v-else-if="!loading && !error && reviews.length === 0" class="empty-state text-center">
      <i class="bi bi-chat-square-text empty-icon"></i>
      <h3 class="text-forest">暫無評價</h3>
      <p class="text-forest">成為第一個評價此營地的人吧！</p>
    </div>

    <!-- 評價列表 -->
    <div v-else>
      <h2 class="section-title text-forest">
        <i class="bi bi-list-stars me-2"></i>
        評價列表 <span class="count">({{ totalElements || reviews.length }})</span>
      </h2>

      <div class="review-list-vertical">
        <div v-for="review in reviews" :key="review.id" class="review-card-wrapper mb-4">
          <!-- 當評價被檢舉時顯示提示訊息 -->
          <div v-if="review.isReported" class="reported-item">
            <i class="bi bi-shield-exclamation me-2"></i>
            此評價已被檢舉，正在審核中
          </div>
          <!-- 否則顯示正常評價 -->
          <div v-else class="review-item">
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-start review-header">
              <div class="d-flex align-items-center">
                <div>
                  <span class="camp-region text-forest">{{ getCampRegion(review.campSiteId) }}</span>
                  <a :href="`/campsites/${review.campSiteId}`" class="camp-name text-forest ms-1">{{ getCampName(review.campSiteId) }}</a>
                </div>
              </div>
              <div class="star-rating overall-rating">
                <span v-for="i in 5" :key="i" :class="['star', i <= review.overallRating ? 'star-filled' : 'star-empty']">★</span>
              </div>
            </div>

            <!-- User Info -->
            <div class="d-flex justify-content-between align-items-center mt-3 user-info">
              <div>
                <h3 class="user-name mb-1 text-forest">{{ review.userName || '匿名用戶' }}</h3>
                <p class="review-date mb-0 text-forest-light">
                  {{ formatDate(review.createdAt) }}
                  <span v-if="review.updatedAt && new Date(review.updatedAt).getTime() > new Date(review.createdAt).getTime() + 60000" class="edited-tag">
                    (已編輯)
                  </span>
                </p>
              </div>
              
              <!-- 管理操作按鈕區 (顯示在右上角) -->
              <div class="admin-actions">
                <!-- 編輯按鈕 -->
                <button 
                  type="button"
                  class="admin-btn edit-btn" 
                  @click.stop="handleEditReview(review)"
                  title="編輯評價"
                >
                  <i class="bi bi-pencil-fill"></i> 編輯
                </button>
                
                <!-- 刪除按鈕 -->
                <button 
                  type="button"
                  class="admin-btn delete-btn" 
                  @click.stop="handleDeleteReview(review)"
                  title="刪除評價"
                >
                  <i class="bi bi-trash"></i> 刪除
                </button>
              </div>
            </div>

            <!-- Content -->
            <p class="review-text mt-3 text-forest">{{ review.reviewText }}</p>

            <!-- 評價圖片 -->
            <div class="review-images" v-if="review.imageUrls && review.imageUrls.length > 0">
              <div v-for="(url, index) in review.imageUrls.slice(0, 3)" :key="index" class="image-item">
                <div class="image-wrapper">
                  <img 
                    :src="processImageUrl(url)" 
                    :alt="`評價圖片 ${index + 1}`" 
                    @click="viewImage(review.id, index)"
                    @load="handleImageLoaded(review.id, index)"
                    @error="(event) => handleImageError(event, review.id, index)"
                    class="review-image"
                  />
                  <div class="image-loading" v-if="imageLoading[review.id]?.[index]">
                    <div class="spinner-border spinner-sm text-secondary" role="status">
                      <span class="visually-hidden">載入中...</span>
                    </div>
                  </div>
                  <div class="image-overlay" @click.stop="viewImage(review.id, index)">
                    <i class="bi bi-zoom-in"></i>
                  </div>
                </div>
              </div>
              <div v-if="review.imageUrls.length > 3" class="more-images" @click="viewImage(review.id, 3)">
                +{{ review.imageUrls.length - 3 }}
              </div>
            </div>

            <!-- Pros & Cons -->
            <div class="pros-cons mt-3" v-if="review.pros || review.cons">
              <div v-if="review.pros" class="pros">
                <span class="label"><i class="bi bi-hand-thumbs-up-fill me-1"></i>優點：</span>
                <span class="value text-forest">{{ review.pros }}</span>
              </div>
              <div v-if="review.cons" class="cons">
                <span class="label"><i class="bi bi-hand-thumbs-down-fill me-1"></i>缺點：</span>
                <span class="value text-forest">{{ review.cons }}</span>
              </div>
            </div>

            <!-- Ratings Detail -->
            <div class="rating-details">
              <div class="rating-item">
                <span class="rating-label text-forest">清潔程度</span>
                <span class="star-rating">
                  <span v-for="i in 5" :key="i" :class="['star', i <= review.cleanlinessRating ? 'star-filled' : 'star-empty']">★</span>
                </span>
              </div>
              <div class="rating-item">
                <span class="rating-label text-forest">便利程度</span>
                <span class="star-rating">
                  <span v-for="i in 5" :key="i" :class="['star', i <= review.convenienceRating ? 'star-filled' : 'star-empty']">★</span>
                </span>
              </div>
              <div class="rating-item">
                <span class="rating-label text-forest">友善程度</span>
                <span class="star-rating">
                  <span v-for="i in 5" :key="i" :class="['star', i <= review.friendlinessRating ? 'star-filled' : 'star-empty']">★</span>
                </span>
              </div>
            </div>

            <div v-if="review.replyText && review.replyText.trim()" class="reply-section">
              <!-- 如果回覆被檢舉，顯示被檢舉提示 -->
              <div v-if="review.isReplyReported" class="reported-reply">
                <i class="bi bi-shield-exclamation me-2"></i>
                此回覆已被檢舉，正在審核中
              </div>
              <!-- 否則顯示正常回覆 -->
              <div v-else>
                <div class="reply-header-container">
                  <h6 class="reply-header text-forest">
                    <i class="bi bi-chat-left-text me-2"></i>營地方回覆：
                  </h6>

                  <!-- 僅在是營地主人且有回覆時顯示 -->
                  <div class="reply-actions" v-if="isOwner && review.replyText && review.replyText.trim()">
                  </div>
                </div>

                <p class="reply-content text-forest">{{ review.replyText }}</p>
              </div>

              <!-- 回覆檢舉按鈕 -->
              <div class="d-flex justify-content-end align-items-center mt-2">
                <button 
                  v-if="isOwner && review.replyText && review.replyText.trim() && !review.isReplyReported"
                  type="button"
                  class="btn btn-outline-success btn-sm me-2"
                  @click.stop="handleEditReply(review)"
                  title="編輯回覆"
                >
                  <i class="bi bi-pencil-fill me-1"></i> 編輯
                </button>

                <button 
                  v-if="isOwner && review.replyText && review.replyText.trim() && !review.isReplyReported"
                  type="button"
                  class="btn btn-outline-danger btn-sm me-2"
                  @click.stop="handleDeleteReply(review)"
                  title="刪除回覆"
                >
                  <i class="bi bi-trash-fill me-1"></i> 刪除
                </button>

                <button 
                  v-if="!review.isReplyReported"
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  @click.stop="handleReportReply(review)"
                >
                  <i class="bi bi-flag me-1"></i> 檢舉回覆
                </button>
              </div>
            </div>

            <!-- Actions -->
            <div class="review-actions">
              <div class="action-group view-edit">
                <!-- 回覆按鈕 (僅顯示給營主，且評價尚未回覆) -->
                <button 
                  type="button"
                  v-if="isOwner && !review.replyText" 
                  class="action-btn reply-btn" 
                  @click.stop="handleReply(review)"
                >
                  <i class="bi bi-chat-dots me-1"></i> 回覆
                </button>
              </div>
              
              <!-- 點讚和檢舉按鈕 - 移到右下角 -->
              <div class="right-bottom-actions">
                <!-- 點讚按鈕 -->
                <button 
                  type="button"
                  class="action-btn like-btn" 
                  :class="{ 'active': review.userLikeStatus === 1 }" 
                  @click.stop="handleLike(review.id)"
                >
                  <i class="bi" :class="review.userLikeStatus === 1 ? 'bi-hand-thumbs-up-fill me-1' : 'bi-hand-thumbs-up me-1'"></i>
                  {{ review.likeCount || 0 }} 讚
                </button>
                
                <!-- 評價檢舉按鈕 - 無條件顯示 -->
                <button 
                  type="button"
                  class="action-btn report-btn" 
                  @click.stop="handleReportReview(review)"
                >
                  <i class="bi bi-flag me-1"></i> 檢舉
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分頁 -->
      <nav v-if="totalPages > 1" class="pagination-container">
        <ul class="pagination">
          <li class="page-item" :class="{ disabled: currentPage === 0 }">
            <a class="page-link" href="#" @click.prevent="currentPage > 0 && changePage(currentPage - 1)">
              <i class="bi bi-chevron-left"></i>
            </a>
          </li>
          <li v-for="page in displayPages" :key="page" class="page-item" :class="{ active: currentPage === page }">
            <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page + 1 }}</a>
          </li>
          <li class="page-item" :class="{ disabled: currentPage >= totalPages - 1 }">
            <a class="page-link" href="#" @click.prevent="currentPage < totalPages - 1 && changePage(currentPage + 1)">
              <i class="bi bi-chevron-right"></i>
            </a>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';

export default {
  name: 'ReviewList',
  props: {
    campSiteId: { 
      type: [Number, String], 
      required: true 
    },
    searchParams: { 
      type: Object, 
      default: () => ({ 
        keyword: '', 
        sortBy: 'overallRating', 
        sortDirection: 'DESC', 
        ratingFilters: {
          overallRating: 0,
          cleanlinessRating: 0,
          convenienceRating: 0,
          friendlinessRating: 0
        } 
      }) 
    }
  },
  setup(props, { emit }) {
    // 基本狀態
    const reviews = ref([]);
    const loading = ref(true);
    const error = ref('');
    const currentPage = ref(0);
    const totalPages = ref(0);
    const totalElements = ref(0);
    const pageSize = ref(5);
    const currentUserId = ref(1);
    
    // 是否為營地主人
    const isOwner = ref(true); // 實際應從用戶權限獲取
    
    // 圖片加載狀態管理
    const imageLoading = ref({});
    
    // 營地資料
    const campSites = {
      1: { name: '山之間', region: '苗栗區' },
      2: { name: '海岸營地', region: '花蓮' },
      3: { name: '森林營區', region: '南投' },
      4: { name: '溪畔營地', region: '宜蘭' },
      5: { name: '山海營區', region: '台東' }
    };

    // 獲取營地名稱
    const getCampName = (campSiteId) => {
      return campSites[campSiteId]?.name || `營地 #${campSiteId}`;
    };
    
    // 獲取營地區域
    const getCampRegion = (campSiteId) => {
      return campSites[campSiteId]?.region || '';
    };

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      
      try {
        const date = new Date(dateString);
        return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
      } catch (e) {
        console.error('日期格式化錯誤:', e);
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

    // 處理圖片載入完成
    const handleImageLoaded = (reviewId, index) => {
      if (!imageLoading.value[reviewId]) {
        imageLoading.value[reviewId] = {};
      }
      imageLoading.value[reviewId][index] = false;
    };

    // 處理圖片載入錯誤
    const handleImageError = (event, reviewId, index) => {
      console.error(`圖片載入失敗: ${reviews.value.find(r => r.id === reviewId)?.imageUrls?.[index]}`);
      event.target.src = '/img/image-placeholder.jpg';
      
      if (!imageLoading.value[reviewId]) {
        imageLoading.value[reviewId] = {};
      }
      imageLoading.value[reviewId][index] = false;
    };

    // 查看大圖
    const viewImage = (reviewId, index) => {
      const review = reviews.value.find(r => r.id === reviewId);
      if (!review || !review.imageUrls || review.imageUrls.length === 0) return;
      
      emit('view-image', review.imageUrls, index);
      
      // 如果沒有emit方法，可以使用Swal展示圖片
      if (!emit) {
        const currentImageUrl = processImageUrl(review.imageUrls[index]);
        Swal.fire({
          imageUrl: currentImageUrl,
          imageAlt: '評價圖片',
          width: 'auto',
          showCloseButton: true,
          showConfirmButton: false,
          background: 'rgba(0,0,0,0.9)',
          padding: '10px',
          customClass: {
            container: 'image-viewer-container',
            popup: 'image-viewer-popup'
          }
        });
      }
    };

    // 重新嘗試連接
    const retryConnection = () => {
      loading.value = true;
      error.value = '';
      fetchReviews();
    };

    // 載入評價
    const fetchReviews = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        // 構建API參數
        const params = { 
          campSiteId: props.campSiteId, 
          page: currentPage.value, 
          size: pageSize.value, 
          userId: currentUserId.value,
          sortBy: props.searchParams.sortBy,
          direction: props.searchParams.sortDirection
        };
        
        // 添加篩選條件
        const ratings = props.searchParams.ratingFilters;
        if (ratings.overallRating > 0) {
          params.minRating = ratings.overallRating;
        }
        if (ratings.cleanlinessRating > 0) {
          params.minCleanlinessRating = ratings.cleanlinessRating;
        }
        if (ratings.convenienceRating > 0) {
          params.minConvenienceRating = ratings.convenienceRating;
        }
        if (ratings.friendlinessRating > 0) {
          params.minFriendlinessRating = ratings.friendlinessRating;
        }
        
        // 添加關鍵字搜尋
        if (props.searchParams.keyword) {
          params.keyword = props.searchParams.keyword;
        }
        
        try {
          // 設置較長的超時時間
          const apiUrl = `/api/reviews`;
          const response = await axios.get(apiUrl, { params, timeout: 10000 });
          const data = response.data;
          
          // 解析數據
          if (data && data.content) {
            // 在前端進行額外過濾
            let filteredContent = data.content;
            if (ratings.overallRating > 0) {
              filteredContent = filteredContent.filter(review => review.overallRating >= ratings.overallRating);
            }
            if (ratings.cleanlinessRating > 0) {
              filteredContent = filteredContent.filter(review => review.cleanlinessRating >= ratings.cleanlinessRating);
            }
            if (ratings.convenienceRating > 0) {
              filteredContent = filteredContent.filter(review => review.convenienceRating >= ratings.convenienceRating);
            }
            if (ratings.friendlinessRating > 0) {
              filteredContent = filteredContent.filter(review => review.friendlinessRating >= ratings.friendlinessRating);
            }
            
            // 使用過濾後的數據
            reviews.value = filteredContent;
            totalPages.value = data.totalPages || 1;
            totalElements.value = filteredContent.length;
            currentPage.value = data.number || 0;
          } else if (Array.isArray(data)) {
            reviews.value = data;
            totalPages.value = 1;
            totalElements.value = reviews.value.length;
            currentPage.value = 0;
          } else {
            reviews.value = [];
            totalPages.value = 0;
            totalElements.value = 0;
            currentPage.value = 0;
          }
        } catch (apiError) {
          console.error('獲取評價API請求失敗:', apiError);
          // 在這裡處理無法連接到API的情況
          // 可以使用模擬數據或顯示錯誤訊息
          error.value = '無法連接到API，請稍後再試';
        }
      } catch (err) {
        console.error('獲取評價失敗:', err);
        // 改進的錯誤消息
        if (err.code === 'ECONNABORTED') {
          error.value = '連接超時，伺服器回應時間過長。';
        } else if (err.code === 'ERR_NETWORK') {
          error.value = '無法連接到伺服器，請檢查網絡連接。';
        } else {
          error.value = `獲取評價失敗: ${err.message}`;
        }
      } finally {
        loading.value = false;
      }
    };

    // 計算分頁顯示
    const displayPages = computed(() => {
      const pages = [];
      const maxDisplay = 5;
      
      if (totalPages.value <= maxDisplay) {
        for (let i = 0; i < totalPages.value; i++) {
          pages.push(i);
        }
      } else {
        let start, end;
        
        if (currentPage.value < Math.floor(maxDisplay / 2)) {
          start = 0;
          end = maxDisplay - 1;
        } else if (currentPage.value >= totalPages.value - Math.floor(maxDisplay / 2)) {
          start = totalPages.value - maxDisplay;
          end = totalPages.value - 1;
        } else {
          start = currentPage.value - Math.floor(maxDisplay / 2);
          end = currentPage.value + Math.floor(maxDisplay / 2);
        }
        
        start = Math.max(0, start);
        end = Math.min(totalPages.value - 1, end);
        
        for (let i = start; i <= end; i++) {
          pages.push(i);
        }
      }
      
      return pages;
    });

    // 頁面變更
    const changePage = (page) => { 
      if (page >= 0 && page < totalPages.value) { 
        currentPage.value = page; 
        fetchReviews(); 
      }
    };
    
    // 處理點讚
    const handleLike = async (reviewId) => {
      try {
        const targetReview = reviews.value.find(r => r.id === reviewId);
        
        if (!targetReview) return;
        
        // 先在本地更新狀態以提升用戶體驗
        const isLiked = targetReview.userLikeStatus === 1;
        
        // 切換點讚狀態
        targetReview.userLikeStatus = isLiked ? 0 : 1;
        // 更新讚數
        targetReview.likeCount = isLiked ? 
          Math.max(0, (targetReview.likeCount || 0) - 1) : 
          (targetReview.likeCount || 0) + 1;
        
        // 獲取當前用戶ID
        const userId = currentUserId.value;
        
        try {
          // 嘗試發送請求到後端
          const response = await axios.post(`/api/reviews/${reviewId}/like?userId=${userId}`);
          console.log('點讚成功:', response.data);
        } catch (apiError) {
          console.warn('點讚API請求失敗，但UI已更新:', apiError);
          // 即使API請求失敗，UI顯示也保持一致，提供更好的用戶體驗
        }
        
      } catch (err) {
        console.error('點讚操作失敗:', err);
        
        // 如果失敗，恢復之前的狀態
        const targetReview = reviews.value.find(r => r.id === reviewId);
        if (targetReview) {
          const isLiked = targetReview.userLikeStatus === 1;
          targetReview.userLikeStatus = isLiked ? 0 : 1;
          targetReview.likeCount = isLiked ? 
            (targetReview.likeCount || 0) - 1 : 
            Math.max(0, (targetReview.likeCount || 0) + 1);
        }
      }
    };
    
    // 編輯評價
    const handleEditReview = (review) => {
      Swal.fire({
        title: '編輯評價',
        html: `
          <div class="mb-3">
            <label class="form-label">評價內容</label>
            <textarea id="reviewText" class="form-control" rows="5">${review.reviewText || ''}</textarea>
          </div>
          <div class="mb-3">
            <label class="form-label">優點</label>
            <textarea id="pros" class="form-control" rows="3">${review.pros || ''}</textarea>
          </div>
          <div class="mb-3">
            <label class="form-label">缺點</label>
            <textarea id="cons" class="form-control" rows="3">${review.cons || ''}</textarea>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '確認更新',
        cancelButtonText: '取消',
        preConfirm: () => {
          const reviewText = document.getElementById('reviewText').value;
          const pros = document.getElementById('pros').value;
          const cons = document.getElementById('cons').value;
          
          if (!reviewText.trim()) {
            Swal.showValidationMessage('評價內容不能為空');
            return false;
          }
          
          return { reviewText, pros, cons };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const { reviewText, pros, cons } = result.value;
          
          try {
            // 使用axios來更新評價
            await axios.put(`/api/reviews/${review.id}`, {
              ...review,
              reviewText,
              pros,
              cons
            });
            
            // 更新本地資料
            const index = reviews.value.findIndex(r => r.id === review.id);
            if (index !== -1) {
              reviews.value[index] = {
                ...reviews.value[index],
                reviewText,
                pros,
                cons,
                updatedAt: new Date().toISOString()
              };
            }
            
            Swal.fire('更新成功', '您的評價已成功更新', 'success');
          } catch (error) {
            console.error('更新評價失敗:', error);
            Swal.fire('更新失敗', '更新評價時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 刪除評價
    const handleDeleteReview = (review) => {
      Swal.fire({
        title: '確認刪除',
        text: '您確定要刪除這則評價嗎？此操作無法復原。',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確認刪除',
        cancelButtonText: '取消',
        confirmButtonColor: '#dc3545'
      }).then(async (result) => {
        if (result.isConfirmed) {
          try {
            // 使用axios刪除評價
            await axios.delete(`/api/reviews/${review.id}?userId=${currentUserId.value}`);
            
            // 從列表中移除該評價
            reviews.value = reviews.value.filter(r => r.id !== review.id);
            
            Swal.fire('刪除成功', '評價已成功刪除', 'success');
          } catch (error) {
            console.error('刪除評價失敗:', error);
            Swal.fire('刪除失敗', '刪除評價時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 回覆評價
    const handleReply = (review) => {
      Swal.fire({
        title: '回覆評價',
        html: `
          <div class="mb-3">
            <label class="form-label">評價內容</label>
            <div class="p-3 bg-light rounded mb-3">${review.reviewText}</div>
            <label class="form-label">您的回覆</label>
            <textarea id="replyText" class="form-control" rows="5"></textarea>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '提交回覆',
        cancelButtonText: '取消',
        preConfirm: () => {
          const replyText = document.getElementById('replyText').value;
          
          if (!replyText.trim()) {
            Swal.showValidationMessage('回覆內容不能為空');
            return false;
          }
          
          return { replyText };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const { replyText } = result.value;
          
          try {
            // 使用axios提交回覆
            const response = await axios.put(`/api/reviews/${review.id}/reply`, {
              replyText
            });
            
            // 更新本地資料
            const index = reviews.value.findIndex(r => r.id === review.id);
            if (index !== -1) {
              reviews.value[index] = {
                ...reviews.value[index],
                replyText,
                updatedAt: new Date().toISOString()
              };
            }
            
            Swal.fire('回覆成功', '您的回覆已成功發布', 'success');
          } catch (error) {
            console.error('提交回覆失敗:', error);
            Swal.fire('回覆失敗', '提交回覆時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 編輯回覆
    const handleEditReply = (review) => {
      Swal.fire({
        title: '編輯回覆',
        html: `
          <div class="mb-3">
            <label class="form-label">評價內容</label>
            <div class="p-3 bg-light rounded mb-3">${review.reviewText}</div>
            <label class="form-label">修改回覆</label>
            <textarea id="editReplyText" class="form-control" rows="5">${review.replyText || ''}</textarea>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '更新回覆',
        cancelButtonText: '取消',
        preConfirm: () => {
          const replyText = document.getElementById('editReplyText').value;
          
          if (!replyText.trim()) {
            Swal.showValidationMessage('回覆內容不能為空');
            return false;
          }
          
          return { replyText };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const { replyText } = result.value;
          
          try {
            // 使用axios更新回覆
            const response = await axios.put(`/api/reviews/${review.id}/reply`, {
              replyText
            });
            
            // 更新本地資料
            const index = reviews.value.findIndex(r => r.id === review.id);
            if (index !== -1) {
              reviews.value[index] = {
                ...reviews.value[index],
                replyText,
                updatedAt: new Date().toISOString()
              };
            }
            
            Swal.fire('更新成功', '回覆已成功更新', 'success');
          } catch (error) {
            console.error('更新回覆失敗:', error);
            Swal.fire('更新失敗', '更新回覆時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 刪除回覆
    const handleDeleteReply = (review) => {
      Swal.fire({
        title: '確認刪除回覆',
        text: '您確定要刪除這則回覆嗎？此操作無法復原。',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確認刪除',
        cancelButtonText: '取消',
        confirmButtonColor: '#dc3545'
      }).then(async (result) => {
        if (result.isConfirmed) {
          try {
            // 使用axios刪除回覆
            await axios.delete(`/api/reviews/${review.id}/reply`);
            
            // 更新本地資料
            const index = reviews.value.findIndex(r => r.id === review.id);
            if (index !== -1) {
              reviews.value[index] = {
                ...reviews.value[index],
                replyText: null,
                updatedAt: new Date().toISOString()
              };
            }
            
            Swal.fire('刪除成功', '回覆已成功刪除', 'success');
          } catch (error) {
            console.error('刪除回覆失敗:', error);
            Swal.fire('刪除失敗', '刪除回覆時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 檢舉評價
    const handleReportReview = (review) => {
      Swal.fire({
        title: '檢舉評價',
        html: `
          <div class="mb-3">
            <label class="form-label">檢舉原因</label>
            <select id="reportType" class="form-select">
              <option value="" disabled selected>請選擇檢舉原因</option>
              <option value="1">不實內容</option>
              <option value="2">冒犯性內容或語言</option>
              <option value="3">廣告或垃圾訊息</option>
              <option value="4">暴力或仇恨言論</option>
              <option value="5">侵犯隱私權</option>
              <option value="6">其他</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">詳細說明</label>
            <textarea id="reportReason" class="form-control" rows="5" placeholder="請詳細說明檢舉原因..."></textarea>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '提交檢舉',
        cancelButtonText: '取消',
        confirmButtonColor: '#dc3545',
        preConfirm: () => {
          const reportType = document.getElementById('reportType').value;
          const reason = document.getElementById('reportReason').value;
          
          if (!reportType) {
            Swal.showValidationMessage('請選擇檢舉原因');
            return false;
          }
          
          if (!reason.trim()) {
            Swal.showValidationMessage('請提供詳細說明');
            return false;
          }
          
          return { reportType: parseInt(reportType), reason };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const { reportType, reason } = result.value;
          
          try {
            // 使用axios提交檢舉
            await axios.post('/api/review-reports', {
              reviewId: review.id,
              userId: currentUserId.value,
              reportType,
              reason,
              status: 'pending'
            });
            
            Swal.fire('檢舉成功', '感謝您的檢舉，我們將盡快處理', 'success');
          } catch (error) {
            console.error('提交檢舉失敗:', error);
            Swal.fire('檢舉失敗', '提交檢舉時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };
    
    // 檢舉回覆
    const handleReportReply = (review) => {
      Swal.fire({
        title: '檢舉回覆',
        html: `
          <div class="mb-3">
            <label class="form-label">檢舉原因</label>
            <select id="replyReportType" class="form-select">
              <option value="" disabled selected>請選擇檢舉原因</option>
              <option value="1">不實內容</option>
              <option value="2">冒犯性內容或語言</option>
              <option value="3">廣告或垃圾訊息</option>
              <option value="4">暴力或仇恨言論</option>
              <option value="5">侵犯隱私權</option>
              <option value="6">其他</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">詳細說明</label>
            <textarea id="replyReportReason" class="form-control" rows="5" placeholder="請詳細說明檢舉原因..."></textarea>
          </div>
        `,
        showCancelButton: true,
        confirmButtonText: '提交檢舉',
        cancelButtonText: '取消',
        confirmButtonColor: '#dc3545',
        preConfirm: () => {
          const reportType = document.getElementById('replyReportType').value;
          const reason = document.getElementById('replyReportReason').value;
          
          if (!reportType) {
            Swal.showValidationMessage('請選擇檢舉原因');
            return false;
          }
          
          if (!reason.trim()) {
            Swal.showValidationMessage('請提供詳細說明');
            return false;
          }
          
          return { reportType: parseInt(reportType), reason };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const { reportType, reason } = result.value;
          
          try {
            // 使用axios提交檢舉
            await axios.post('/api/review-reports', {
              reviewId: review.id,
              userId: currentUserId.value,
              reportType,
              reason,
              isReplyReport: true,
              status: 'pending'
            });
            
            Swal.fire('檢舉成功', '感謝您的檢舉，我們將盡快處理', 'success');
          } catch (error) {
            console.error('提交檢舉失敗:', error);
            Swal.fire('檢舉失敗', '提交檢舉時發生錯誤，請稍後再試', 'error');
          }
        }
      });
    };

    // 提供公開方法以供父組件調用
    const applySearchFilters = () => {
      currentPage.value = 0;
      fetchReviews();
    };

    const clearSearchFilters = () => {
      currentPage.value = 0;
      fetchReviews();
    };

    const refreshReviews = () => {
      fetchReviews();
    };

    // 添加一個新評價（由父組件調用）
    const addNewReview = (review) => {
      reviews.value.unshift(review);
      totalElements.value = (totalElements.value || 0) + 1;
    };

    // 應用加載時初始化
    onMounted(() => {
      fetchReviews();
    });

    return { 
      reviews, 
      loading, 
      error, 
      currentPage, 
      totalPages, 
      totalElements, 
      displayPages, 
      currentUserId,
      getCampName, 
      getCampRegion, 
      formatDate, 
      changePage, 
      handleLike, 
      isOwner,
      retryConnection,
      handleEditReview,
      handleDeleteReview,
      handleReply,
      handleEditReply,
      handleDeleteReply,
      handleReportReview,
      handleReportReply,
      applySearchFilters,
      clearSearchFilters,
      refreshReviews,
      addNewReview,
      // 圖片相關功能
      processImageUrl,
      handleImageLoaded,
      handleImageError,
      viewImage,
      imageLoading
    };
  }
};
</script>

<style scoped>
/* 首先定義墨綠色系色彩變數 */
:root {
  --forest-dark: #1E432E;
  --forest-medium: #356648;
  --forest-light: #5B8F6D;
}

/* 所有文字使用墨綠色的類 */
.text-forest {
  color: var(--forest-dark, #1E432E) !important;
}

.text-forest-light {
  color: var(--forest-medium, #356648) !important;
}

.review-list-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  background-color: transparent;
  color: #1E432E;
  line-height: 1.6;
  width: 100%;
}

.loading-state {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 2px 6px rgba(40, 80, 60, 0.08);
  border: 1px solid #BFE0C9;
}

.spinner-border {
  color: #47845D;
  width: 3rem;
  height: 3rem;
}

.empty-state {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 40px 20px;
  box-shadow: 0 2px 6px rgba(40, 80, 60, 0.08);
  border: 1px solid #BFE0C9;
}

.empty-icon {
  font-size: 3rem;
  color: #47845D;
  margin-bottom: 15px;
  opacity: 0.7;
}

.empty-state h3 {
  font-weight: 600;
  margin-bottom: 10px;
}

.alert-box {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-weight: 500;
}

.alert-danger {
  background-color: rgba(220, 53, 69, 0.1);
  color: #d32f2f;
  border: 1px solid rgba(220, 53, 69, 0.3);
}

.section-title {
  font-size: 1.8rem;
  color: #1E432E;
  margin-bottom: 1.8rem;
  font-weight: 700; /* 修改為700使更粗 */
  position: relative;
  padding-bottom: 0.8rem;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #47845D, #2A5D3C);
  border-radius: 3px;
}

.count {
  font-size: 1.1rem;
  font-weight: 500;
  color: #356648;
}

.review-item {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 6px rgba(40, 80, 60, 0.08);
  padding: 25px;
  transition: all 0.3s ease;
  border: 1px solid #BFE0C9;
  position: relative;
}

.review-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(40, 80, 60, 0.15);
}

.review-header {
  padding-bottom: 15px;
  margin-bottom: 15px;
  border-bottom: 1px solid #BFE0C9;
}

/* 營地區域和名稱更大更粗 */
.camp-region, .camp-name {
  font-size: 1.4rem; /* 放大字體 */
  font-weight: 700; /* 更粗的字體 */
  color: #1E432E; /* 墨綠色 */
}

.camp-region {
  color: #1E432E;
}

.camp-name {
  color: #1E432E;
  text-decoration: underline; /* 保留下劃線 */
  text-underline-offset: 3px; /* 調整下劃線位置 */
  display: inline-block;
}

.camp-name:hover {
  color: #47845D;
}

/* 營地評價系統標題 */
.section-title {
  font-size: 1.8rem;
  color: #1E432E; /* 墨綠色 */
  margin-bottom: 1.8rem;
  font-weight: 700; /* 加粗 */
  position: relative;
  padding-bottom: 0.8rem;
}

.star-rating {
  display: inline-flex;
  line-height: 1;
  align-items: center;
}

.star {
  font-size: 1.3rem;
  color: #FFCB45 !important;
  margin-right: 2px;
  cursor: default;
  display: inline-block !important;
}

.star-empty {
  opacity: 0.35;
  color: #FFCB45 !important;
  display: inline-block !important;
}

.star-filled {
  opacity: 1;
  text-shadow: 0 0 3px rgba(255, 203, 69, 0.3);
  color: #FFCB45 !important;
  display: inline-block !important;
}

.overall-rating .star {
  font-size: 1.4rem;
}

.user-info {
  margin-bottom: 15px;
  position: relative; /* 用於相對定位管理按鈕 */
}

/* 修正管理按鈕區域 - 確保顯示在正確位置 */
.admin-actions {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  gap: 10px;
  z-index: 10;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 5px;
  border-radius: 8px;
}

/* 優化管理按鈕樣式，解決顯示問題 */
.admin-btn {
  padding: 6px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid;
  background-color: white;
}

.admin-btn i {
  font-size: 14px;
}

.edit-btn {
  color: #2A5D3C;
  border-color: #2A5D3C;
}

.edit-btn:hover {
  background-color: rgba(42, 93, 60, 0.1);
  transform: translateY(-2px);
}

.delete-btn {
  color: #dc3545;
  border-color: #dc3545;
}

.delete-btn:hover {
  background-color: rgba(220, 53, 69, 0.1);
  transform: translateY(-2px);
}

/* 優化回覆管理按鈕 */
.reply-actions {
  display: flex;
  gap: 8px;
}

.reply-edit-btn {
  color: #2A5D3C;
  border-color: #2A5D3C;
  padding: 4px 8px;
  font-size: 12px;
}

.reply-edit-btn:hover {
  background-color: rgba(42, 93, 60, 0.1);
  transform: translateY(-2px);
}

.reply-delete-btn {
  color: #dc3545;
  border-color: #dc3545;
  padding: 4px 8px;
  font-size: 12px;
}

.reply-delete-btn:hover {
  background-color: rgba(220, 53, 69, 0.1);
  transform: translateY(-2px);
}

.user-name {
  font-size: 1.1rem;
  color: #1E432E;
  font-weight: 600;
}

.review-date {
  color: #356648;
  font-size: 0.9rem;
}

.edited-tag {
  background-color: rgba(110, 171, 127, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
  margin-left: 5px;
  color: #1E432E;
}

.review-text {
  color: #1E432E;
  line-height: 1.7;
  white-space: pre-line;
  margin-bottom: 15px;
}

.pros-cons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.pros, .cons {
  padding: 15px;
  border-radius: 12px;
  flex: 1;
}

.pros {
  background-color: rgba(76, 175, 80, 0.08);
  border-left: 3px solid #47845D;
}

.cons {
  background-color: rgba(244, 67, 54, 0.08);
  border-left: 3px solid #f44336;
}

.label {
  font-weight: 600;
  color: #1E432E;
  margin-right: 5px;
  display: block;
  margin-bottom: 5px;
}

.value {
  color: #1E432E;
  line-height: 1.6;
}

.rating-details {
  padding: 15px 0;
  margin: 15px 0;
  border-top: 1px solid rgba(191, 224, 201, 0.5);
  border-bottom: 1px solid rgba(191, 224, 201, 0.5);
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.rating-item {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 30%;
}

.rating-label {
  min-width: 85px;
  color: #1E432E;
  font-weight: 500;
}

/* 評價圖片相關樣式 - 從ReviewItem.vue整合 */
.review-images {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding: 5px 2px;
}

.image-wrapper {
  width: 160px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 1px solid #BFE0C9;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  flex-shrink: 0;
  background-color: #f8f9fa;
}

.image-wrapper:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.review-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
  display: block;
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
  background-color: rgba(0, 0, 0, 0.03);
}

.spinner-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.15em;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  color: white;
  font-size: 1.5rem;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.image-wrapper:hover .review-image {
  transform: scale(1.1);
}

.more-images {
  width: 60px;
  height: 120px;
  border-radius: 10px;
  background-color: rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #1E432E;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px dashed #BFE0C9;
  flex-shrink: 0;
}

.more-images:hover {
  background-color: rgba(0, 0, 0, 0.1);
  transform: scale(1.03);
}

/* 圖片查看器樣式 */
.image-viewer-container {
  z-index: 1060;
}

.image-viewer-popup {
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
}

/* 評價回覆區塊樣式 */
.reply-section {
  background-color: rgba(76, 175, 80, 0.05);
  padding: 15px;
  border-radius: 12px;
  border-left: 3px solid #47845D;
  margin: 15px 0;
  position: relative;
}

.reply-header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reply-header {
  color: #1E432E;
  font-weight: 600;
  margin: 0;
}

.reply-content {
  color: #1E432E;
  margin-bottom: 0;
  padding-left: 10px;
  line-height: 1.6;
}

/* 回覆檢舉按鈕樣式 */
.reply-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.report-reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  background-color: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.report-reply-btn:hover {
  background-color: #f0f0f0;
  color: #dc3545;
  border-color: #dc3545;
}

.report-reply-btn i {
  font-size: 10px;
}

/* 操作按鈕樣式 */
.review-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid rgba(191, 224, 201, 0.5);
  position: relative;
}

.action-group {
  display: flex;
  gap: 10px;
}

/* 點讚和檢舉按鈕 - 放在右下角 */
.right-bottom-actions {
  position: absolute;
  bottom: 0;
  right: 0;
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 15px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #ffffff;
  border: 1px solid #BFE0C9;
  color: #1E432E;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  min-width: 90px; /* 確保按鈕寬度一致 */
  text-decoration: none; /* 確保連結樣式正確 */
}

/* 加強所有按鈕的交互反饋 */
.action-btn:hover {
  transform: translateY(-3px) !important;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1) !important;
}

.action-btn:active {
  transform: translateY(1px) !important;
}

.action-btn i {
  color: inherit;
}

.view-btn:hover, .edit-btn:hover {
  background-color: rgba(110, 171, 127, 0.1);
}

.like-btn {
  color: #1E432E;
}

.like-btn.active {
  background-color: rgba(110, 171, 127, 0.1);
  border-color: #2A5D3C;
  color: #1E432E;
}

.like-btn:hover, .like-btn.active:hover {
  background-color: rgba(110, 171, 127, 0.15);
}

.reply-btn {
  color: #17a2b8;
  border-color: #17a2b8;
  background-color: white;
}

.reply-btn:hover {
  background-color: rgba(23, 162, 184, 0.1);
}

/* 檢舉按鈕樣式 */
.report-btn {
  color: #6c757d;
  border-color: #6c757d;
}

.report-btn:hover {
  background-color: rgba(108, 117, 125, 0.1);
  color: #dc3545;
  border-color: #dc3545;
}

/* 新增的編輯和刪除按鈕替代樣式 */
.edit-btn-alt {
  color: #1E432E;
  border-color: #1E432E;
}

.edit-btn-alt:hover {
  background-color: rgba(30, 67, 46, 0.1);
}

.delete-btn-alt {
  color: #dc3545;
  border-color: #dc3545;
}

.delete-btn-alt:hover {
  background-color: rgba(220, 53, 69, 0.1);
}

/* 分頁區塊樣式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.pagination {
  background-color: #ffffff;
  padding: 8px;
  border-radius: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #BFE0C9;
  display: flex;
  gap: 5px;
}

.pagination .page-item {
  list-style: none;
}

.pagination .page-link {
  border: none;
  color: #1E432E;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  text-decoration: none;
  font-weight: 500;
}

.pagination .page-item.active .page-link {
  background: linear-gradient(135deg, #2A5D3C, #47845D);
  color: white;
  box-shadow: 0 2px 5px rgba(42, 93, 60, 0.2);
}

.pagination .page-item:not(.active) .page-link:hover {
  background-color: rgba(110, 171, 127, 0.1);
  transform: translateY(-2px);
}

.pagination .page-item.disabled .page-link {
  color: #aaa;
  cursor: not-allowed;
}

/* 響應式調整 */
@media (max-width: 992px) {
  .rating-details {
    flex-direction: column;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .pros-cons {
    flex-direction: column;
  }
  
  .image-wrapper {
    width: 120px;
    height: 90px;
  }
  
  .more-images {
    width: 50px;
    height: 90px;
  }
  
  .right-bottom-actions {
    position: relative;
    margin-top: 15px;
    justify-content: flex-end;
    width: 100%;
  }
}

@media (max-width: 576px) {
  .review-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .action-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .section-title {
    font-size: 1.4rem;
  }
  
  .review-item {
    padding: 20px;
  }
}
</style>