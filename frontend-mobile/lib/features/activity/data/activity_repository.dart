import '../../../core/network/dio_client.dart';

class ActivityRepository {
  Future<Map<String, dynamic>> getMemberStats(int memberId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/activity/stats/$memberId');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<List<dynamic>> getWeeklyCaloriesByDay(int memberId) async {
    final dio = DioClient.instance;
    // Returns sessions for this week to build a chart
    final res = await dio.get('/activity/sessions/$memberId', queryParameters: {'size': 50});
    return (res.data['data']?['content'] as List?) ?? [];
  }

  Future<List<dynamic>> getRecentSessions(int memberId, {int size = 10}) async {
    final dio = DioClient.instance;
    final res = await dio.get('/activity/sessions/$memberId', queryParameters: {'size': size});
    return (res.data['data']?['content'] as List?) ?? [];
  }
}
