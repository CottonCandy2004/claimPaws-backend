import { ref, reactive } from 'vue'
import type { PageParams } from '@/types'

export function usePagination(defaultSize = 10) {
  const pageParams = reactive<PageParams>({ page: 1, size: defaultSize })
  const total = ref(0)
  const loading = ref(false)

  function resetPage() { pageParams.page = 1 }
  function handlePageChange(page: number) { pageParams.page = page }
  function handleSizeChange(size: number) { pageParams.size = size; pageParams.page = 1 }

  return { pageParams, total, loading, resetPage, handlePageChange, handleSizeChange }
}
