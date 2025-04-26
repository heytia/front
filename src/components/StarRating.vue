<script setup>
import { computed } from 'vue';

const props = defineProps({
  rating: {
    type: Number,
    default: 0
  },
  maxRating: {
    type: Number,
    default: 5
  },
  readonly: {
    type: Boolean,
    default: true  // 默認為唯讀模式
  }
});

const emit = defineEmits(['update:rating']);

// 計算星星的數組
const stars = computed(() => {
  return Array.from({ length: props.maxRating }, (_, index) => index + 1);
});

// 選擇星星評分 - 只在非唯讀模式下才會觸發
const selectRating = (value) => {
  if (props.readonly) return;
  emit('update:rating', value);
};

// 獲取星星的類名
const getStarClass = (value) => {
  return value <= props.rating ? 'filled' : 'empty';
};
</script>

<template>
  <div class="star-rating" :class="{ 'readonly': readonly }">
    <span
      v-for="star in stars"
      :key="star"
      :class="['star', getStarClass(star)]"
      @click="selectRating(star)"
    >
      ★
    </span>
  </div>
</template>

<style scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
}

.star {
  font-size: 18px;
  color: #FFD700; /* 空星星也使用金黃色 */
  opacity: 0.5; /* 空星星使用透明度區分 */
  margin-right: 3px;
  user-select: none; /* 防止文字被選中 */
}

.star.filled {
  color: #FFD700; /* 填充星星使用金黃色 */
  opacity: 1; /* 填充星星完全不透明 */
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.3); /* 添加陰影使星星更突出 */
}

/* 非唯讀模式下才有交互效果 */
.star-rating:not(.readonly) .star {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.star-rating:not(.readonly) .star:hover {
  transform: scale(1.1);
}

/* 唯讀模式樣式 */
.readonly .star {
  cursor: default;
}
</style>