import '../../../core/network/dio_client.dart';

class SubscriptionRepository {
  Future<Map<String, dynamic>> getActivePlan(int memberId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/subscriptions/$memberId');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<List<dynamic>> getInvoices(int memberId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/subscriptions/$memberId/invoices');
    return res.data['data'] as List<dynamic>;
  }

  Future<List<dynamic>> getPlans() async {
    final dio = DioClient.instance;
    final res = await dio.get('/subscriptions/plans');
    return res.data['data'] as List<dynamic>;
  }

  Future<Map<String, dynamic>> upgradePlan(int memberId, String plan, String paymentMethod) async {
    final dio = DioClient.instance;
    final res = await dio.post('/subscriptions', data: {
      'memberId': memberId,
      'plan': plan,
      'paymentMethod': paymentMethod,
    });
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> pause(int subscriptionId) async {
    final dio = DioClient.instance;
    final res = await dio.put('/subscriptions/$subscriptionId/pause');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> resume(int subscriptionId) async {
    final dio = DioClient.instance;
    final res = await dio.put('/subscriptions/$subscriptionId/resume');
    return res.data['data'] as Map<String, dynamic>;
  }
}
