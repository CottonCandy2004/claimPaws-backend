<template>
  <el-tag :type="RESERVATION_STATUS_COLOR[status]">{{ RESERVATION_STATUS_MAP[status] }}</el-tag>
</template>

<script setup lang="ts">
import type { ReservationStatus } from '@/types'
import { RESERVATION_STATUS_MAP, RESERVATION_STATUS_COLOR } from '@/utils/constants'
defineProps<{ status: ReservationStatus }>()
</script>
