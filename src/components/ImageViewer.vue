<template>
    <div class="image-viewer" v-if="visible" @click="closeViewer">
      <div class="image-viewer-content" @click.stop>
        <!-- 關閉按鈕 -->
        <button class="close-button" @click="closeViewer" title="關閉">
          <i class="bi bi-x-lg"></i>
        </button>
        
        <!-- 導航按鈕 -->
        <button 
          class="nav-button prev-button" 
          @click="prevImage" 
          :disabled="currentIndex === 0" 
          title="上一張"
        >
          <i class="bi bi-chevron-left"></i>
        </button>
        
        <!-- 圖片 -->
        <div class="viewer-image-container">
          <img 
            :src="processedCurrentImage" 
            alt="評價圖片" 
            class="viewer-image" 
            @load="imageLoaded = true" 
            @error="handleImageError"
            :class="{'d-none': !imageLoaded}"
          />
          <div v-if="!imageLoaded" class="image-loading">
            <div class="spinner-border text-light" role="status">
              <span class="visually-hidden">載入中...</span>
            </div>
          </div>
        </div>
        
        <button 
          class="nav-button next-button" 
          @click="nextImage" 
          :disabled="currentIndex >= images.length - 1" 
          title="下一張"
        >
          <i class="bi bi-chevron-right"></i>
        </button>
        
        <!-- 圖片計數器 -->
        <div class="image-counter">
          {{ currentIndex + 1 }} / {{ images.length }}
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
  
  // 定義 props
  const props = defineProps({
    images: {
      type: Array,
      default: () => []
    },
    startIndex: {
      type: Number,
      default: 0
    },
    visible: {
      type: Boolean,
      default: false
    }
  });
  
  // 定義事件
  const emit = defineEmits(['close']);
  
  // 當前顯示圖片的索引
  const currentIndex = ref(props.startIndex || 0);
  // 圖片載入狀態
  const imageLoaded = ref(false);
  
  // 當 visible 變更時，重置圖片載入狀態
  watch(() => props.visible, (newValue) => {
    if (newValue) {
      document.body.style.overflow = 'hidden'; // 防止背景滾動
      imageLoaded.value = false;
    } else {
      document.body.style.overflow = '';
    }
  });
  
  // 當圖片索引變更時，重置圖片載入狀態
  watch(() => currentIndex.value, () => {
    imageLoaded.value = false;
  });
  
  // 當索引變更時監聽
  watch(() => props.startIndex, (newIndex) => {
    currentIndex.value = newIndex;
  });
  
  // 處理圖片 URL
  const processImageUrl = (url) => {
    if (!url) return '';
    
    // 如果已經是完整 URL（包含 http 或 https）
    if (url.startsWith('http')) {
      return url;
    }
    
    // 如果是相對路徑，需要確保路徑正確
    return url.startsWith('/') ? url : `/${url}`;
  };
  
  // 處理圖片URL
  const processedCurrentImage = computed(() => {
    if (!props.images || props.images.length === 0 || currentIndex.value >= props.images.length) {
      return '';
    }
    
    const url = props.images[currentIndex.value];
    return processImageUrl(url);
  });
  
  // 處理圖片載入錯誤
  const handleImageError = (event) => {
    console.error('圖片載入失敗:', processedCurrentImage.value);
    event.target.src = '/img/image-placeholder.jpg';
    imageLoaded.value = true;
  };
  
  // 關閉查看器
  const closeViewer = () => {
    emit('close');
    document.body.style.overflow = ''; // 恢復背景滾動
  };
  
  // 上一張圖片
  const prevImage = () => {
    if (currentIndex.value > 0) {
      currentIndex.value--;
    }
  };
  
  // 下一張圖片
  const nextImage = () => {
    if (currentIndex.value < props.images.length - 1) {
      currentIndex.value++;
    }
  };
  
  // 鍵盤事件處理
  const handleKeyDown = (event) => {
    if (!props.visible) return;
    
    switch (event.key) {
      case 'Escape':
        closeViewer();
        break;
      case 'ArrowLeft':
        prevImage();
        break;
      case 'ArrowRight':
        nextImage();
        break;
    }
  };
  
  // 掛載時添加鍵盤事件監聽
  onMounted(() => {
    document.addEventListener('keydown', handleKeyDown);
  });
  
  // 卸載時移除鍵盤事件監聽
  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeyDown);
    document.body.style.overflow = ''; // 確保恢復背景滾動
  });
  </script>
  
  <style scoped>
  .image-viewer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    animation: fade-in 0.3s ease;
  }
  
  @keyframes fade-in {
    from { opacity: 0; }
    to { opacity: 1; }
  }
  
  .image-viewer-content {
    position: relative;
    max-width: 90%;
    max-height: 90%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .viewer-image-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    position: relative;
  }
  
  .viewer-image {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
    animation: image-zoom-in 0.3s ease;
  }
  
  @keyframes image-zoom-in {
    from { transform: scale(0.9); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
  }
  
  .image-loading {
    position: absolute;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
  }
  
  .close-button {
    position: absolute;
    top: -40px;
    right: 0;
    background: none;
    border: none;
    color: white;
    font-size: 2rem;
    cursor: pointer;
    z-index: 2;
    opacity: 0.8;
    transition: opacity 0.3s, transform 0.3s;
  }
  
  .close-button:hover {
    opacity: 1;
    transform: scale(1.1);
  }
  
  .nav-button {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    font-size: 1.5rem;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background 0.3s;
    z-index: 2;
  }
  
  .nav-button:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.4);
  }
  
  .nav-button:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
  
  .prev-button {
    left: -80px;
  }
  
  .next-button {
    right: -80px;
  }
  
  .image-counter {
    position: absolute;
    bottom: -40px;
    left: 50%;
    transform: translateX(-50%);
    color: white;
    background: rgba(0, 0, 0, 0.6);
    padding: 5px 15px;
    border-radius: 20px;
    font-size: 0.9rem;
  }
  
  /* 響應式調整 */
  @media (max-width: 992px) {
    .nav-button {
      width: 40px;
      height: 40px;
      font-size: 1.2rem;
    }
    
    .prev-button {
      left: -50px;
    }
    
    .next-button {
      right: -50px;
    }
  }
  
  @media (max-width: 768px) {
    .prev-button {
      left: 10px;
    }
    
    .next-button {
      right: 10px;
    }
    
    .nav-button {
      background: rgba(0, 0, 0, 0.5);
    }
  }
  </style>