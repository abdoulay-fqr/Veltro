import api from "@/lib/api";

export interface ConversationResponse {
  id: number;
  memberId: number;
  coachId: number;
  lastMessageAt: string | null;
  lastMessagePreview: string | null;
  unreadCount: number;
  createdAt: string;
}

export interface MessageResponse {
  id: number;
  conversationId: number;
  senderId: number;
  senderRole: "MEMBER" | "COACH";
  content: string;
  sentAt: string;
  readAt: string | null;
}

export const messagingApi = {
  listConversations() {
    return api.get<{ data: ConversationResponse[] }>("/conversations");
  },

  startConversation(coachId: number) {
    return api.post<{ data: ConversationResponse }>("/conversations", { coachId });
  },

  getMessages(conversationId: number, page = 0, size = 30) {
    return api.get<{ data: { content: MessageResponse[]; totalPages: number } }>(
      `/conversations/${conversationId}/messages`,
      { params: { page, size, sort: "sentAt,asc" } }
    );
  },

  sendMessage(conversationId: number, content: string) {
    return api.post<{ data: MessageResponse }>(
      `/conversations/${conversationId}/messages`,
      { content }
    );
  },

  markRead(conversationId: number) {
    return api.put(`/conversations/${conversationId}/read`);
  },

  markAllRead() {
    return api.put("/conversations/read-all");
  },

  getUnreadCount() {
    return api.get<{ data: { unreadCount: number } }>("/conversations/unread-count");
  },
};
