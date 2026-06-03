import '../../../core/network/dio_client.dart';

class MessagingRepository {
  Future<List<dynamic>> getConversations() async {
    final dio = DioClient.instance;
    final res = await dio.get('/conversations');
    return (res.data['data'] as List?) ?? [];
  }

  Future<Map<String, dynamic>> startConversation(int coachId) async {
    final dio = DioClient.instance;
    final res = await dio.post('/conversations', data: {'coachId': coachId});
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<List<dynamic>> getMessages(int conversationId, {int page = 0, int size = 30}) async {
    final dio = DioClient.instance;
    final res = await dio.get('/conversations/$conversationId/messages',
        queryParameters: {'page': page, 'size': size, 'sort': 'sentAt,asc'});
    return (res.data['data']?['content'] as List?) ?? [];
  }

  Future<Map<String, dynamic>> sendMessage(int conversationId, String content) async {
    final dio = DioClient.instance;
    final res = await dio.post('/conversations/$conversationId/messages', data: {'content': content});
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<void> markRead(int conversationId) async {
    final dio = DioClient.instance;
    await dio.put('/conversations/$conversationId/read');
  }

  Future<int> getUnreadCount() async {
    final dio = DioClient.instance;
    final res = await dio.get('/conversations/unread-count');
    return (res.data['data']?['unreadCount'] as int?) ?? 0;
  }
}
