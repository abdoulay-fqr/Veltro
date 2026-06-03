"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import ReactMarkdown from "react-markdown";
import { chatbotApi, HistoryMessage } from "@/lib/api/chatbot";
import { useAuth } from "@/lib/auth/useAuth";
import { MessageCircle, X, Send, Bot } from "lucide-react";

const QUICK_CHIPS = [
  "What's my subscription?",
  "My next class?",
  "How many sessions this week?",
  "What's in the shop?",
];

interface Message {
  role: "user" | "model";
  content: string;
}

export default function ChatWidget() {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [history, setHistory] = useState<HistoryMessage[]>([]);
  const [input, setInput] = useState("");
  const [thinking, setThinking] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, thinking]);

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  const sendMessage = useCallback(async (text: string) => {
    if (!text.trim() || thinking || !user) return;

    const userMessage: Message = { role: "user", content: text.trim() };
    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setThinking(true);

    try {
      const res = await chatbotApi.sendMessage(
        // userId derived from identifier hash — in production this would be the real userId from auth
        Math.abs(user.identifier.split("").reduce((a, c) => a + c.charCodeAt(0), 0)),
        text.trim(),
        history
      );
      const data = (res.data as unknown as { data: { reply: string; updatedHistory: HistoryMessage[] } }).data;
      setMessages((prev) => [...prev, { role: "model", content: data.reply }]);
      setHistory(data.updatedHistory);
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status;
      const fallback =
        status === 429
          ? "You've sent too many messages. Please wait a bit and try again."
          : "I'm having trouble right now. Please try again later.";
      setMessages((prev) => [...prev, { role: "model", content: fallback }]);
    } finally {
      setThinking(false);
    }
  }, [thinking, user, history]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage(input);
    }
  };

  if (!user) return null;

  return (
    <>
      {/* Floating trigger button */}
      {!open && (
        <button
          onClick={() => setOpen(true)}
          className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full bg-teal-600 text-white shadow-lg hover:bg-teal-700 transition-colors flex items-center justify-center"
          aria-label="Open Veltro Assistant"
        >
          <MessageCircle size={24} />
        </button>
      )}

      {/* Chat panel */}
      {open && (
        <div className="fixed bottom-6 right-6 z-50 w-96 flex flex-col rounded-2xl shadow-2xl bg-white border border-zinc-200 overflow-hidden"
             style={{ height: "520px" }}>
          {/* Header */}
          <div className="flex items-center gap-3 px-4 py-3 bg-teal-600 text-white shrink-0">
            <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">
              <Bot size={18} />
            </div>
            <div className="flex-1">
              <p className="text-sm font-semibold">Veltro Assistant</p>
              <p className="text-xs text-teal-100">Powered by Gemini AI</p>
            </div>
            <button
              onClick={() => setOpen(false)}
              className="text-white/80 hover:text-white transition-colors"
            >
              <X size={18} />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3 bg-zinc-50">
            {messages.length === 0 && !thinking && (
              <div className="space-y-3">
                <div className="flex items-start gap-2">
                  <div className="w-7 h-7 rounded-full bg-teal-100 flex items-center justify-center shrink-0 mt-0.5">
                    <Bot size={14} className="text-teal-600" />
                  </div>
                  <div className="bg-white rounded-2xl rounded-tl-sm px-3 py-2 shadow-sm text-sm text-zinc-700 max-w-[85%]">
                    Hi! I'm Veltro Assistant. Ask me anything about your subscription, classes, or gym activity.
                  </div>
                </div>

                {/* Quick action chips */}
                <div className="flex flex-wrap gap-2 pl-9">
                  {QUICK_CHIPS.map((chip) => (
                    <button
                      key={chip}
                      onClick={() => sendMessage(chip)}
                      className="rounded-full border border-teal-200 bg-white px-3 py-1 text-xs text-teal-700 hover:bg-teal-50 transition-colors"
                    >
                      {chip}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.role === "user" ? "justify-end" : "items-start gap-2"}`}>
                {msg.role === "model" && (
                  <div className="w-7 h-7 rounded-full bg-teal-100 flex items-center justify-center shrink-0 mt-0.5">
                    <Bot size={14} className="text-teal-600" />
                  </div>
                )}
                <div className={`max-w-[85%] rounded-2xl px-3 py-2 text-sm shadow-sm ${
                  msg.role === "user"
                    ? "bg-zinc-900 text-white rounded-br-sm"
                    : "bg-white text-zinc-800 rounded-tl-sm"
                }`}>
                  {msg.role === "model" ? (
                    <div className="prose prose-sm prose-zinc max-w-none">
                      <ReactMarkdown>{msg.content}</ReactMarkdown>
                    </div>
                  ) : (
                    <p>{msg.content}</p>
                  )}
                </div>
              </div>
            ))}

            {/* Thinking indicator */}
            {thinking && (
              <div className="flex items-start gap-2">
                <div className="w-7 h-7 rounded-full bg-teal-100 flex items-center justify-center shrink-0">
                  <Bot size={14} className="text-teal-600" />
                </div>
                <div className="bg-white rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm">
                  <span className="flex gap-1">
                    {[0, 1, 2].map((i) => (
                      <span
                        key={i}
                        className="w-1.5 h-1.5 rounded-full bg-zinc-400 animate-bounce"
                        style={{ animationDelay: `${i * 0.15}s` }}
                      />
                    ))}
                  </span>
                </div>
              </div>
            )}

            <div ref={bottomRef} />
          </div>

          {/* Input */}
          <div className="shrink-0 px-3 py-3 border-t border-zinc-200 bg-white flex gap-2">
            <input
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask anything…"
              disabled={thinking}
              className="flex-1 rounded-xl border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500 disabled:opacity-50"
            />
            <button
              onClick={() => sendMessage(input)}
              disabled={!input.trim() || thinking}
              className="rounded-xl bg-teal-600 px-3 py-2 text-white hover:bg-teal-700 disabled:opacity-40 transition-colors"
            >
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
    </>
  );
}
