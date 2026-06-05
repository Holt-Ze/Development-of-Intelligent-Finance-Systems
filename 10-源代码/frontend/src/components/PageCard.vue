<template>
  <el-card shadow="hover">
    <template v-if="title" #header>
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <span>{{ title }}</span>
        <slot name="extra" />
      </div>
    </template>
    <slot />
  </el-card>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>
