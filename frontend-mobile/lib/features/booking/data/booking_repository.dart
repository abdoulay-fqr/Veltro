import '../../../core/network/dio_client.dart';

class BookingRepository {
  Future<List<dynamic>> getCourses({String? level, String? status}) async {
    final dio = DioClient.instance;
    final params = <String, dynamic>{};
    if (level != null) params['level'] = level;
    if (status != null) params['status'] = status;
    final res = await dio.get('/courses', queryParameters: params);
    return (res.data['data']?['content'] as List?) ?? [];
  }

  Future<Map<String, dynamic>> getCourseById(int courseId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/courses/$courseId');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> bookCourse(int courseId, String? memberEmail) async {
    final dio = DioClient.instance;
    final res = await dio.post('/bookings', data: {
      'courseId': courseId,
      'memberEmail': ?memberEmail,
    });
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> cancelBooking(int bookingId) async {
    final dio = DioClient.instance;
    final res = await dio.delete('/bookings/$bookingId');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<List<dynamic>> getMyBookings(int memberId, {String? status}) async {
    final dio = DioClient.instance;
    final params = <String, dynamic>{};
    if (status != null) params['status'] = status;
    final res = await dio.get('/bookings/member/$memberId', queryParameters: params);
    return (res.data['data'] as List?) ?? [];
  }
}
