import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'chats_screen.dart';

class ChatScreen extends ConsumerStatefulWidget {
  final int conversationId;
  const ChatScreen({super.key, required this.conversationId});

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final _msgCtrl = TextEditingController();
  final _scrollCtrl = ScrollController();
  List<dynamic> _messages = [];
  bool _sending = false;
  Timer? _pollTimer;

  @override
  void initState() {
    super.initState();
    _loadMessages();
    _pollTimer = Timer.periodic(const Duration(seconds: 3), (_) => _loadMessages());
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    _msgCtrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  Future<void> _loadMessages() async {
    try {
      final msgs = await ref.read(messagingRepoProvider).getMessages(widget.conversationId);
      if (mounted) setState(() => _messages = msgs);
      await ref.read(messagingRepoProvider).markRead(widget.conversationId);
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (_scrollCtrl.hasClients) {
          _scrollCtrl.animateTo(_scrollCtrl.position.maxScrollExtent,
              duration: const Duration(milliseconds: 200), curve: Curves.easeOut);
        }
      });
    } catch (_) {}
  }

  Future<void> _sendMessage() async {
    final text = _msgCtrl.text.trim();
    if (text.isEmpty || _sending) return;
    setState(() => _sending = true);
    try {
      await ref.read(messagingRepoProvider).sendMessage(widget.conversationId, text);
      _msgCtrl.clear();
      await _loadMessages();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Failed to send message')));
      }
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: Text('Conversation #${widget.conversationId}',
            style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600)),
        elevation: 0,
      ),
      body: Column(children: [
        Expanded(
          child: ListView.builder(
            controller: _scrollCtrl,
            padding: const EdgeInsets.all(16),
            itemCount: _messages.length,
            itemBuilder: (_, i) {
              final msg = _messages[i] as Map<String, dynamic>;
              // For simplicity, treat MEMBER role as "mine" — adjust based on actual role
              final isMine = msg['senderRole'] == 'MEMBER';
              final readAt = msg['readAt'];
              final isLast = i == _messages.length - 1;

              return Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Column(
                  crossAxisAlignment: isMine ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: isMine ? MainAxisAlignment.end : MainAxisAlignment.start,
                      children: [
                        if (!isMine) ...[
                          CircleAvatar(radius: 14, backgroundColor: const Color(0xFF2A2A2A),
                              child: const Icon(Icons.person, size: 14, color: Colors.white54)),
                          const SizedBox(width: 8),
                        ],
                        Flexible(
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                            decoration: BoxDecoration(
                              color: isMine ? Colors.white : const Color(0xFF2A2A2A),
                              borderRadius: BorderRadius.only(
                                topLeft: const Radius.circular(18),
                                topRight: const Radius.circular(18),
                                bottomLeft: Radius.circular(isMine ? 18 : 4),
                                bottomRight: Radius.circular(isMine ? 4 : 18),
                              ),
                            ),
                            child: Text(
                              msg['content']?.toString() ?? '',
                              style: TextStyle(
                                  color: isMine ? Colors.black : Colors.white,
                                  fontSize: 14),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 3),
                    Row(
                      mainAxisAlignment: isMine ? MainAxisAlignment.end : MainAxisAlignment.start,
                      children: [
                        if (isMine) ...[
                          if (isLast && readAt != null)
                            const Text('Read ', style: TextStyle(color: Colors.white38, fontSize: 10)),
                          Text(
                            msg['sentAt']?.toString().substring(11, 16) ?? '',
                            style: const TextStyle(color: Colors.white24, fontSize: 10),
                          ),
                        ] else
                          Text(
                            msg['sentAt']?.toString().substring(11, 16) ?? '',
                            style: const TextStyle(color: Colors.white24, fontSize: 10),
                          ),
                      ],
                    ),
                  ],
                ),
              );
            },
          ),
        ),
        Container(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
          decoration: const BoxDecoration(
            color: Color(0xFF1A1A1A),
            border: Border(top: BorderSide(color: Color(0xFF2A2A2A))),
          ),
          child: Row(children: [
            Expanded(
              child: TextField(
                controller: _msgCtrl,
                style: const TextStyle(color: Colors.white, fontSize: 14),
                maxLines: null,
                onSubmitted: (_) => _sendMessage(),
                decoration: InputDecoration(
                  hintText: 'Message…',
                  hintStyle: const TextStyle(color: Colors.white38),
                  filled: true, fillColor: const Color(0xFF2A2A2A),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(24), borderSide: BorderSide.none),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                ),
              ),
            ),
            const SizedBox(width: 8),
            GestureDetector(
              onTap: _sendMessage,
              child: Container(
                width: 44, height: 44,
                decoration: const BoxDecoration(color: Colors.white, shape: BoxShape.circle),
                child: Icon(Icons.send, color: Colors.black, size: 18),
              ),
            ),
          ]),
        ),
      ]),
    );
  }
}
