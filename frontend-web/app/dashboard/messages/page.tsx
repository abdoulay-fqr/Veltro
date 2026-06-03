"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import { toast } from "sonner";
import { messagingApi, ConversationResponse, MessageResponse } from "@/lib/api/messaging";
import { useAuth } from "@/lib/auth/useAuth";
import { Send } from "lucide-react";

export default function MessagesPage() {
  const { user } = useAuth();
  const [conversations, setConversations] = useState<ConversationResponse[]>([]);
  const [selected, setSelected] = useState<ConversationResponse | null>(null);
  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const loadConversations = useCallback(async () => {
    try {
      const res = await messagingApi.listConversations();
      setConversations((res.data as unknown as { data: ConversationResponse[] }).data ?? []);
    } catch {}
  }, []);

  const loadMessages = useCallback(async (convId: number) => {
    try {
      const res = await messagingApi.getMessages(convId);
      const data = (res.data as unknown as { data: { content: MessageResponse[] } }).data;
      setMessages(data?.content ?? []);
      bottomRef.current?.scrollIntoView({ behavior: "smooth" });
      await messagingApi.markRead(convId);
    } catch {}
  }, []);

  useEffect(() => { loadConversations(); }, [loadConversations]);

  useEffect(() => {
    if (!selected) return;
    loadMessages(selected.id);
    pollingRef.current = setInterval(() => loadMessages(selected.id), 3000);
    return () => { if (pollingRef.current) clearInterval(pollingRef.current); };
  }, [selected, loadMessages]);

  const handleSelect = (conv: ConversationResponse) => {
    setSelected(conv);
    setMessages([]);
  };

  const handleSend = async () => {
    if (!input.trim() || !selected || sending) return;
    setSending(true);
    try {
      await messagingApi.sendMessage(selected.id, input.trim());
      setInput("");
      await loadMessages(selected.id);
      loadConversations();
    } catch { toast.error("Failed to send message"); }
    finally { setSending(false); }
  };

  const myId = user?.identifier;
  const isMyMessage = (msg: MessageResponse) => {
    const myRole = user?.role === "COACH" ? "COACH" : "MEMBER";
    return msg.senderRole === myRole;
  };

  return (
    <div className="flex h-full" style={{ height: "calc(100vh - 0px)" }}>
      {/* Conversation list */}
      <aside className="w-72 shrink-0 border-r border-zinc-200 bg-white flex flex-col">
        <div className="px-4 py-4 border-b border-zinc-100">
          <h2 className="text-sm font-semibold text-zinc-900">Messages</h2>
        </div>
        <div className="flex-1 overflow-y-auto">
          {conversations.length === 0 ? (
            <p className="p-4 text-sm text-zinc-400">No conversations yet.</p>
          ) : (
            conversations.map((conv) => (
              <button
                key={conv.id}
                onClick={() => handleSelect(conv)}
                className={`w-full text-left px-4 py-3 border-b border-zinc-50 hover:bg-zinc-50 transition-colors ${selected?.id === conv.id ? "bg-zinc-100" : ""}`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-zinc-900 truncate">
                      {user?.role === "COACH" ? `Member #${conv.memberId}` : `Coach #${conv.coachId}`}
                    </p>
                    <p className="text-xs text-zinc-500 truncate mt-0.5">
                      {conv.lastMessagePreview ?? "No messages yet"}
                    </p>
                  </div>
                  {conv.unreadCount > 0 && (
                    <span className="ml-2 shrink-0 flex items-center justify-center w-5 h-5 rounded-full bg-zinc-900 text-white text-xs font-medium">
                      {conv.unreadCount}
                    </span>
                  )}
                </div>
              </button>
            ))
          )}
        </div>
      </aside>

      {/* Message thread */}
      <div className="flex-1 flex flex-col bg-zinc-50">
        {!selected ? (
          <div className="flex-1 flex items-center justify-center text-zinc-400 text-sm">
            Select a conversation to start messaging
          </div>
        ) : (
          <>
            <div className="px-4 py-3 border-b border-zinc-200 bg-white">
              <p className="text-sm font-semibold text-zinc-900">
                {user?.role === "COACH" ? `Member #${selected.memberId}` : `Coach #${selected.coachId}`}
              </p>
            </div>

            <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
              {messages.map((msg) => {
                const mine = isMyMessage(msg);
                return (
                  <div key={msg.id} className={`flex ${mine ? "justify-end" : "justify-start"}`}>
                    <div className={`max-w-sm rounded-2xl px-4 py-2 text-sm ${
                      mine ? "bg-zinc-900 text-white rounded-br-sm" : "bg-white text-zinc-900 shadow-sm rounded-bl-sm"
                    }`}>
                      <p>{msg.content}</p>
                      <p className={`text-xs mt-1 ${mine ? "text-zinc-400" : "text-zinc-400"}`}>
                        {new Date(msg.sentAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                        {mine && msg.readAt && <span className="ml-2">Read</span>}
                      </p>
                    </div>
                  </div>
                );
              })}
              <div ref={bottomRef} />
            </div>

            <div className="px-4 py-3 border-t border-zinc-200 bg-white flex gap-2">
              <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && handleSend()}
                placeholder="Type a message…"
                className="flex-1 rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
              />
              <button
                onClick={handleSend}
                disabled={!input.trim() || sending}
                className="rounded-lg bg-zinc-900 px-4 py-2 text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors"
              >
                <Send size={16} />
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
