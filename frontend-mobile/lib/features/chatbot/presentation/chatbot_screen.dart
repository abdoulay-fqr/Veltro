import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/chatbot_repository.dart';

// ── Providers ────────────────────────────────────────────────────────────────

final chatbotRepoProvider = Provider<ChatbotRepository>((ref) => ChatbotRepository());

class ChatMessage {
  final String role;   // "user" or "model"
  final String content;
  const ChatMessage({required this.role, required this.content});
}

class ChatbotState {
  final List<ChatMessage> messages;
  final List<Map<String, String>> history;
  final bool thinking;
  final String? error;
  const ChatbotState({
    this.messages = const [],
    this.history = const [],
    this.thinking = false,
    this.error,
  });
  ChatbotState copyWith({
    List<ChatMessage>? messages,
    List<Map<String, String>>? history,
    bool? thinking,
    String? error,
  }) => ChatbotState(
    messages: messages ?? this.messages,
    history: history ?? this.history,
    thinking: thinking ?? this.thinking,
    error: error,
  );
}

final chatbotNotifierProvider =
    StateNotifierProvider<ChatbotNotifier, ChatbotState>(
  (ref) => ChatbotNotifier(ref.read(chatbotRepoProvider)),
);

class ChatbotNotifier extends StateNotifier<ChatbotState> {
  final ChatbotRepository _repo;
  static const int _stubUserId = 1;

  ChatbotNotifier(this._repo) : super(const ChatbotState());

  Future<void> send(String message) async {
    if (message.trim().isEmpty || state.thinking) return;

    final userMsg = ChatMessage(role: 'user', content: message.trim());
    state = state.copyWith(
      messages: [...state.messages, userMsg],
      thinking: true,
    );

    try {
      final result = await _repo.sendMessage(
        userId: _stubUserId,
        message: message.trim(),
        history: state.history,
      );

      final reply = result['reply'] as String? ?? 'No response received.';
      final rawHistory = result['updatedHistory'] as List<dynamic>? ?? [];
      final updatedHistory = rawHistory
          .cast<Map<String, dynamic>>()
          .map((h) => {'role': h['role'].toString(), 'content': h['content'].toString()})
          .toList();

      final modelMsg = ChatMessage(role: 'model', content: reply);
      state = state.copyWith(
        messages: [...state.messages, modelMsg],
        history: updatedHistory,
        thinking: false,
      );
    } catch (e) {
      final errorMsg = e.toString().contains('429')
          ? 'You\'ve sent too many messages. Please wait a bit and try again.'
          : 'I\'m having trouble connecting. Please try again later.';
      state = state.copyWith(
        messages: [...state.messages, ChatMessage(role: 'model', content: errorMsg)],
        thinking: false,
        error: e.toString(),
      );
    }
  }

  void clear() => state = const ChatbotState();
}

// ── Screen ────────────────────────────────────────────────────────────────────

const _quickChips = [
  'My subscription',
  'Next class',
  'This week\'s activity',
  'Shop products',
];

class ChatbotScreen extends ConsumerStatefulWidget {
  const ChatbotScreen({super.key});

  @override
  ConsumerState<ChatbotScreen> createState() => _ChatbotScreenState();
}

class _ChatbotScreenState extends ConsumerState<ChatbotScreen> {
  final _inputCtrl = TextEditingController();
  final _scrollCtrl = ScrollController();

  @override
  void dispose() {
    _inputCtrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 250),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _send(String text) async {
    if (text.trim().isEmpty) return;
    _inputCtrl.clear();
    await ref.read(chatbotNotifierProvider.notifier).send(text);
    _scrollToBottom();
  }

  @override
  Widget build(BuildContext context) {
    final chatState = ref.watch(chatbotNotifierProvider);

    if (chatState.messages.isNotEmpty) _scrollToBottom();

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: Row(children: [
          Container(
            width: 32, height: 32,
            decoration: BoxDecoration(
              color: const Color(0xFF0D9488),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(Icons.smart_toy_outlined, color: Colors.white, size: 18),
          ),
          const SizedBox(width: 10),
          const Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Veltro Assistant', style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600)),
            Text('Powered by Gemini AI', style: TextStyle(fontSize: 10, color: Colors.white38)),
          ]),
        ]),
        elevation: 0,
        actions: [
          if (chatState.messages.isNotEmpty)
            TextButton(
              onPressed: () => ref.read(chatbotNotifierProvider.notifier).clear(),
              child: const Text('Clear', style: TextStyle(color: Colors.white38, fontSize: 12)),
            ),
        ],
      ),
      body: Column(children: [
        // Messages
        Expanded(
          child: chatState.messages.isEmpty && !chatState.thinking
              ? _WelcomeView(onChipTap: _send)
              : ListView.builder(
                  controller: _scrollCtrl,
                  padding: const EdgeInsets.all(16),
                  itemCount: chatState.messages.length + (chatState.thinking ? 1 : 0),
                  itemBuilder: (_, i) {
                    if (i == chatState.messages.length) {
                      return const _ThinkingBubble();
                    }
                    final msg = chatState.messages[i];
                    return _MessageBubble(message: msg);
                  },
                ),
        ),

        // Input row
        Container(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 20),
          decoration: const BoxDecoration(
            color: Color(0xFF1A1A1A),
            border: Border(top: BorderSide(color: Color(0xFF2A2A2A))),
          ),
          child: Row(children: [
            Expanded(
              child: TextField(
                controller: _inputCtrl,
                style: const TextStyle(color: Colors.white, fontSize: 14),
                maxLines: null,
                onSubmitted: _send,
                decoration: InputDecoration(
                  hintText: 'Ask me anything…',
                  hintStyle: const TextStyle(color: Colors.white38),
                  filled: true,
                  fillColor: const Color(0xFF2A2A2A),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide.none,
                  ),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                ),
              ),
            ),
            const SizedBox(width: 10),
            GestureDetector(
              onTap: () => _send(_inputCtrl.text),
              child: Container(
                width: 44, height: 44,
                decoration: const BoxDecoration(
                  color: Color(0xFF0D9488),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.send, color: Colors.white, size: 18),
              ),
            ),
          ]),
        ),
      ]),
    );
  }
}

// ── Sub-widgets ───────────────────────────────────────────────────────────────

class _WelcomeView extends StatelessWidget {
  final void Function(String) onChipTap;
  const _WelcomeView({required this.onChipTap});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 72, height: 72,
              decoration: BoxDecoration(
                color: const Color(0xFF0D9488).withValues(alpha: 0.15),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.smart_toy_outlined, color: Color(0xFF0D9488), size: 36),
            ),
            const SizedBox(height: 16),
            const Text('Veltro Assistant',
                style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            const Text(
              'Ask me about your subscription,\nclasses, activity, or anything gym-related.',
              style: TextStyle(color: Colors.white54, fontSize: 14),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 28),
            Wrap(
              spacing: 8, runSpacing: 8,
              alignment: WrapAlignment.center,
              children: _quickChips.map((chip) => GestureDetector(
                onTap: () => onChipTap(chip),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  decoration: BoxDecoration(
                    border: Border.all(color: const Color(0xFF0D9488).withValues(alpha: 0.4)),
                    borderRadius: BorderRadius.circular(20),
                    color: const Color(0xFF0D9488).withValues(alpha: 0.08),
                  ),
                  child: Text(chip,
                      style: const TextStyle(color: Color(0xFF0D9488), fontSize: 13)),
                ),
              )).toList(),
            ),
          ],
        ),
      );
}

class _MessageBubble extends StatefulWidget {
  final ChatMessage message;
  const _MessageBubble({required this.message});

  @override
  State<_MessageBubble> createState() => _MessageBubbleState();
}

class _MessageBubbleState extends State<_MessageBubble> {
  String _displayed = '';
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    if (widget.message.role == 'model') {
      _startTypewriter();
    } else {
      _displayed = widget.message.content;
    }
  }

  void _startTypewriter() {
    final full = widget.message.content;
    int idx = 0;
    _timer = Timer.periodic(const Duration(milliseconds: 18), (t) {
      if (!mounted) { t.cancel(); return; }
      if (idx >= full.length) { t.cancel(); return; }
      setState(() => _displayed = full.substring(0, ++idx));
    });
  }

  @override
  void dispose() { _timer?.cancel(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final isUser = widget.message.role == 'user';
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isUser) ...[
            Container(
              width: 28, height: 28,
              decoration: BoxDecoration(
                color: const Color(0xFF0D9488).withValues(alpha: 0.15),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.smart_toy_outlined, color: Color(0xFF0D9488), size: 16),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: isUser ? const Color(0xFF0D9488) : const Color(0xFF1E1E1E),
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(18),
                  topRight: const Radius.circular(18),
                  bottomLeft: Radius.circular(isUser ? 18 : 4),
                  bottomRight: Radius.circular(isUser ? 4 : 18),
                ),
              ),
              child: Text(
                isUser ? widget.message.content : _displayed,
                style: TextStyle(
                  color: isUser ? Colors.white : Colors.white.withValues(alpha: 0.87),
                  fontSize: 14,
                  height: 1.4,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ThinkingBubble extends StatefulWidget {
  const _ThinkingBubble();
  @override
  State<_ThinkingBubble> createState() => _ThinkingBubbleState();
}

class _ThinkingBubbleState extends State<_ThinkingBubble>
    with SingleTickerProviderStateMixin {
  late AnimationController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 1200))
      ..repeat();
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Container(
              width: 28, height: 28,
              decoration: BoxDecoration(
                color: const Color(0xFF0D9488).withValues(alpha: 0.15),
                shape: BoxShape.circle,
              ),
              child: AnimatedBuilder(
                animation: _ctrl,
                builder: (_, _) => Icon(
                  Icons.smart_toy_outlined,
                  color: Color.lerp(const Color(0xFF0D9488),
                      const Color(0xFF0D9488).withValues(alpha: 0.3), _ctrl.value),
                  size: 16,
                ),
              ),
            ),
            const SizedBox(width: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              decoration: BoxDecoration(
                color: const Color(0xFF1E1E1E),
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(18),
                  topRight: Radius.circular(18),
                  bottomRight: Radius.circular(18),
                  bottomLeft: Radius.circular(4),
                ),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: List.generate(3, (i) => AnimatedBuilder(
                  animation: _ctrl,
                  builder: (_, _) {
                    final delay = i / 3;
                    final t = ((_ctrl.value - delay) % 1.0).clamp(0.0, 1.0);
                    return Padding(
                      padding: EdgeInsets.only(right: i < 2 ? 4 : 0),
                      child: Container(
                        width: 6, height: 6,
                        decoration: BoxDecoration(
                          color: Color.lerp(
                              Colors.white38, Colors.white70, t < 0.5 ? t * 2 : (1 - t) * 2),
                          shape: BoxShape.circle,
                        ),
                      ),
                    );
                  },
                )),
              ),
            ),
          ],
        ),
      );
}
