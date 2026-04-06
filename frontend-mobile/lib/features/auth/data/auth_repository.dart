import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class AuthRepository {
  static const String _baseUrl = 'http://localhost:8080/api/v1';
  static const _storage = FlutterSecureStorage();
  final Dio _dio = Dio(BaseOptions(
    baseUrl: _baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {'Content-Type': 'application/json'},
  ));

  Future<Map<String, dynamic>> login(String identifier, String password) async {
    final response = await _dio.post('/auth/login', data: {
      'identifier': identifier,
      'password': password,
    });
    final data = response.data['data'];
    await _storage.write(key: 'access_token', value: data['accessToken']);
    await _storage.write(key: 'refresh_token', value: data['refreshToken']);
    await _storage.write(key: 'role', value: data['role']);
    await _storage.write(key: 'identifier', value: identifier);
    return data;
  }

  Future<Map<String, dynamic>> register(String identifier, String password, String role) async {
    final response = await _dio.post('/auth/register', data: {
      'identifier': identifier,
      'password': password,
      'role': role,
    });
    final data = response.data['data'];
    await _storage.write(key: 'access_token', value: data['accessToken']);
    await _storage.write(key: 'refresh_token', value: data['refreshToken']);
    await _storage.write(key: 'role', value: data['role']);
    await _storage.write(key: 'identifier', value: identifier);
    return data;
  }

  Future<void> logout() async {
    final token = await _storage.read(key: 'access_token');
    if (token != null) {
      try {
        await _dio.post(
          '/auth/logout',
          options: Options(headers: {'Authorization': 'Bearer $token'}),
        );
      } catch (_) {}
    }
    await _storage.deleteAll();
  }

  Future<Map<String, String?>> getSavedSession() async {
    final role = await _storage.read(key: 'role');
    final identifier = await _storage.read(key: 'identifier');
    final token = await _storage.read(key: 'access_token');
    return {'role': role, 'identifier': identifier, 'token': token};
  }
}