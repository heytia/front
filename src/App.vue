<template>
  <div class="app-container">
    <header>
      <div class="wrapper">
        <h1>營地評價系統</h1>
        <div class="test-connection">
          <button @click="testBackendConnection">測試後端連接</button>
          <span v-if="connectionStatus" :class="{ 'success': connectionStatus.includes('成功'), 'error': connectionStatus.includes('失敗') }">
            {{ connectionStatus }}
          </span>
        </div>
      </div>
    </header>

    <main>
      <div class="app-layout">
        <!-- 左側篩選面板 -->
        <aside class="filter-sidebar">
          <div class="search-box">
            <h3>關鍵字搜尋</h3>
            <div class="input-group">
              <input type="text" :value="searchKeyword" @input="updateKeyword" placeholder="請輸入關鍵字..."/>
            </div>
          </div>
          
          <div class="filter-section">
            <h3>排序設定</h3>
            <div class="sort-options">
              <div class="sort-group">
                <label>排序依據:</label>
                <div class="option-buttons">
                  <button type="button" @click="handleSortFieldChange('overallRating')" 
                    :class="{ 'active': searchParams.sortBy === 'overallRating' }">總體評分</button>
                  <button type="button" @click="handleSortFieldChange('cleanlinessRating')" 
                    :class="{ 'active': searchParams.sortBy === 'cleanlinessRating' }">清潔度</button>
                  <button type="button" @click="handleSortFieldChange('convenienceRating')" 
                    :class="{ 'active': searchParams.sortBy === 'convenienceRating' }">便利性</button>
                  <button type="button" @click="handleSortFieldChange('friendlinessRating')" 
                    :class="{ 'active': searchParams.sortBy === 'friendlinessRating' }">友善度</button>
                  <button type="button" @click="handleSortFieldChange('createdAt')" 
                    :class="{ 'active': searchParams.sortBy === 'createdAt' }">發表時間</button>
                </div>
              </div>
              
              <div class="sort-group">
                <label>排序方式:</label>
                <div class="option-buttons direction-buttons">
                  <button type="button" @click="handleSortDirectionChange('DESC')" 
                    :class="{ 'active': searchParams.sortDirection === 'DESC' }">
                    {{ getSortDirectionText('DESC') }}</button>
                  <button type="button" @click="handleSortDirectionChange('ASC')" 
                    :class="{ 'active': searchParams.sortDirection === 'ASC' }">
                    {{ getSortDirectionText('ASC') }}</button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="filter-section">
            <h3>依評分篩選</h3>
            <div class="rating-filters">
              <div class="filter-group">
                <label>總體評分至少:</label>
                <div class="star-buttons">
                  <button v-for="rating in 5" :key="`overall-${rating}`" 
                    :class="['star-button', { 'active': searchParams.ratingFilters.overallRating === rating }]"
                    @click="handleRatingFilterChange('overallRating', rating)">
                    {{ rating }}★
                  </button>
                </div>
              </div>
              
              <div class="filter-group">
                <label>清潔度至少:</label>
                <div class="star-buttons">
                  <button v-for="rating in 5" :key="`cleanliness-${rating}`" 
                    :class="['star-button', { 'active': searchParams.ratingFilters.cleanlinessRating === rating }]"
                    @click="handleRatingFilterChange('cleanlinessRating', rating)">
                    {{ rating }}★
                  </button>
                </div>
              </div>
              
              <div class="filter-group">
                <label>便利性至少:</label>
                <div class="star-buttons">
                  <button v-for="rating in 5" :key="`convenience-${rating}`" 
                    :class="['star-button', { 'active': searchParams.ratingFilters.convenienceRating === rating }]"
                    @click="handleRatingFilterChange('convenienceRating', rating)">
                    {{ rating }}★
                  </button>
                </div>
              </div>
              
              <div class="filter-group">
                <label>友善度至少:</label>
                <div class="star-buttons">
                  <button v-for="rating in 5" :key="`friendliness-${rating}`" 
                    :class="['star-button', { 'active': searchParams.ratingFilters.friendlinessRating === rating }]"
                    @click="handleRatingFilterChange('friendlinessRating', rating)">
                    {{ rating }}★
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="filter-actions">
            <button type="button" class="search-button" @click="executeSearch">執行搜尋</button>
            <button type="button" class="clear-button" @click="clearAllFilters">清除條件</button>
          </div>
        </aside>
        
        <!-- 右側內容區域 -->
        <div class="content-area">
          <div class="add-review-section">
            <button v-if="!showReviewForm" @click="openReviewForm" class="add-review-btn">發表新評價</button>
          </div>
          
          <!-- 評價表單 -->
          <div v-if="showReviewForm" class="review-form">
            <h2>發表評價</h2>
            
            <form id="reviewForm" @submit.prevent="handleReviewSubmit" enctype="multipart/form-data">
              <div class="rating-section">
                <div class="rating-group">
                  <label>清潔度:</label>
                  <div class="star-rating">
                    <span v-for="i in 5" :key="i" :class="['star', { 'filled': i <= form.cleanlinessRating }]"
                      @click="setRating('cleanlinessRating', i)">★</span>
                    <span v-if="form.cleanlinessRating > 0" class="rating-value">{{ form.cleanlinessRating }}</span>
                  </div>
                </div>
                
                <div class="rating-group">
                  <label>便利性:</label>
                  <div class="star-rating">
                    <span v-for="i in 5" :key="i" :class="['star', { 'filled': i <= form.convenienceRating }]"
                      @click="setRating('convenienceRating', i)">★</span>
                    <span v-if="form.convenienceRating > 0" class="rating-value">{{ form.convenienceRating }}</span>
                  </div>
                </div>
                
                <div class="rating-group">
                  <label>友善度:</label>
                  <div class="star-rating">
                    <span v-for="i in 5" :key="i" :class="['star', { 'filled': i <= form.friendlinessRating }]"
                      @click="setRating('friendlinessRating', i)">★</span>
                    <span v-if="form.friendlinessRating > 0" class="rating-value">{{ form.friendlinessRating }}</span>
                  </div>
                </div>
              </div>
              
              <div class="form-group">
                <label for="reviewText">評價內容:</label>
                <textarea id="reviewText" v-model="form.reviewText" rows="5" placeholder="請分享您的營地體驗..." required></textarea>
              </div>
              
              <div class="pros-cons-section">
                <div class="form-group">
                  <label for="pros">優點:</label>
                  <textarea id="pros" v-model="form.pros" rows="3" placeholder="有哪些地方讓您滿意？"></textarea>
                </div>
                
                <div class="form-group">
                  <label for="cons">缺點:</label>
                  <textarea id="cons" v-model="form.cons" rows="3" placeholder="有哪些地方需要改進？"></textarea>
                </div>
              </div>
              
              <div class="form-group">
                <label>上傳照片: <span class="text-muted">(最多 {{ maxFiles }} 張，每張不超過 5MB)</span></label>
                <div class="file-upload-container">
                  <input type="file" multiple accept="image/*" @change="handleFileSelection"
                    class="form-control" id="imageFile" name="imageFile" ref="fileInput"/>
                </div>
                
                <div v-if="previewImages.length > 0" class="image-previews">
                  <div v-for="(preview, index) in previewImages" :key="index" class="preview-item">
                    <img :src="preview" :alt="`預覽圖片 ${index + 1}`" class="preview-image" />
                    <button type="button" class="remove-image" @click="removePreviewImage(index)">×</button>
                  </div>
                </div>
              </div>
              
              <div class="form-actions">
                <button type="button" class="cancel-button" @click="closeReviewForm">取消</button>
                <button type="submit" class="submit-button" :disabled="uploading">
                  <span v-if="uploading">上傳中...</span>
                  <span v-else>發表評價</span>
                </button>
              </div>
            </form>

            <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: `${uploadProgress}%` }"></div>
              </div>
              <div class="progress-text">上傳進度: {{ uploadProgress }}%</div>
            </div>

            <div v-if="uploadMessage" class="upload-message" :class="{ 'success': uploadSuccess, 'error': !uploadSuccess }">
              {{ uploadMessage }}
            </div>
          </div>
          
          <!-- 評價列表 -->
          <ReviewList ref="reviewListRef" :campSiteId="campSiteId" :searchParams="searchParams" />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onUnmounted, watch } from 'vue';
import axios from 'axios';
import ReviewList from './components/ReviewList.vue';

// 基本資料
const campSiteId = ref(1);
const connectionStatus = ref('');
const showReviewForm = ref(false);
const reviewListRef = ref(null);
const fileInput = ref(null);
const uploadProgress = ref(0);
const uploadMessage = ref('');
const uploadSuccess = ref(false);

// 表單資料
const form = reactive({
  campSiteId: 1,
  userId: 1,
  userName: '測試用戶', 
  reviewText: '',
  cleanlinessRating: 0,
  convenienceRating: 0,
  friendlinessRating: 0,
  pros: '',
  cons: '',
  imageUrls: []
});

// 自動計算整體評分
const overallRating = computed(() => {
  const { cleanlinessRating, convenienceRating, friendlinessRating } = form;
  if (cleanlinessRating > 0 && convenienceRating > 0 && friendlinessRating > 0) {
    const avg = (cleanlinessRating + convenienceRating + friendlinessRating) / 3;
    return Math.round(avg * 2) / 2;
  }
  return 0;
});

// 搜尋和排序相關
const searchKeyword = ref('');
const searchParams = reactive({
  keyword: '',
  sortBy: 'overallRating',
  sortDirection: 'DESC',
  ratingFilters: {
    overallRating: 0,
    cleanlinessRating: 0,
    convenienceRating: 0,
    friendlinessRating: 0
  }
});

// 圖片上傳相關
const selectedFiles = ref([]);
const previewImages = ref([]);
const uploading = ref(false);
const maxFiles = 5;

// 打開評價表單
const openReviewForm = () => {
  resetForm();
  showReviewForm.value = true;
  nextTick(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });
};

// 關閉評價表單
const closeReviewForm = () => {
  showReviewForm.value = false;
  resetForm();
};

// 重置表單
const resetForm = () => {
  form.reviewText = '';
  form.cleanlinessRating = 0;
  form.convenienceRating = 0;
  form.friendlinessRating = 0;
  form.pros = '';
  form.cons = '';
  form.imageUrls = [];
  
  selectedFiles.value = [];
  previewImages.value = [];
  uploadProgress.value = 0;
  uploadMessage.value = '';
  uploadSuccess.value = false;
  
  if (fileInput.value) {
    fileInput.value.value = '';
  }
  
  previewImages.value.forEach(url => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  });
};

// 更新臨時關鍵字
const updateKeyword = (e) => {
  searchKeyword.value = e.target.value;
};

// 處理排序欄位變更
const handleSortFieldChange = (field) => {
  searchParams.sortBy = field;
  
  // 當排序欄位為發表時間時，預設為最新優先
  if (field === 'createdAt') {
    searchParams.sortDirection = 'DESC';
  }
  
  // 執行搜尋
  executeSearch();
};

// 獲取排序方向的文字描述
const getSortDirectionText = (direction) => {
  if (searchParams.sortBy === 'createdAt') {
    return direction === 'DESC' ? '最新優先' : '最舊優先';
  } else {
    return direction === 'DESC' ? '由高至低' : '由低至高';
  }
};

// 處理排序方向變更
const handleSortDirectionChange = (direction) => {
  // 設置新的排序方向
  searchParams.sortDirection = direction;
  
  // 執行搜尋
  executeSearch();
};

// 處理星星數篩選變更
const handleRatingFilterChange = (field, rating) => {
  console.log(`變更 ${field} 評分篩選: ${rating}`);
  
  if (searchParams.ratingFilters[field] === rating) {
    // 如果點擊了已選中的星數，則取消選擇
    searchParams.ratingFilters[field] = 0;
    console.log(`取消 ${field} 評分篩選`);
  } else {
    // 否則，選中該星數
    searchParams.ratingFilters[field] = rating;
    console.log(`設定 ${field} 評分篩選為 ${rating} 星`);
  }
  
  // 不立即執行搜尋，等待「執行搜尋」按鈕點擊
  // executeSearch();
};

// 執行搜尋
const executeSearch = () => {
  searchKeyword.value = searchKeyword.value.trim();
  searchParams.keyword = searchKeyword.value;
  
  if (reviewListRef.value) {
    reviewListRef.value.applySearchFilters();
  }
};

// 清除所有篩選
const clearAllFilters = () => {
  searchKeyword.value = '';
  searchParams.keyword = '';
  searchParams.ratingFilters = {
    overallRating: 0,
    cleanlinessRating: 0,
    convenienceRating: 0,
    friendlinessRating: 0
  };
  
  if (reviewListRef.value) {
    reviewListRef.value.clearSearchFilters();
  }
};

// 設置評分
const setRating = (field, value) => {
  form[field] = value;
};

// 處理文件選擇
const handleFileSelection = (event) => {
  selectedFiles.value = Array.from(event.target.files || []);
  
  if (selectedFiles.value.length > maxFiles) {
    alert(`最多只能上傳 ${maxFiles} 張圖片！`);
    selectedFiles.value = selectedFiles.value.slice(0, maxFiles);
    
    // 重置文件輸入
    if (fileInput.value) {
      fileInput.value.value = '';
    }
  }
  
  // 生成預覽
  previewImages.value = [];
  selectedFiles.value.forEach(file => {
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImages.value.push(e.target.result);
    };
    reader.readAsDataURL(file);
  });
};

// 移除預覽圖片
const removePreviewImage = (index) => {
  if (previewImages.value[index] && previewImages.value[index].startsWith('blob:')) {
    URL.revokeObjectURL(previewImages.value[index]);
  }
  
  previewImages.value.splice(index, 1);
  selectedFiles.value.splice(index, 1);
};

// 處理評價提交函數
const handleReviewSubmit = async () => {
  // 1. 驗證必填項
  if (!form.reviewText.trim()) {
    alert('請輸入評價內容');
    return;
  }
  
  if (form.cleanlinessRating === 0) {
    alert('請給予清潔度評分');
    return;
  }
  
  if (form.convenienceRating === 0) {
    alert('請給予便利性評分');
    return;
  }
  
  if (form.friendlinessRating === 0) {
    alert('請給予友善度評分');
    return;
  }
  
  // 2. 設置上傳狀態
  uploading.value = true;
  uploadProgress.value = 0;
  uploadMessage.value = '準備提交評價...';
  
  try {
    // 計算總體評分（如果未設置）
    if (!form.overallRating || form.overallRating === 0) {
      const avgRating = (form.cleanlinessRating + form.convenienceRating + form.friendlinessRating) / 3;
      form.overallRating = Math.round(avgRating * 10) / 10; // 四捨五入到小數點一位
    }
    
    // 3. 準備評價數據，不包含圖片
    const reviewData = {
      campSiteId: form.campSiteId,
      userId: form.userId,
      userName: form.userName,
      reviewText: form.reviewText,
      overallRating: form.overallRating,
      cleanlinessRating: form.cleanlinessRating,
      convenienceRating: form.convenienceRating,
      friendlinessRating: form.friendlinessRating,
      pros: form.pros || '',
      cons: form.cons || ''
    };
    
    uploadProgress.value = 20;
    uploadMessage.value = '提交評價資料中...';
    
    // 4. 發送評價到後端 - 確保使用正確的 API 路徑
    const reviewsApiUrl = '/api/reviews';
    console.log('提交評價數據:', reviewData);
    
    const response = await axios.post(reviewsApiUrl, reviewData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    uploadProgress.value = 50;
    console.log('評價提交成功:', response.data);
    
    // 5. 如果有圖片且成功提交評價，上傳圖片並關聯到評價 ID
    if (selectedFiles.value.length > 0 && response.data && response.data.id) {
      uploadMessage.value = '評價已提交，開始上傳圖片...';
      const reviewId = response.data.id;
      console.log(`上傳 ${selectedFiles.value.length} 張圖片到評價 ID: ${reviewId}`);
      
      // 6. 創建 FormData 用於圖片上傳
      const formData = new FormData();
      for (const file of selectedFiles.value) {
        formData.append('files', file);
      }
      formData.append('reviewId', reviewId);
      
      // 7. 上傳圖片
      try {
        const imageResponse = await axios.post('/api/review-images/upload-multiple', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (progressEvent) => {
            // 更新上傳進度 (50% - 90%)
            const percent = Math.round(
              (progressEvent.loaded * 40 / progressEvent.total) + 50
            );
            uploadProgress.value = percent;
          }
        });
        
        // 8. 更新評價數據以包含圖片 URL
        if (imageResponse.data && Array.isArray(imageResponse.data)) {
          response.data.imageUrls = imageResponse.data.map(img => img.imageUrl || img.url);
        }
        
        console.log('圖片上傳成功:', imageResponse.data);
      } catch (imageError) {
        console.error('圖片上傳失敗:', imageError);
        uploadMessage.value = '評價已提交，但圖片上傳失敗。';
      }
    }
    
    // 9. 完成處理
    uploadProgress.value = 100;
    uploadMessage.value = '評價發布成功！';
    uploadSuccess.value = true;
    
    setTimeout(() => {
      alert('評價發布成功！');
      
      // 隱藏表單並重置
      showReviewForm.value = false;
      resetForm();
      
      // 更新評價列表
      if (reviewListRef.value) {
        if (typeof reviewListRef.value.addNewReview === 'function' && response?.data) {
          reviewListRef.value.addNewReview(response.data);
        } else if (typeof reviewListRef.value.refreshReviews === 'function') {
          reviewListRef.value.refreshReviews();
        } else {
          // 嘗試直接調用 fetchReviews 方法
          reviewListRef.value.fetchReviews?.();
        }
      }
    }, 500);
    
  } catch (error) {
    console.error('評價提交失敗:', error);
    console.log('錯誤響應:', error.response?.data);
    
    // 如果 API 失敗，顯示錯誤訊息
    uploadMessage.value = '評價提交失敗: ' + (error.response?.data?.message || error.message || '未知錯誤');
    uploadSuccess.value = false;
    alert('評價提交失敗，請稍後再試。錯誤: ' + (error.response?.data?.message || error.message));
  } finally {
    uploading.value = false;
  }
};

// 應用加載時初始化
onMounted(() => {
  sessionStorage.setItem('API_BASE_PATH', '/api/reviews');
  testBackendConnection();
  window.addEventListener('scroll', handleScroll);
});

// 處理滾動事件
const handleScroll = () => {
  if (document.querySelectorAll('.app-container').length > 1) {
    const containers = document.querySelectorAll('.app-container');
    for (let i = 1; i < containers.length; i++) {
      containers[i].remove();
    }
  }
};

// 組件卸載前清理
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  
  previewImages.value.forEach(url => {
    if (url.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  });
});
</script>

<style>
:root {
  --primary-color: #2A5D3C;
  --secondary-color: #47845D;
  --accent-color: #6EAB7F;
  --light-accent: #9ACCA8;
  --background-color: #E8F4EC;
  --text-color: #1E432E;
  --text-light: #356648;
  --text-lighter: #5B8F6D;
  --border-color: #BFE0C9;
  --selected-color: #2A5D3C;
  --star-color: #FFCB45;
  --hover-color: #D7E9DE;
  --white: #FFFFFF;
  --shadow-sm: 0 2px 6px rgba(40, 80, 60, 0.08);
  --shadow-md: 0 4px 12px rgba(40, 80, 60, 0.12);
  --shadow-lg: 0 8px 24px rgba(40, 80, 60, 0.15);
  --gradient-primary: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  --gradient-accent: linear-gradient(135deg, var(--accent-color), var(--light-accent));
}

html, body {
  margin: 0;
  padding: 0;
  font-family: 'Microsoft JhengHei', 'PingFang TC', 'Heiti TC', sans-serif;
  background-color: var(--background-color);
  color: var(--text-color);
  line-height: 1.6;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

#app {
  width: 100%;
  height: 100%;
}

.app-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--background-color);
  overflow-x: hidden;
  position: relative;
}

header {
  background-color: transparent;
  padding: 30px 0 20px;
  margin-bottom: 30px;
  position: relative;
}

header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color) 20%, var(--border-color) 80%, transparent);
}

.wrapper {
  text-align: center;
  padding: 10px 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 嚴格修改App.vue中的h1標籤樣式，使營地評價系統標題呈現墨綠色粗體 */

/* 針對App.vue的標題樣式加強版 - 使用更精確的選擇器和!important */

/* 直接針對header內的h1標籤 */
header h1 {
  font-weight: 900 !important; /* 最粗的字體粗細，添加!important確保不被覆蓋 */
  font-size: 3rem !important;
  color: #1E432E !important; /* 最深墨綠色，使用!important確保優先級 */
  letter-spacing: 2px !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15) !important; /* 增加陰影效果並確保應用 */
}

/* 如果需要，也可以加強下劃線效果 */
header h1::after {
  background: linear-gradient(135deg, #2A5D3C, #47845D) !important;
  height: 4px !important; /* 增加下劃線高度 */
}

/* 添加額外媒體查詢確保在不同屏幕大小下都能正確顯示 */
@media (max-width: 768px) {
  header h1 {
    font-weight: 900 !important;
    color: #1E432E !important;
  }
}

.test-connection {
  margin: 25px 0 10px;
}

.test-connection button {
  padding: 10px 20px;
  background: var(--gradient-primary);
  color: var(--white);
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.test-connection button:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.test-connection span {
  margin-left: 12px;
  padding: 8px 15px;
  border-radius: 6px;
  display: inline-block;
  font-weight: 500;
}

.test-connection .success {
  background-color: rgba(80, 200, 120, 0.15);
  color: #2A753B;
  border: 1px solid rgba(80, 200, 120, 0.3);
}

.test-connection .error {
  background-color: rgba(220, 53, 69, 0.15);
  color: #d32f2f;
  border: 1px solid rgba(220, 53, 69, 0.3);
}

main {
  width: 100%;
  margin: 0 auto;
  padding: 0;
  flex: 1;
}

.app-layout {
  display: flex;
  width: 100%;
  gap: 25px;
  padding: 0 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-sidebar {
  width: 300px;
  flex-shrink: 0;
  background-color: var(--white);
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  padding: 25px;
  height: fit-content;
  border: 1px solid var(--border-color);
  position: sticky;
  top: 20px;
}

.content-area {
  flex-grow: 1;
  min-width: 0;
}

.search-box {
  background-color: var(--white);
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 25px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: box-shadow 0.3s ease;
}

.search-box:hover {
  box-shadow: var(--shadow-md);
}

.search-box h3 {
  color: var(--text-color);
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 17px;
  font-weight: 600;
  position: relative;
  padding-bottom: 10px;
}

.search-box h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: var(--gradient-accent);
  border-radius: 2px;
}

.input-group {
  position: relative;
}

.input-group input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 15px;
  background-color: var(--white);
  color: var(--text-color);
  transition: all 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(110, 171, 127, 0.15);
}

.filter-section {
  background-color: var(--white);
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 25px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  transition: box-shadow 0.3s ease;
}

.filter-section:hover {
  box-shadow: var(--shadow-md);
}

.filter-section h3 {
  color: var(--text-color);
  margin-top: 0;
  margin-bottom: 18px;
  font-size: 17px;
  font-weight: 600;
  position: relative;
  padding-bottom: 10px;
}

.filter-section h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: var(--gradient-accent);
  border-radius: 2px;
}

.rating-filters {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.filter-group, .sort-group {
  margin-bottom: 18px;
}

.filter-group label, .sort-group label {
  display: block;
  font-weight: 500;
  color: var(--text-color);
  margin-bottom: 10px;
  font-size: 15px;
}

.star-buttons {
  display: flex;
  gap: 6px;
}

.star-button {
  flex: 1;
  padding: 8px 0;
  background-color: var(--white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-color);
  transition: all 0.2s;
  font-size: 14px;
  box-shadow: var(--shadow-sm);
}

.star-button.active {
  background: var(--gradient-primary);
  color: var(--white);
  border-color: var(--primary-color);
  box-shadow: 0 2px 6px rgba(42, 93, 60, 0.25);
}

.star-button:hover:not(.active) {
  background-color: var(--hover-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.sort-options {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.option-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.direction-buttons {
  grid-template-columns: repeat(2, 1fr);
}

.option-buttons button {
  padding: 10px;
  background-color: var(--white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-color);
  transition: all 0.3s;
  font-size: 14px;
  white-space: nowrap;
  box-shadow: var(--shadow-sm);
}

.option-buttons button.active {
  background: var(--gradient-primary);
  color: var(--white);
  border-color: var(--primary-color);
  box-shadow: 0 2px 6px rgba(42, 93, 60, 0.25);
}

.option-buttons button:hover:not(.active) {
  background-color: var(--hover-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.filter-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}

.search-button {
  padding: 14px 0;
  width: 100%;
  background: var(--gradient-primary);
  color: var(--white);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: var(--shadow-sm);
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.clear-button {
  padding: 12px 0;
  width: 100%;
  background-color: var(--white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s;
  box-shadow: var(--shadow-sm);
}

.clear-button:hover {
  background-color: var(--hover-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.add-review-section {
  margin-bottom: 30px;
  text-align: right;
}

.add-review-btn {
  padding: 14px 28px;
  background: var(--gradient-primary);
  color: var(--white);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--shadow-md);
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.add-review-btn::before {
  content: '+';
  display: inline-block;
  font-size: 20px;
  font-weight: 700;
}

.add-review-btn:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.review-form {
  background-color: var(--white);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.review-form h2 {
  margin-top: 0;
  margin-bottom: 25px;
  color: var(--text-color);
  font-weight: 700;
  font-size: 22px;
  position: relative;
  padding-bottom: 15px;
}

.review-form h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: var(--gradient-accent);
  border-radius: 3px;
}

.rating-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.rating-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rating-group label {
  font-weight: 600;
  color: var(--text-color);
  font-size: 16px;
}

.star-rating {
  display: flex;
  align-items: center;
}

.review-form .star-rating .star {
  font-size: 26px;
  cursor: pointer; 
  margin-right: 6px;
  transition: transform 0.3s, color 0.3s;
  color: #e0e0e0;
  display: inline-block;
}

.review-form .star-rating .star.filled {
  color: var(--star-color);
  text-shadow: 0 0 3px rgba(255, 203, 69, 0.5);
  display: inline-block;
}

.review-form .star-rating .star:hover {
  transform: scale(1.15);
}

.rating-value {
  margin-left: 10px;
  font-weight: bold;
  color: var(--primary-color);
  background-color: rgba(110, 171, 127, 0.1);
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: var(--text-color);
  font-size: 16px;
}

.text-muted {
  color: var(--text-lighter);
  font-size: 0.9em;
  font-weight: normal;
}

textarea, .form-control {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  resize: vertical;
  font-family: inherit;
  font-size: 15px;
  background-color: var(--white);
  color: var(--text-color);
  transition: all 0.3s ease;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}

textarea:focus, .form-control:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(110, 171, 127, 0.15);
}

.pros-cons-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  margin-bottom: 25px;
}

.file-upload-container {
  margin-bottom: 15px;
}

.file-upload-container input[type="file"] {
  padding: 12px;
  border: 2px dashed var(--border-color);
  border-radius: 10px;
  background-color: rgba(191, 224, 201, 0.1);
  transition: all 0.3s ease;
  width: 100%;
}

.file-upload-container input[type="file"]:hover {
  border-color: var(--accent-color);
  background-color: rgba(191, 224, 201, 0.2);
}

.image-previews {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.preview-item {
  position: relative;
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.preview-item:hover {
  transform: scale(1.03);
  box-shadow: var(--shadow-md);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #dc3545;
  color: #dc3545;
  font-size: 16px;
  line-height: 1;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.remove-image:hover {
  background-color: #dc3545;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 2px 6px rgba(220, 53, 69, 0.4);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 10px;
}

.submit-button, .cancel-button {
  padding: 14px 28px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.submit-button {
  background: var(--gradient-primary);
  color: var(--white);
  border: none;
  box-shadow: var(--shadow-sm);
}

.submit-button:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.submit-button:disabled {
  background: linear-gradient(135deg, #95a99e, #a8b8b0);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.cancel-button {
  background-color: var(--white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.cancel-button:hover {
  background-color: var(--hover-color);
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.star-icon, .bi-star-fill, .rating-star, .star.filled {
  color: var(--star-color) !important;
}

.star-empty {
  color: #e0e0e0 !important;
}

.upload-progress {
  margin-top: 20px;
  padding: 15px;
  background-color: rgba(110, 171, 127, 0.1);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.progress-bar {
  height: 10px;
  background-color: #e9ecef;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: var(--text-color);
  text-align: center;
}

.upload-message {
  margin-top: 15px;
  padding: 12px 15px;
  border-radius: 8px;
  font-weight: 500;
}

.upload-message.success {
  background-color: rgba(40, 167, 69, 0.1);
  border: 1px solid rgba(40, 167, 69, 0.3);
  color: #28a745;
}

.upload-message.error {
  background-color: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.3);
  color: #dc3545;
}

@media (max-width: 1200px) {
  .app-layout {
    padding: 0 15px;
  }
}

@media (max-width: 992px) {
  .app-layout {
    flex-direction: column;
  }
  
  .filter-sidebar {
    width: 100%;
    margin-bottom: 25px;
    position: static;
  }
  
  .option-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .rating-section, .pros-cons-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  h1 {
    font-size: 2.5rem;
  }
  
  .review-form {
    padding: 20px;
  }
  
  .pros-cons-section, .rating-section {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}

@media (max-width: 576px) {
  .option-buttons {
    grid-template-columns: 1fr;
  }
  
  .submit-button, .cancel-button {
    padding: 12px 20px;
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .image-previews {
    grid-template-columns: repeat(2, 1fr);
  }
  
  h1 {
    font-size: 2rem;
  }  
}
</style>