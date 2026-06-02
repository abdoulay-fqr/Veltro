import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/network/dio_client.dart';

class ProfileRepository {
  static const _storage = FlutterSecureStorage();

  Future<Map<String, dynamic>> getMemberProfile(int memberProfileId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/users/members/$memberProfileId');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> updateMemberProfile(
    int memberProfileId,
    Map<String, dynamic> fields,
  ) async {
    final dio = DioClient.instance;
    final res = await dio.put('/users/members/$memberProfileId', data: fields);
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> uploadAvatar(int memberProfileId, String filePath) async {
    final dio = DioClient.instance;
    final form = FormData.fromMap({
      'file': await MultipartFile.fromFile(filePath, filename: 'avatar.jpg'),
    });
    final res = await dio.post(
      '/users/members/$memberProfileId/avatar',
      data: form,
      options: Options(headers: {'Content-Type': 'multipart/form-data'}),
    );
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> getHealthProfile(int memberProfileId) async {
    final dio = DioClient.instance;
    final res = await dio.get('/users/members/$memberProfileId/health');
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> upsertHealthProfile(
    int memberProfileId,
    Map<String, dynamic> fields,
  ) async {
    final dio = DioClient.instance;
    final res = await dio.put('/users/members/$memberProfileId/health', data: fields);
    return res.data['data'] as Map<String, dynamic>;
  }

  Future<void> changePassword(String currentPassword, String newPassword) async {
    final dio = DioClient.instance;
    await dio.post('/auth/change-password', data: {
      'currentPassword': currentPassword,
      'newPassword': newPassword,
    });
  }

  Future<String?> getSavedIdentifier() => _storage.read(key: 'identifier');
  Future<String?> getSavedUserId() => _storage.read(key: 'user_id');
}
