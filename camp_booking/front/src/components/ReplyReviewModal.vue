<!-- src/components/ReplyReviewModal.vue (修改版) -->
<template>
    <div class="modal-backdrop" v-if="show" @click.stop="$emit('close')">
      <div class="reply-modal" @click.stop>
        <div class="modal-header">
          <h2 class="text-forest">回覆評價</h2>
          <button type="button" class="close-btn" @click.stop="$emit('close')">&times;</button>
        </div>
        
        <div class="modal-body">
          <!-- 評價預覽 - 美化版 -->
          <div class="review-preview-container" v-if="review">
            <h3 class="preview-title">
              <i class="bi bi-chat-quote me-2"></i>原始評價
            </h3>
            
            <div class="review-preview">
              <div class="user-info">
                <div class="user-avatar">{{ review.userName ? review.userName.charAt(0) : 'U' }}</div>
                <div class="user-details">
                  <h3 class="text-forest">{{ review.userName || '匿名用戶' }}</h3>
                  <p class="review-date text-forest-light">{{ formatDate(review.createdAt) }}</p>
                </div>
                <div class="review-rating">
                  <span v-for="i in 5" :key="i" class="star" :class="i <= review.overallRating ? 'filled' : 'empty'">★</span>
                </div>
              </div>
              
              <p class="review-text text-forest">{{ review.reviewText }}</p>
              
              <!-- 評分詳情 -->
              <div class="ratings-detail">
                <div class="rating-item">
                  <span class="rating-label">清潔度:</span>
                  <div class="stars-display">
                    <span v-for="i in 5" :key="i" class="small-star" :class="i <= review.cleanlinessRating ? 'filled' : 'empty'">★</span>
                  </div>
                </div>
                <div class="rating-item">
                  <span class="rating-label">便利性:</span>
                  <div class="stars-display">
                    <span v-for="i in 5" :key="i" class="small-star" :class="i <= review.convenienceRating ? 'filled' : 'empty'">★</span>
                  </div>
                </div>
                <div class="rating-item">
                  <span class="rating-label">友善度:</span>
                  <div class="stars-display">
                    <span v-for="i in 5" :key="i" class="small-star" :class="i <= review.friendlinessRating ? 'filled' : 'empty'">★</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 回覆表單 - 美化版 -->
          <div class="reply-form-container">
            <h3 class="reply-title">
              <i class="bi bi-chat-left-text me-2"></i>您的回覆
            </h3>
            
            <form @submit.prevent="submitReply" id="replyForm">
              <div class="form-group">
                <textarea 
                  id="replyText" 
                  v-model="replyText" 
                  rows="6" 
                  placeholder="請輸入您的回覆內容..."
                  required
                  class="reply-textarea"
                ></textarea>
                
                <div class="reply-tips">
                  <i class="bi bi-lightbulb me-2"></i>
                  <div>
                    <p class="tip-title">營主回覆小提示:</p>
                    <p class="tip-content">感謝顧客的回饋並提供解決方案，能夠大幅提升顧客滿意度。</p>
                  </div>
                </div>
              </div>
              
              <div class="form-actions">
                <button type="button" class="cancel-button text-forest" @click.stop="$emit('close')">
                  <i class="bi bi-x-circle me-1"></i>取消
                </button>
                <button 
                  type="submit" 
                  class="submit-button" 
                  :disabled="submitting"
                >
                  <span v-if="submitting">
                    <span class="spinner"></span>
                    提交中...
                  </span>
                  <span v-else>
                    <i class="bi bi-send me-1"></i>提交回覆
                  </span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import { ref, defineProps, defineEmits, watch } from 'vue';
  import axios from 'axios';
  
  export default {
    name: 'ReplyReviewModal',
    props: {
      show: {
        type: Boolean,
        default: false
      },
      review: {
        type: Object,
        required: true
      }
    },
    emits: ['close', 'submit'],
    setup(props, { emit }) {
      const submitting = ref(false);
      const replyText = ref('');
      
      // 監聽 show 屬性變化，確保每次開啟模態框時重置表單
      watch(() => props.show, (newVal) => {
        if (newVal) {
          // 如果模態框重新開啟，清空表單
          replyText.value = '';
        }
      });
      
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
      
      // 簡化的回覆提交函數 - 只使用正確的 PUT 方法
      const submitReply = async () => {
        if (!replyText.value.trim()) {
          alert('請輸入回覆內容');
          return;
        }
        
        submitting.value = true;
        
        try {
          // 準備回覆數據
          const replyData = {
            replyText: replyText.value.trim()
          };
          
          // 使用PUT方法提交回覆 - 與後端API匹配
          const response = await axios.put(
            `/api/reviews/${props.review.id}/reply`,
            replyData,
            {
              headers: {
                'Content-Type': 'application/json'
              },
              timeout: 5000
            }
          );
          
          console.log("回覆提交成功:", response.data);
          
          // 發送事件通知父組件更新UI
          emit('submit', {
            reviewId: props.review.id,
            replyText: replyText.value.trim()
          });
          
          // 關閉模態框
          emit('close');
        } catch (error) {
          console.error('回覆評價失敗:', error);
          alert('回覆提交失敗，請稍後再試');
        } finally {
          submitting.value = false;
        }
      };
      
      return {
        formatDate,
        replyText,
        submitting,
        submitReply
      };
    }
  }
  </script>
  
  <style scoped>
  /* 顏色變數 */
  :root {
    --forest-dark: #1E432E;
    --forest-medium: #356648;
    --forest-light: #5B8F6D;
  }
  
  /* 墨綠色文字通用樣式 */
  .text-forest {
    color: var(--forest-dark, #1E432E) !important;
  }
  
  .text-forest-light {
    color: var(--forest-medium, #356648) !important;
  }
  
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    backdrop-filter: blur(2px);
  }
  
  .reply-modal {
    width: 90%;
    max-width: 700px;
    background-color: white;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    overflow: hidden;
    animation: modalFadeIn 0.3s ease-out;
  }
  
  @keyframes modalFadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 25px;
    border-bottom: 1px solid #BFE0C9;
    background-color: #E8F4EC;
  }
  
  .modal-header h2 {
    margin: 0;
    color: #2A5D3C;
    font-size: 22px;
    font-weight: 600;
  }
  
  .close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .close-btn:hover {
    color: #333;
    transform: scale(1.1);
  }
  
  .modal-body {
    padding: 25px;
  }
  
  /* 美化評價預覽 */
  .review-preview-container {
    margin-bottom: 25px;
  }
  
  .preview-title, .reply-title {
    color: #1E432E;
    font-size: 18px;
    margin-top: 0;
    margin-bottom: 15px;
    font-weight: 600;
  }
  
  .review-preview {
    background-color: #f8f9fa;
    border-radius: 10px;
    padding: 20px;
    border: 1px solid #e9ecef;
    margin-bottom: 20px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
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
    margin-right: 15px;
  }
  
  .user-details {
    flex-grow: 1;
  }
  
  .user-details h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
  
  .review-date {
    margin: 0;
    font-size: 14px;
  }
  
  .review-rating {
    display: flex;
    align-items: center;
  }
  
  .star {
    font-size: 18px;
    color: #e0e0e0;
    margin-left: 2px;
  }
  
  .star.filled {
    color: #FFCB45;
  }
  
  .review-text {
    margin-bottom: 15px;
    line-height: 1.6;
    white-space: pre-line;
  }
  
  /* 評分詳情 */
  .ratings-detail {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    background-color: #fff;
    border-radius: 8px;
    padding: 12px;
    border: 1px dashed #dee2e6;
  }
  
  .rating-item {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-right: 15px;
  }
  
  .rating-label {
    font-weight: 500;
    color: #1E432E;
    font-size: 14px;
  }
  
  .stars-display {
    display: flex;
  }
  
  .small-star {
    font-size: 14px;
    color: #e0e0e0;
  }
  
  .small-star.filled {
    color: #FFCB45;
  }
  
  /* 美化回覆表單 */
  .reply-form-container {
    background-color: #fff;
  }
  
  .form-group {
    margin-bottom: 20px;
  }
  
  .reply-textarea {
    width: 100%;
    padding: 15px;
    border: 1px solid #BFE0C9;
    border-radius: 10px;
    resize: vertical;
    font-family: inherit;
    font-size: 15px;
    color: #1E432E;
    transition: all 0.3s ease;
    min-height: 120px;
    margin-bottom: 15px;
    background-color: #f8f9fa;
  }
  
  .reply-textarea:focus {
    outline: none;
    border-color: #6EAB7F;
    box-shadow: 0 0 0 3px rgba(110, 171, 127, 0.15);
    background-color: #fff;
  }
  
  .reply-tips {
    background-color: #E8F4EC;
    border-radius: 10px;
    padding: 15px;
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }
  
  .reply-tips i {
    font-size: 22px;
    color: #2A5D3C;
  }
  
  .tip-title {
    margin: 0 0 5px 0;
    font-weight: 600;
    color: #1E432E;
  }
  
  .tip-content {
    margin: 0;
    color: #356648;
    font-size: 14px;
    line-height: 1.5;
  }
  
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    margin-top: 20px;
  }
  
  .submit-button, .cancel-button {
    padding: 12px 24px;
    border-radius: 10px;
    font-weight: 600;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .submit-button {
    background: linear-gradient(135deg, #2A5D3C, #47845D);
    color: white;
    border: none;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    min-width: 140px;
  }
  
  .submit-button:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
  
  .submit-button:disabled {
    background: #aaa;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }
  
  .cancel-button {
    background-color: white;
    color: #333;
    border: 1px solid #ddd;
  }
  
  .cancel-button:hover {
    background-color: #f8f9fa;
  }
  
  .spinner {
    display: inline-block;
    width: 1em;
    height: 1em;
    border: 2px solid currentColor;
    border-right-color: transparent;
    border-radius: 50%;
    animation: spinner 0.75s linear infinite;
    margin-right: 8px;
  }
  
  @keyframes spinner {
    to { transform: rotate(360deg); }
  }
  
  @media (max-width: 576px) {
    .form-actions {
      flex-direction: column-reverse;
    }
    
    .submit-button, .cancel-button {
      width: 100%;
    }
  }
  </style>