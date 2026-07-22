<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth'
import { onMounted } from 'vue'

const auth = useAuthStore()

onMounted(async () => {
  if (auth.isLoggedIn) {
    try { await auth.fetchUserInfo() }
    catch { auth.logout() }
  }
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif; }
</style>
