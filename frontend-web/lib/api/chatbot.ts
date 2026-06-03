import api from "@/lib/api";

export interface HistoryMessage {
  role: "user" | "model";
  content: string;
}

export interface ChatResponse {
  reply: string;
  updatedHistory: HistoryMessage[];
}

export const chatbotApi = {
  sendMessage(userId: number, message: string, history: HistoryMessage[]) {
    return api.post<{ data: ChatResponse }>("/chat/message", { userId, message, history });
  },
};
