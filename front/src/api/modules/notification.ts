import { get, post, put, del } from '@/api/request'
import type { WebhookConfig, DeliveryAudit, PageResult, PageParams } from '@/types'

export function getWebhookList(params: PageParams) { return get<PageResult<WebhookConfig>>('/webhooks', params) }
export function getWebhookById(id: number) { return get<WebhookConfig>(`/webhooks/${id}`) }
export function createWebhook(data: Partial<WebhookConfig>) { return post<WebhookConfig>('/webhooks', data) }
export function updateWebhook(id: number, data: Partial<WebhookConfig>) { return put<WebhookConfig>(`/webhooks/${id}`, data) }
export function deleteWebhook(id: number) { return del<void>(`/webhooks/${id}`) }
export function testWebhook(id: number) { return post<void>(`/webhooks/${id}/test`) }
export function getDeliveryAuditList(params: PageParams & { webhookId?: number; status?: string }) { return get<PageResult<DeliveryAudit>>('/webhooks/delivery-audits', params) }
export function retryDelivery(auditId: number) { return post<void>(`/webhooks/delivery-audits/${auditId}/retry`) }
