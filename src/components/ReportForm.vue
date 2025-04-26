<template>
    <form @submit.prevent="submitReport" class="report-form">
      <div class="mb-3">
        <label for="reportType" class="form-label">檢舉原因</label>
        <select 
          id="reportType" 
          class="form-select" 
          v-model="reportData.reportType"
          required
        >
          <option value="" disabled selected>請選擇檢舉原因</option>
          <option v-for="option in reportOptions" :key="option.id" :value="option.id">
            {{ option.label }}
          </option>
        </select>
      </div>
      
      <div class="mb-3">
        <label for="reason" class="form-label">詳細說明</label>
        <textarea 
          id="reason" 
          class="form-control reason-textarea" 
          v-model="reportData.reason" 
          rows="6" 
          placeholder="請提供更詳細的說明，例如為什麼這條評價違反規定，或者內容如何不實等..."
          required
        ></textarea>
      </div>
      
      <div class="alert alert-info notice-box">
        <i class="bi bi-info-circle me-2"></i>
        所有檢舉都會經過管理員審核，若情況屬實，此評價可能會被移除或修改。惡意檢舉可能導致帳號被限制功能。
      </div>
      
      <div class="d-flex justify-content-between">
        <button 
          type="button" 
          class="btn btn-outline-secondary cancel-btn"
          @click="$emit('cancel')"
        >
          <i class="bi bi-x-circle me-1"></i>取消
        </button>
        <button 
          type="submit" 
          class="btn btn-danger submit-btn" 
          :disabled="isSubmitting"
        >
          <i class="bi bi-send me-1"></i>
          <span v-if="isSubmitting">
            <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
            提交中...
          </span>
          <span v-else>提交檢舉</span>
        </button>
      </div>
    </form>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue';
  import { reportService } from '../services/reportService';
  import Swal from 'sweetalert2';
  
  const props = defineProps({
    reviewId: {
      type: [Number, String],
      required: true
    },
    userId: {
      type: [Number, String],
      default: null
    }
  });
  
  const emit = defineEmits(['cancel', 'success']);
  
  const isSubmitting = ref(false);
  
  // 檢舉選項
  const reportOptions = [
    { id: 1, label: '不實內容' },
    { id: 2, label: '冒犯性內容或語言' },
    { id: 3, label: '廣告或垃圾訊息' },
    { id: 4, label: '暴力或仇恨言論' },
    { id: 5, label: '侵犯隱私權' },
    { id: 6, label: '其他' }
  ];
  
  // 檢舉表單數據
  const reportData = reactive({
    reviewId: Number(props.reviewId),
    userId: props.userId,
    reportType: '',
    reason: '',
    status: 'pending'
  });
  
  // 提交檢舉
  const submitReport = async () => {
    if (!reportData.reportType || !reportData.reason.trim()) {
      Swal.fire({
        icon: 'error',
        title: '提交失敗',
        text: '請填寫所有必填欄位。',
        confirmButtonColor: '#3a8c5f'
      });
      return;
    }
    
    isSubmitting.value = true;
    
    try {
      await reportService.createReport(reportData);
      
      Swal.fire({
        icon: 'success',
        title: '檢舉已提交',
        text: '感謝您的回報，我們將盡快處理。',
        confirmButtonColor: '#3a8c5f'
      });
      
      emit('success');
    } catch (error) {
      console.error('檢舉提交失敗:', error);
      
      let errorMessage = '提交檢舉時發生錯誤，請稍後再試。';
      if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      }
      
      Swal.fire({
        icon: 'error',
        title: '提交失敗',
        text: errorMessage,
        confirmButtonColor: '#3a8c5f'
      });
    } finally {
      isSubmitting.value = false;
    }
  };
  </script>
  
  <style scoped>
  .report-form {
    background-color: white;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
  
  .form-label {
    font-weight: 500;
    color: #333333;
  }
  
  .form-select, .form-control {
    border-color: #e5e7eb;
    transition: all 0.3s ease;
  }
  
  .form-select:focus, .form-control:focus {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 0.25rem rgba(58, 140, 95, 0.25);
  }
  
  .reason-textarea {
    resize: vertical;
    min-height: 120px;
  }
  
  .notice-box {
    background-color: rgba(23, 162, 184, 0.1);
    border-left: 4px solid #17a2b8;
    color: #0c5460;
    border-radius: 4px;
  }
  
  .cancel-btn {
    transition: all 0.3s ease;
  }
  
  .cancel-btn:hover {
    background-color: #6c757d;
    color: white;
  }
  
  .submit-btn {
    background-color: #dc3545;
    border-color: #dc3545;
    transition: all 0.3s ease;
  }
  
  .submit-btn:hover:not(:disabled) {
    background-color: #c82333;
    border-color: #bd2130;
  }
  </style>