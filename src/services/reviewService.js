// src/services/reviewService.js
import axios from 'axios';

// 獲取 API 基礎 URL
const getBaseUrl = () => {
  // 嘗試從多個來源獲取基礎 URL
  return (
    localStorage.getItem('API_BASE_URL') ||
    sessionStorage.getItem('API_BASE_URL') ||
    window.location.origin
  );
};

// 獲取 API 路徑
const getApiPath = () => {
  return sessionStorage.getItem('API_BASE_PATH') || '/api/reviews';
};

// 創建 API 實例
const createApiInstance = () => {
  const baseURL = getBaseUrl();
  const apiPath = getApiPath();
  
  return axios.create({
    baseURL: baseURL,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });
};

export const reviewService = {
  // 獲取評價列表
  getReviews: async (params = {}) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.get(apiPath, { params });
    return response.data;
  },
  
  // 獲取單個評價
  getReviewById: async (reviewId) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.get(`${apiPath}/${reviewId}`);
    return response.data;
  },
  
  // 創建新評價
  createReview: async (reviewData) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.post(apiPath, reviewData);
    return response.data;
  },
  
  // 更新評價
  updateReview: async (reviewId, reviewData) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.put(`${apiPath}/${reviewId}`, reviewData);
    return response.data;
  },
  
  // 刪除評價
  deleteReview: async (reviewId) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.delete(`${apiPath}/${reviewId}`);
    return response.data;
  },
  
  // 回覆評價
  replyReview: async (reviewId, replyData) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.post(`${apiPath}/${reviewId}/reply`, replyData);
    return response.data;
  },
  
  // 點讚/取消點讚評價
  toggleLike: async (reviewId, userId, status) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.post(`${apiPath}/${reviewId}/like`, {
      userId: userId,
      reviewId: reviewId,
      status: status
    });
    return response.data;
  },
  
  // 檢舉評價
  reportReview: async (reportData) => {
    const api = createApiInstance();
    const apiPath = getApiPath();
    const response = await api.post(`${apiPath}/report`, reportData);
    return response.data;
  },
  

  }


export default reviewService;