import '../../../core/network/dio_client.dart';

class ShopRepository {
  Future<List<dynamic>> getProducts({String? category}) async {
    final dio = DioClient.instance;
    final params = <String, dynamic>{'size': 50};
    if (category != null) params['category'] = category;
    final res = await dio.get('/shop/products', queryParameters: params);
    return (res.data['data']?['content'] as List?) ?? [];
  }

  Future<Map<String, dynamic>> placeOrder(
      List<Map<String, dynamic>> items, String shippingAddress) async {
    final dio = DioClient.instance;
    final res = await dio.post('/shop/orders', data: {
      'items': items,
      'shippingAddress': shippingAddress,
    });
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<List<dynamic>> getMyOrders() async {
    final dio = DioClient.instance;
    final res = await dio.get('/shop/orders');
    return (res.data['data']?['content'] as List?) ?? [];
  }
}
