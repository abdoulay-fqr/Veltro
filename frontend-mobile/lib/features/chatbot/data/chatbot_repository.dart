import '../../../core/network/dio_client.dart';

class ChatbotRepository {
  Future<Map<String, dynamic>> sendMessage({
    required int userId,
    required String message,
    required List<Map<String, String>> history,
  }) async {
    final dio = DioClient.instance;
    final res = await dio.post('/chat/message', data: {
      'userId': userId,
      'message': message,
      'history': history,
    });
    return res.data['data'] as Map<String, dynamic>;
  }
}
