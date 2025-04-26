import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'

// 引入 Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css'

// API配置和基礎URL設置
// 自動檢測並設置API基礎URL
const setupApiBaseUrl = async () => {
  // 優先使用環境變數中的API基礎URL (如果在Vue項目中設置了)
  const envApiUrl = import.meta.env?.VITE_API_BASE_URL;
  
  if (envApiUrl) {
    console.log('使用環境變數中的API基礎URL:', envApiUrl);
    localStorage.setItem('API_BASE_URL', envApiUrl);
    sessionStorage.setItem('API_BASE_URL', envApiUrl);
    return envApiUrl;
  }
  
  // 否則嘗試使用當前域名
  const currentOrigin = window.location.origin;
  
  if (currentOrigin && currentOrigin !== 'null') {
    console.log('使用當前域名作為API基礎URL:', currentOrigin);
    localStorage.setItem('API_BASE_URL', currentOrigin);
    sessionStorage.setItem('API_BASE_URL', currentOrigin);
    return currentOrigin;
  }
};

// 自動檢測後端API連接 - 全局導出
window.testBackendConnection = async () => {
  const baseUrl = localStorage.getItem('API_BASE_URL') || await setupApiBaseUrl();
  
  const paths = [
    '/api/reviews',
    '/api/review-images/upload',
    '/api/health',
    '/health'
  ];
  
  let success = false;
  
  for (const path of paths) {
    try {
      const fullUrl = `${baseUrl}${path}`;
      console.log(`嘗試連接: ${fullUrl}`);
      
      const params = path.includes('reviews') ? '?page=0&size=1' : '';
      
      const response = await fetch(`${fullUrl}${params}`, { 
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        },
        mode: 'cors',
        cache: 'no-cache'
      });
      
      if (response.status >= 200 && response.status < 400) {
        console.log(`✅ API連接成功: ${fullUrl}`);
        
        if (path.includes('/api/reviews')) {
          sessionStorage.setItem('API_BASE_PATH', '/api/reviews');
        }
        
        success = true;
        return {
          success: true,
          message: `✅ 連接成功！路徑: ${path}`,
          baseUrl: baseUrl,
          path: path
        };
      }
    } catch (error) {
      console.log(`嘗試 ${path} 失敗:`, error.message);
    }
  }
  
  if (!success) {
    console.error('所有API路徑連接嘗試都失敗');
    return {
      success: false,
      message: '❌ API連接失敗！請確保後端服務正在運行',
      baseUrl: baseUrl
    };
  }
};

// 為了向後兼容，提供舊函數名的別名
window.testApiConnection = window.testBackendConnection;


// 在應用啟動時執行API連接測試
(async () => {
    try {
        await setupApiBaseUrl();
        const result = await window.testBackendConnection();
        console.log('API連接測試結果:', result);
        
        // 設置連接狀態，以便其他組件使用
        sessionStorage.setItem('API_CONNECTION_STATUS', result.success ? 'success' : 'failed');
    } catch (error) {
        console.error('API連接測試失敗:', error);
        sessionStorage.setItem('API_CONNECTION_STATUS', 'failed');
    }
})();

// 創建 Pinia 實例並使用持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 創建 Vue 應用
const app = createApp(App)

// 使用路由和狀態管理
app.use(router)
app.use(pinia)

// 掛載應用
app.mount('#app')