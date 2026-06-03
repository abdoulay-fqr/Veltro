import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../data/messaging_repository.dart';

final messagingRepoProvider = Provider<MessagingRepository>((ref) => MessagingRepository());

final conversationsProvider = FutureProvider<List<dynamic>>((ref) async {
  return ref.read(messagingRepoProvider).getConversations();
});

class ChatsScreen extends ConsumerWidget {
  const ChatsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final convsAsync = ref.watch(conversationsProvider);

    return Scaffold(
      backgroundColor: const Color(0xFF0F0F0F),
      appBar: AppBar(
        backgroundColor: const Color(0xFF0F0F0F),
        foregroundColor: Colors.white,
        title: const Text('Messages', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, size: 20),
            onPressed: () => ref.invalidate(conversationsProvider),
          ),
        ],
      ),
      body: convsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e', style: const TextStyle(color: Colors.white54))),
        data: (convs) {
          if (convs.isEmpty) {
            return const Center(child: Text('No conversations yet', style: TextStyle(color: Colors.white38)));
          }
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: convs.length,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: (_, i) {
              final c = convs[i] as Map<String, dynamic>;
              final unread = (c['unreadCount'] as int? ?? 0);
              return GestureDetector(
                onTap: () => context.push('/chat/${c['id']}'),
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E1E1E),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Row(
                    children: [
                      CircleAvatar(
                        radius: 22,
                        backgroundColor: const Color(0xFF2A2A2A),
                        child: Text(
                          'C${c['coachId']}',
                          style: const TextStyle(color: Colors.white54, fontSize: 11),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Coach #${c['coachId']}',
                                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600, fontSize: 14)),
                            const SizedBox(height: 2),
                            Text(
                              c['lastMessagePreview']?.toString() ?? 'No messages yet',
                              style: const TextStyle(color: Colors.white38, fontSize: 12),
                              maxLines: 1, overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                      if (unread > 0)
                        Container(
                          width: 22, height: 22,
                          decoration: const BoxDecoration(color: Colors.white, shape: BoxShape.circle),
                          child: Center(child: Text('$unread',
                              style: const TextStyle(color: Colors.black, fontSize: 11, fontWeight: FontWeight.w700))),
                        ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
